{{/*
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/}}

{{- define "amoro.pod.initContainer.flink" -}}
- name: install-flink
  image: {{ include "amoro.optimizer.container.flink.image" .}}
  command: ["cp", "/opt/flink/.", "/opt/flink_install/", "-R"]
  volumeMounts:
    - name: flink-install
      mountPath: /opt/flink_install
{{- end -}}

{{- define "amoro.pod.initContainer.spark" -}}
- name: install-spark
  image: {{ include "amoro.optimizer.container.spark.image" .}}
  command: ["cp", "/opt/spark/.", "/opt/spark_install/", "-R"]
  volumeMounts:
    - name: spark-install
      mountPath: /opt/spark_install
{{- end -}}


{{- define "amoro.pod.initContainers" -}}
{{- if .Values.optimizer.flink.enabled -}}
{{- include "amoro.pod.initContainer.flink" . }}
{{/* initialized flink env */}}
{{- end -}}
{{- if .Values.optimizer.spark.enabled -}}
{{- include "amoro.pod.initContainer.spark" . }}
{{/* initialized spark env */}}
{{- end -}}
{{- end }}


{{- define "amoro.pod.container.mounts" -}}
- name: logs
  mountPath: {{ include "amoro.home" . }}/logs
- name: conf
  mountPath: {{ include "amoro.home" . }}/conf/config.yaml
  readOnly: true
  subPath: "config.yaml"
{{- if or .Values.amoroConf.log4j2 }}
{{/* log4j2.yaml from config-map*/}}
- name: conf
  mountPath: {{ include "amoro.home" . }}/conf/log4j2.xml
  readOnly: true
  subPath: "log4j2.xml"
{{- end }}
{{- if or .Values.jvmOptions }}
- name: conf
  mountPath: {{ include "amoro.home" . }}/conf/jvm.properties
  readOnly: true
  subPath: "jvm.properties"
{{- end -}}
{{- /* metric-reporters.yaml from config-map*/ -}}
{{- if or .Values.plugin.metricReporters }}
- name: conf
  mountPath: {{ include "amoro.home" . }}/conf/plugins/metric-reporters.yaml
  readOnly: true
  subPath: "metric-reporters.yaml"
{{- end -}}
{{- /* flink install dir. if flink optimizer container enabled.
flink distribution package will be installed to here*/ -}}
{{- if .Values.optimizer.flink.enabled }}
- name: flink-install
  mountPath: /opt/flink
{{- end -}}
{{- /* spark install dir. if spark optimizer container enabled.
spark distribution package will be installed to here*/ -}}
{{- if .Values.optimizer.spark.enabled }}
- name: spark-install
  mountPath: /opt/spark
{{- end -}}
{{- end -}}
{{- /* define amoro.pod.container.mounts end */ -}}


{{/* defined volumes for pod */}}
{{- define "amoro.pod.volumes" -}}
- name: conf
  configMap:
    name: {{ include "common.names.fullname" . }}
- name: logs
  emptyDir: {}
{{- /* volume for flink distribution package install from init container. */ -}}
{{- if .Values.optimizer.flink.enabled }}
- name: flink-install
  emptyDir: {}
{{- end -}}
{{- /* volume for spark distribution package install from init container. */ -}}
{{- if .Values.optimizer.spark.enabled }}
- name: spark-install
  emptyDir: {}
{{- end -}}
{{- end -}}
{{- /* define "amoro.pod.volumes" end */ -}}

{{- /* define ports for each pod */ -}}
{{- define "amoro.pod.container.ports" -}}
- name: rest
  containerPort: {{ .Values.server.rest.port }}
- name: table
  containerPort: {{ .Values.server.table.port }}
- name: optimizing
  containerPort: {{ .Values.server.optimizing.port }}
{{- if .Values.plugin.metricReporters }}
{{- if .Values.plugin.metricReporters.prometheusExporter.enabled }}
- name: prometheus
  containerPort: {{ .Values.plugin.metricReporters.prometheusExporter.properties.port }}
{{- end -}}
{{- end -}}
{{- end -}}
{{- /* define amoro.pod.container.ports end */ -}}