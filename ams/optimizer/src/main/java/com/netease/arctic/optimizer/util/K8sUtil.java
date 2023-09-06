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
package com.netease.arctic.optimizer.util;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.StatusDetails;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.JobStatus;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class K8sUtil {
  public static void runJob(String namespace, String name, KubernetesClient client, String image, String cmd, Logger logger) {
    // delete job
    logger.info("delete job if need ,job name: " + name);
    List<StatusDetails> statusDetailsList = client.batch().v1().jobs()
        .inNamespace(namespace)
        .withName(name)
        .delete();

    // Polling loop to wait for deletion
    long timeout = 300; // Timeout in seconds
    long startTime = System.currentTimeMillis();

    waitForDeleteJob(namespace, name, client, timeout, startTime, logger);

    submitJob(namespace, name, client, image, cmd);


    // Polling loop to wait for deletion
    long waitPodTimeout = 300; // Timeout in seconds
    long waitPodStartTime = System.currentTimeMillis();

    String podName = "";
    podName = waitForCreatePodOfJob(namespace, name, client, logger, podName, waitPodStartTime, waitPodTimeout);
    logger.info("Pod name: " + podName);

    CountDownLatch jobCompletionLatch = new CountDownLatch(1);

    AtomicBoolean isJobEndSuccess = new AtomicBoolean(false);
    Watcher<Job> watcher = new Watcher<Job>() {
      @Override
      public void eventReceived(Action action, Job job) {

        if (action == Action.ADDED || action == Action.MODIFIED) {
          JobStatus status = job.getStatus();
          if (status != null) {
            boolean isJobSuccessful = status.getSucceeded() != null && status.getSucceeded() > 0;
            boolean isJobFailed = status.getFailed() != null && status.getFailed() > 0;

            if (isJobSuccessful) {
              isJobEndSuccess.set(true);
              jobCompletionLatch.countDown(); // Decrement the latch count
            } else if (isJobFailed) {
              isJobEndSuccess.set(false);
              jobCompletionLatch.countDown(); // Decrement the latch count
            }
          }
        }
      }

      @Override
      public void onClose(WatcherException cause) {
        logger.info("Watcher closed");
        if (cause != null) {
          logger.error(cause.getMessage(), cause);
        }
      }


    };

    Watch watch = client.batch().v1().jobs()
        .inNamespace(namespace)
        .withName(name)
        .watch(watcher);


    // Print pod logs
    try (LogWatch logWatch = client.pods()
        .inNamespace(namespace)
        .withName(podName)
        .watchLog()) {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(logWatch.getOutput()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          logger.info("p> " + line);  // You can replace this with your desired logging mechanism
        }
      } catch (IOException e) {
        logger.error(e.getMessage(), e);
      } finally {
        logWatch.close();
      }

      // Wait for the job to complete
      logger.info("Waiting  for job to complete...");
      jobCompletionLatch.await();

    } catch (InterruptedException e) {
      logger.error(e.getMessage(), e);
    } finally {
      watch.close();
    }

    boolean flag = isJobEndSuccess.get();
    logger.info("Job completed with success status: " + flag);
    if (!flag) {
      throw new RuntimeException("Job failed.");
    }
  }

  private static String waitForCreatePodOfJob(String namespace, String jobName, KubernetesClient client, Logger logger, String podName, long waitPodStartTime, long waitPodTimeout) {
    // 循环等待创建pod成功
    while (true) {
      // 需要考虑，有可能pod还没创建出来
      //  正在创建也不行 {"kind":"Status","apiVersion":"v1","metadata":{},"status":"Failure","message":"container \"init\" in pod \"init-flinkdir-hdfs-6c9r5\" is waiting to start: ContainerCreating","reason":"BadRequest","code":400}
      // Get the pod name associated with the job
      List<Pod> pods = client.pods()
          .inNamespace(namespace)
          .withLabel("job-name", jobName)
          .list().getItems();


      if (!pods.isEmpty()) {
        Pod pod = pods.get(0);
        podName = pod.getMetadata().getName();
        // 检查pod是否正在创建
        String phase = pod.getStatus().getPhase();
        if (phase.equals("Pending")) {
          logger.info("Pod {} is pending, waiting for it to be running...", podName);
        } else if (phase.equals("Running")) {
          logger.info("Pod {} is running.", podName);
          break;
        }
      }


      // Check timeout
      long elapsedTime = System.currentTimeMillis() - waitPodStartTime;
      if (TimeUnit.MILLISECONDS.toSeconds(elapsedTime) > waitPodTimeout) {
        throw new RuntimeException("Timeout reached. Job Pod create failed.");
      }

      // Polling interval
      try {
        TimeUnit.SECONDS.sleep(2);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    return podName;
  }

  private static void waitForDeleteJob(String namespace, String jobName, KubernetesClient client, long timeout, long startTime, Logger logger) {


    // 循环等待删除成功
    while (true) {
      Job deletedJob = client.batch().v1().jobs()
          .inNamespace(namespace)
          .withName(jobName)
          .get();

      List<Pod> podList = client.pods()
          .inNamespace(namespace)
          .withLabel("job-name", jobName)
          .list().getItems();

      if (deletedJob == null && podList.isEmpty()) {
        logger.info("Job {} has been deleted.", jobName);
        return;
      }


      // Check timeout
      long elapsedTime = System.currentTimeMillis() - startTime;
      if (TimeUnit.MILLISECONDS.toSeconds(elapsedTime) > timeout) {
        throw new RuntimeException("Timeout reached. Job deletion failed.");
      }

      // Polling interval
      try {
        TimeUnit.SECONDS.sleep(2);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static void submitJob(String namespace, String name, KubernetesClient client, String image, String cmd) {


    Container container = new ContainerBuilder()
        .withName("init")
        .withImage(image)
        .withCommand("sh", "-c", cmd)
        .build();

    Job job = new JobBuilder()
        .withNewMetadata()
        .withName(name)
        .endMetadata()
        .withNewSpec()
        .withBackoffLimit(0)
        .withNewTemplate()
        .withNewSpec()
        .withContainers(container).withHostNetwork(true)
        .withRestartPolicy("Never")
        .endSpec()
        .endTemplate()
        .endSpec()
        .build();

    client.batch().v1().jobs()
        .inNamespace(namespace)
        .resource(job).create();
  }
}
