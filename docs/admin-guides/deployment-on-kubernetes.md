---
title: "Deployment On Kubernetes"
url: deployment-on-kubernetes
aliases:
    - "admin-guides/deployment-on-kubernetes"
menu:
    main:
        parent: Admin Guides
        weight: 150
---
<!--
 - Licensed to the Apache Software Foundation (ASF) under one or more
 - contributor license agreements.  See the NOTICE file distributed with
 - this work for additional information regarding copyright ownership.
 - The ASF licenses this file to You under the Apache License, Version 2.0
 - (the "License"); you may not use this file except in compliance with
 - the License.  You may obtain a copy of the License at
 -
 -   http://www.apache.org/licenses/LICENSE-2.0
 -
 - Unless required by applicable law or agreed to in writing, software
 - distributed under the License is distributed on an "AS IS" BASIS,
 - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 - See the License for the specific language governing permissions and
 - limitations under the License.
 -->
# Deploy AMS On Kubernetes

## Requirements

If you want to deploy AMS on Kubernetes, you’d better get a sense of the following things.

- Use AMS official docker image or build AMS docker image
- [An active Kubernetes cluster](https://kubernetes.io/docs/setup/) 
- [Kubectl](https://kubernetes.io/docs/tasks/tools/#kubectl) 
- [Helm3+](https://helm.sh/docs/intro/quickstart/)

## Amoro Official Docker Image

You can find the official docker image at [Amoro Docker Hub](https://hub.docker.com/u/apache).

The following are images that can be used in a production environment.

**apache/amoro**

This is an image built based on the Amoro binary distribution package for deploying AMS.

**apache/amoro-flink-optimizer**

This is an image built based on the official version of Flink for deploying the Flink optimizer.

**apache/amoro-spark-optimizer**

This is an image built based on the official version of Spark for deploying the Spark optimizer.

## Build AMS Docker Image

If you want to build images locally, you can find the `build.sh` script in the docker folder of the project and pass the following command:

```shell
./docker/build.sh amoro
```

or build the `amoro-flink-optimizer` image by:

```shell
./docker/build.sh amoro-flink-optimizer --flink-version <flink-version>
```

or build the `amoro-spark-optimizer` image by:

```shell
./docker/build.sh amoro-spark-optimizer --spark-version <spark-version>
```

## Get Helm Charts
You can find the latest charts directly from the Github source code.

```shell
$ git clone https://github.com/apache/amoro.git
$ cd amoro/charts
$ helm dependency build ./amoro
```

## Install

When you are ready, you can use the following helm command to start

```shell
$ helm install <deployment-name> ./amoro 
```

After successful installation, you can access WebUI through the following command.

```shell
$ kubectl port-forward services/<deployment-name>-amoro-rest 1630:1630
```

Open browser to go web: http://localhost:1630

## Access logs

Then, use pod name to get logs:

```shell
$ kubectl get pod
$ kubectl logs {amoro-pod-name}
```

## Uninstall

```shell
$ helm uninstall <deployment-name>
```


## Configuring Helm application.

Helm uses `<chart>/values.yaml` files as configuration files, and you can also copy this file for separate maintenance.

```shell
$ cp amoro/values.yaml my-values.yaml
$ vim my-values.yaml
```

And deploy Helm applications using independent configuration files.

```shell
$ helm install <deployment-name> ./amoro -f my-values.yaml
```


### Enable Ingress

Ingress is not enabled by default. In production environments, it is recommended to enable Ingress to access the AMS Dashboard from outside the cluster.

```yaml
ingress:
  enabled: true
  ingressClassName: "nginx"
  hostname: minikube.amoro.com
```

### Configure the database.

AMS uses embedded [Apache Derby](https://db.apache.org/derby/) as its backend storage by default.
In production environments, we recommend using a RDBMS(Relational Database Management System) with higher availability guarantees as the storage for system data, you can ref to [Database Configuration](/deployment/#configure-system-database) for more detail.

```yaml
amoroConf: 
  database:
    type: ${your_database_type}
    driver: ${your_database_driver}
    url: ${your_jdbc_url}
    username: ${your_username}
    password: ${your_password}
```


### Configure the Images

Helm charts deploy images by default using the latest tag. 
If you need to modify the image address, such as using a private repository or building your own image


```yaml
image:
  repository: <your repository>
  pullPolicy: IfNotPresent
  tag: <your tag>
imagePullSecrets: [ ]
```

### Configure the Flink Optimizer Container

By default, the Flink Optimizer Container is enabled. 
You can modify the container configuration by changing the `optimizer.flink` section.

```yaml
optimizer: 
  flink: 
    enabled: true
    ## container name, default is flink
    name: ~ 
    image:
      ## the image repository
      repository: apache/amoro-flink-optimizer
      ## the image tag, if not set, the default value is the same with amoro image tag.
      tag: ~
      pullPolicy: IfNotPresent
      ## the location of flink optimizer jar in image.
      jobUri: "local:///opt/flink/usrlib/optimizer-job.jar"
    properties: {
      "flink-conf.taskmanager.memory.managed.size": "32mb",
      "flink-conf.taskmanager.memory.network.max": "32mb",
      "flink-conf.taskmanager.memory.network.min": "32mb"
    }
```
### Configure the Kubernetes Optimizer Container

By default, the Kubernetes Optimizer Container is enabled.
You can modify the container configuration by changing the `optimizer.Kubernetes` section.

```yaml
optimizer:
  kubernetes:
    # enable the kubernetes optimizer container
    enabled: true
    properties:
      namespace: "default"
      kube-config-path: "~/.kube/config"
      image: "apache/amoro:latest"
      pullPolicy: "IfNotPresent"
      # configure additional parameters by using the extra. prefix
      # extra.jvm.heap.ratio: "0.8"
```

To use PodTemplate, you need to copy and paste the following into the `kubernetes.properties`.

This is the default podTemplate, and when the user doesn't specify any additional parameters, the default is to use the template's parameters

Therefore, there will be a priority issue that needs to be elaborated: _Resource(WebUi) > Independent User Profile Configuration > PodTemplate_

```yaml
podTemplate: |
  apiVersion: apps/v1
  kind: PodTemplate
  metadata:
    name: <NAME_PREFIX><resourceId>
  template:
    metadata:
      labels:
        app: <NAME_PREFIX><resourceId>
        AmoroOptimizerGroup: <groupName>
        AmoroResourceId: <resourceId>
    spec:
      containers:
        - name: optimizer
          image: apache/amoro:0.6
          imagePullPolicy: IfNotPresent
          command: [ "sh", "-c", "echo 'Hello, World!'" ]
          resources:
            limits:
              memory: 2048Mi
              cpu: 2
            requests:
              memory: 2048Mi
              cpu: 2

```


### Configure the RBAC

By default, Helm Chart creates a service account, role, and role bind for Amoro deploy. 
You can also modify this configuration to use an existing account.

```yaml
# ServiceAccount of Amoro to schedule optimizer.
serviceAccount:
  # Specifies whether a service account should be created or using an existed account
  create: true
  # Annotations to add to the service account
  annotations: { }
  # Specifies ServiceAccount name to be used if `create: false`
  name: ~
  # if `serviceAccount.create` == true. role and role-bind will be created
  rbac:
    # create a role/role-bind if automatically create service account
    create: true
    # create a cluster-role and cluster-role-bind if cluster := true
    cluster: false
```

Notes:

- If `serviceAccount.create` is false, you must provide a `serviceAccount.name` and create the `serviceAccount` beforehand.
- If `serviceAccount.rbac.create` is false, the role and role-bind will not be created automatically.
- You can set `serviceAccount.rbac.cluster` to true, which will create a `cluster-role` and `cluster-role-bind` instead of a `role` and `role-bind`.

By default, the `serviceAccount` will be used to create the flink-optimizer. 
Therefore, if you need to schedule the flink-optimizer across namespaces, 
please create a `cluster-role` or use your own created `serviceAccount`.
