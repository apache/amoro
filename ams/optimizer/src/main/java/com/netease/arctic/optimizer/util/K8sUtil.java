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
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class K8sUtil {
  public static void runDeployment(String namespace, String name, KubernetesClient client, String image, String cmd, Logger logger, String memory , String cpu) {

    submitJob(namespace, name, client, image, cmd,memory,cpu);

    // Polling loop to wait for deletion
    long waitPodTimeout = 300; // Timeout in seconds
    long waitPodStartTime = System.currentTimeMillis();

    String podName = "";
    podName = waitForCreatePodOfDeployment(namespace, name, client, logger, podName, waitPodStartTime, waitPodTimeout);
    logger.info("Pod name: " + podName);
  }

  public static void deleteDeployment(String namespace, String name, KubernetesClient client, Logger logger) {
    client.apps().deployments()
        .inNamespace(namespace)
        .withName(name)
        .delete();
    waitForDeleteDeployment(namespace, name, client, logger);
  }

  private static String waitForCreatePodOfDeployment(String namespace, String name, KubernetesClient client, Logger logger, String podName, long waitPodStartTime, long waitPodTimeout) {
    while (true) {
      // Get the pod name associated with the job
      List<Pod> pods = client.pods()
          .inNamespace(namespace)
          .withLabel("app", name)
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
        throw new RuntimeException("Timeout reached. Deployment Pod create failed.");
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

  public static void waitForDeleteDeployment(String namespace, String name, KubernetesClient client, Logger logger) {
    long timeout = 300; // Timeout in seconds
    long startTime = System.currentTimeMillis();
    waitForDeleteDeployment(namespace, name, client, timeout, startTime, logger);
  }

  private static void waitForDeleteDeployment(String namespace, String name, KubernetesClient client, long timeout, long startTime, Logger logger) {

    while (true) {
      Deployment deletedDeployment = client.apps().deployments()
          .inNamespace(namespace)
          .withName(name)
          .get();

      List<Pod> podList = client.pods()
          .inNamespace(namespace)
          .withLabel("app", name)
          .list().getItems();

      if (deletedDeployment == null && podList.isEmpty()) {
        logger.info("Deployment {} has been deleted.", name);
        return;
      }

      // Check timeout
      long elapsedTime = System.currentTimeMillis() - startTime;
      if (TimeUnit.MILLISECONDS.toSeconds(elapsedTime) > timeout) {
        throw new RuntimeException("Timeout reached. Deployment deletion failed.");
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

    Deployment deployment = new DeploymentBuilder()
        .withNewMetadata()
        .withName(name)
        .endMetadata()
        .withNewSpec()
        .withReplicas(1)
        .withNewTemplate()
        .withNewMetadata()
        .addToLabels("app", name)
        .endMetadata()
        .withNewSpec()
        .addNewContainer()
        .withName("optimizer")
        .withImage(image)
        .withCommand("sh", "-c", cmd)
        .withResources(new ResourceRequirementsBuilder()
            .withLimits(ImmutableMap.of("memory", new Quantity(memory), "cpu", new Quantity(cpu)))
            .withRequests(ImmutableMap.of("memory", new Quantity(memory), "cpu", new Quantity(cpu)))
            .build())
        .endContainer()
        .endSpec()
        .endTemplate()
        .withNewSelector()
        .addToMatchLabels("app", name)
        .endSelector()
        .endSpec()
        .build();

    client.apps().deployments().inNamespace(namespace).createOrReplace(deployment);
  }
}
