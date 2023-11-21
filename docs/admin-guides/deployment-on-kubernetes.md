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
# Deploy AMS On Kubernetes

## Requirements

If you want to deploy AMS on Kubernetes, you’d better get a sense of the following things.

- Use AMS official docker image or build AMS docker image
- [An active Kubernetes cluster](https://kubernetes.io/docs/setup/) 
- [Kubectl](https://kubernetes.io/docs/tasks/tools/#kubectl) 
- [Helm3+](https://helm.sh/docs/intro/quickstart/)

## Amoro Official Docker Image

You can find the official docker image at [Amoro Docker Hub](https://hub.docker.com/u/arctic163).

The following are images that can be used in a production environment.

**arctic163/amoro**

This is an image built based on the Amoro binary distribution package for deploying AMS.

**arctic163/optimizer-flink**

This is an image built based on the official version of Flink for deploying the Flink optimizer.

## Build AMS Docker Image

If you want to build images locally, you can find the `build.sh` script in the docker folder of the project and pass the following command:

```shell
./docker/build.sh amoro
```

or build the `optimizer-flink` image by:

```shell
./docker/build.sh optimizer-flink --flink-version <flink-version>
```


## Get Helm Charts

You can obtain the latest official release chart by adding the official Helm repository.

```shell
$ helm repo add amoro https://netease.github.io/amoro/charts
$ helm search repo amoro 
NAME           CHART VERSION    APP VERSION        DESCRIPTION           
amoro/amoro    0.1.0            0.6.0              A Helm chart for Amoro 

$ helm pull amoro/amoro 
$ tar zxvf amoro-*.tgz
```

Alternatively, you can find the latest charts directly from the Github source code.

```shell
$ git clone https://github.com/NetEase/amoro.git
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

Open browser to go web: http://loclhost:1630

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

AMS default is to use Derby database for storage. When the pod is destroyed, the data will also disappear.
In production environments, we recommend using MySQL as the storage for system data.

```yaml
amoroConf: 
  database:
    type: mysql
    driver: com.mysql.cj.jdbc.Driver
    url: <jdbc-uri>
    username: <mysql-user>
    password: <mysql-password>
```


### Configure the Images

Helm charts deploy images by default using the latest tag. 
If you need to modify the image address, such as using a private repository or building your own image


```yaml
image:
  repository: <your repository>
  pullPolicy: IfNotPresent
  tag: <your tag>
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
      repository: arctic163/optimizer-flink
      ## the image tag, if not set, the default value is the same with amoro image tag.
      tag: ~
      ## the location of flink optimizer jar in image.
      jobUri: "local:///opt/flink/usrlib/optimizer-job.jar"
    properties: {
      "flink-conf.taskmanager.memory.managed.size": "32mb",
      "flink-conf.taskmanager.memory.netwrok.max": "32mb",
      "flink-conf.taskmanager.memory.netwrok.nin": "32mb"
    }
```


### Configure the RBAC

By default, Helm Chart creates a service account, role, and role bind for Amaro deploy. 
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