package com.netease.arctic.hive;

import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.iceberg.ClientPool;
import org.apache.thrift.TException;

public interface HMSClient extends ClientPool<HiveMetaStoreClient, TException> {
}
