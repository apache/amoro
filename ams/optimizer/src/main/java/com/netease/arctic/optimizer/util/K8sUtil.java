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

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.*;
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
  public static void runJob(String namespace, String name, KubernetesClient client, String image, String cmd, Logger logger,String memory ,String cpu) {

    submitJob(namespace, name, client, image, cmd,memory,cpu);

    // Polling loop to wait for deletion
    long waitPodTimeout = 300; // Timeout in seconds
    long waitPodStartTime = System.currentTimeMillis();

    String podName = "";
    podName = waitForCreatePodOfJob(namespace, name, client, logger, podName, waitPodStartTime, waitPodTimeout);
    logger.info("Pod name: " + podName);

  }

  private static String waitForCreatePodOfJob(String namespace, String jobName, KubernetesClient client, Logger logger, String podName, long waitPodStartTime, long waitPodTimeout) {
    while (true) {
      // Get the pod name associated with the job
      List<Pod> pods = client.pods()
          .inNamespace(namespace)
          .withLabel("job-name", jobName)
          .list().getItems();

      if (!pods.isEmpty()) {
        Pod pod = pods.get(0);
        podName = pod.getMetadata().getName();
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

  public static void waitForDeleteJob(String namespace, String jobName, KubernetesClient client, Logger logger) {
    long timeout = 300; // Timeout in seconds
    long startTime = System.currentTimeMillis();
    waitForDeleteJob(namespace, jobName, client, timeout, startTime, logger);
  }

  private static void waitForDeleteJob(String namespace, String jobName, KubernetesClient client, long timeout, long startTime, Logger logger) {

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

  private static void submitJob(String namespace, String name, KubernetesClient client, String image, String cmd,String memory ,String cpu) {

    Container container = new ContainerBuilder()
        .withName("init")
        .withImage(image)
        .withCommand("sh", "-c", cmd)
        .withResources(new ResourceRequirementsBuilder()
            .withLimits(ImmutableMap.of("memory", new Quantity(memory), "cpu", new Quantity(cpu)))
            .withRequests(ImmutableMap.of("memory", new Quantity(memory), "cpu", new Quantity(cpu)))
            .build())
        .build();

    Job job = new JobBuilder()
        .withNewMetadata()
        .withName(name)
        .endMetadata()
        .withNewSpec()
        .withBackoffLimit(0)
        .withNewTemplate()
        .withNewSpec()
        .withContainers(container)
        .withHostNetwork(true)
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
