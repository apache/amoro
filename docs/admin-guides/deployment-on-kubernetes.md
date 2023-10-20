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
- An active Kubernetes cluster
- Reading About Deploy AMS on Kubernetes
- Kubectl
- KubeConfig of the target cluster
- Helm3+

## Build AMS Docker Image

You can find the official docker image at [Amoro Docker Hub](https://hub.docker.com/u/arctic163).
If you want to package locally, you can find the build.sh script in the docker folder of the project and pass the following command:

```shell
./docker/build.sh amoro
```

## Install

When you are ready, you can use the following helm command to start

```shell
helm install {{ .Release.Name }} ${AMORO_HOME}/charts/amoro
```
After successful installation, you can access webui through the following command
```shell
kubectl port-forward services/{{ .Release.Name }}-amoro-rest 1630:1630
```
Open browser to go web: http://loclhost:1630

## Access logs

Then, use pod name to get logs:
```shell
kubectl logs {amoro-pod-name}
```

## Uninstall

```shell
helm uninstall amoro
```

## Use Ingress Access WebUI

Ingress is not enabled by default. If you need to enable it, confirm whether your Kubernetes cluster has installed the relevant components.

```yaml
ingress:
  enabled: true
  ingressClassName: "nginx"
  hostname: minikube.amoro.com
```

## Config

The default config is used for ${AMORO_HOME}/conf/conf.yaml file.

AMS default is to use Derby database for storage.When the pod is destroyed, the data will also disappear. We recommend using another database for persistence.

If you need to configure the database, you can modify **amoroConf.database** properties

