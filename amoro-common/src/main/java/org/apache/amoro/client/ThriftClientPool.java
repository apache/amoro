/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.amoro.client;

import org.apache.amoro.shade.thrift.org.apache.thrift.TConfiguration;
import org.apache.amoro.shade.thrift.org.apache.thrift.transport.TSocket;
import org.apache.amoro.shade.thrift.org.apache.thrift.transport.TTransport;
import org.apache.amoro.shade.thrift.org.apache.thrift.transport.TTransportException;
import org.apache.amoro.shade.thrift.org.apache.thrift.transport.layered.TFramedTransport;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThriftClientPool<
    T extends org.apache.amoro.shade.thrift.org.apache.thrift.TServiceClient> {
  private static final Logger LOG = LoggerFactory.getLogger(ThriftClientPool.class);
  private static final int RECONNECT_INTERVAL = 2000;
  private static final int BORROW_ATTEMPTS = 5;
  private final ThriftClientFactory clientFactory;
  private final ThriftPingFactory pingFactory;
  private final GenericObjectPool<ThriftClient<T>> pool;
  private final PoolConfig<T> poolConfig;

  public ThriftClientPool(
      String url,
      ThriftClientFactory factory,
      ThriftPingFactory pingFactory,
      PoolConfig<T> config,
      String serviceName) {
    if (url == null || url.isEmpty()) {
      throw new IllegalArgumentException("url is empty!");
    }
    if (factory == null) {
      throw new IllegalArgumentException("factory is empty!");
    }
    if (config == null) {
      throw new IllegalArgumentException("config is empty!");
    }

    this.clientFactory = factory;
    this.pingFactory = pingFactory;
    this.poolConfig = config;
    // test if config change
    this.poolConfig.setTestOnReturn(true);
    this.poolConfig.setTestOnBorrow(true);
    this.pool =
        new GenericObjectPool<>(
            new BasePooledObjectFactory<ThriftClient<T>>() {

              @Override
              public ThriftClient<T> create() throws Exception {

                // get from global list first
                AmsThriftUrl amsThriftUrl = AmsThriftUrl.parse(url, serviceName);
                ServiceInfo serviceInfo = new ServiceInfo(amsThriftUrl.host(), amsThriftUrl.port());
                TTransport transport = getTransport(serviceInfo);

                try {
                  transport.open();
                } catch (TTransportException e) {
                  LOG.warn(
                      "Transport open failed, service address: {}:{}",
                      serviceInfo.getHost(),
                      serviceInfo.getPort());
                  if (poolConfig.isAutoReconnect() && poolConfig.getMaxReconnects() > 0) {
                    for (int i = 0; i < poolConfig.getMaxReconnects(); i++) {
                      try {
                        amsThriftUrl = AmsThriftUrl.parse(url, serviceName);
                        serviceInfo.setHost(amsThriftUrl.host());
                        serviceInfo.setPort(amsThriftUrl.port());
                        transport = getTransport(serviceInfo); // while break here
                        LOG.info(
                            "Reconnecting service address: {}:{}",
                            serviceInfo.getHost(),
                            serviceInfo.getPort());
                        transport.open();
                        break;
                      } catch (TTransportException e2) {
                        LOG.warn(
                            "Reconnected service address: {}:{} failed",
                            serviceInfo.getHost(),
                            serviceInfo.getPort(),
                            e2);
                      }
                      TimeUnit.MILLISECONDS.sleep(RECONNECT_INTERVAL);
                    }
                    if (!transport.isOpen()) {
                      throw new ConnectionFailException(
                          String.format(
                              "Connected error after %d retries, last address: %s:%d",
                              poolConfig.getMaxReconnects(),
                              serviceInfo.getHost(),
                              serviceInfo.getPort()),
                          e);
                    }
                  } else {
                    throw new ConnectionFailException(
                        String.format(
                            "Connected error, address: %s:%d",
                            serviceInfo.getHost(), serviceInfo.getPort()),
                        e);
                  }
                }

                ThriftClient<T> client =
                    new ThriftClient<>(clientFactory.createClient(transport), pool, serviceInfo);

                LOG.debug("created new thrift pool for url:{}", url);
                return client;
              }

              @Override
              public PooledObject<ThriftClient<T>> wrap(ThriftClient<T> obj) {
                return new DefaultPooledObject<>(obj);
              }

              @Override
              public void destroyObject(PooledObject<ThriftClient<T>> p) throws Exception {
                p.getObject().closeClient();
                super.destroyObject(p);
              }
            },
            poolConfig);
  }

  private TTransport getTransport(ServiceInfo serviceInfo) throws TTransportException {

    if (serviceInfo == null) {
      throw new NoBackendServiceException();
    }
    TConfiguration configuration = new TConfiguration();
    configuration.setMaxMessageSize(poolConfig.getMaxMessageSize());
    return new TFramedTransport(
        new TSocket(
            new TConfiguration(),
            serviceInfo.getHost(),
            serviceInfo.getPort(),
            poolConfig.getConnectTimeout(),
            poolConfig.getSocketTimeout()),
        poolConfig.getMaxMessageSize());
  }

  /**
   * Get a client's IFace from pool
   *
   * <ul>
   *   <li><span style="color:red">Important: Iface is totally generated by thrift, a
   *       ClassCastException will be thrown if assign not match!</span>
   *   <li><span style="color:red">Limitation: The return object can only used once.</span>
   * </ul>
   *
   * @return thrift service interface
   * @throws NoBackendServiceException if {@link PoolConfig#setAutoReconnect(boolean)} is set and no
   *     service can connect to
   * @throws ConnectionFailException if {@link PoolConfig#setAutoReconnect(boolean)} not set and
   *     connection fail
   * @throws IllegalStateException if call method on return object twice
   */
  @SuppressWarnings("unchecked")
  public <X> X iface() {
    ThriftClient<T> client = null;
    int attempt;
    for (attempt = 0; attempt < BORROW_ATTEMPTS; ++attempt) {
      try {
        client = pool.borrowObject();
        if (client.isDisConnected() || !pingFactory.ping(client.iface())) {
          if (attempt > 1) {
            // if attempt > 1, it means the server is maybe restarting, so we should wait a while
            LOG.warn("Maybe server is restarting, wait a while");
            Thread.sleep(RECONNECT_INTERVAL);
          }
          pool.invalidateObject(client);
          pool.clear();
        } else {
          break;
        }
      } catch (Exception e) {
        if (e instanceof ThriftException) {
          throw (ThriftException) e;
        }
        throw new ThriftException("Get client from pool failed", e);
      }
    }
    if (attempt >= BORROW_ATTEMPTS) {
      throw new ThriftException("Client can not connect.");
    }
    AtomicBoolean returnToPool = new AtomicBoolean(false);
    ThriftClient<T> finalClient = client;
    return (X)
        Proxy.newProxyInstance(
            this.getClass().getClassLoader(),
            finalClient.iface().getClass().getInterfaces(),
            (proxy, method, args) -> {
              if (returnToPool.get()) {
                throw new IllegalStateException("Client borrowed from pool can only used once!");
              }
              boolean success = false;
              try {
                Object result = method.invoke(finalClient.iface(), args);
                success = true;
                return result;
              } catch (InvocationTargetException e) {
                throw e.getTargetException();
              } finally {
                if (success) {
                  pool.returnObject(finalClient);
                } else {
                  finalClient.closeClient();
                  pool.invalidateObject(finalClient);
                }
                returnToPool.set(true);
              }
            });
  }
}
