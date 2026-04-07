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

{{/*Flink Optimizer Image Pull Secrets*/}}
{{- define "amoro.optimizer.container.flink.pull.secrets" -}}
  {{- if .Values.imagePullSecrets -}}
    {{- $secrets := .Values.imagePullSecrets -}}
    {{- $secretNames := list -}}
    {{- range $secrets }}
      {{- $secretNames = append $secretNames .name -}}
    {{- end }}
    {{- join ";" $secretNames -}}
  {{- end -}}
{{- end -}}

{{/*Spark Optimizer Image Pull Secrets*/}}
{{- define "amoro.optimizer.container.spark.pull.secrets" -}}
  {{- if .Values.imagePullSecrets -}}
    {{- $secrets := .Values.imagePullSecrets -}}
    {{- $secretNames := list -}}
    {{- range $secrets }}
      {{- $secretNames = append $secretNames .name -}}
    {{- end }}
    {{- join "," $secretNames -}}
  {{- end -}}
{{- end -}}

{{/*Kubernetes Optimizer Image Pull Secrets*/}}
{{- define "amoro.optimizer.container.kubernetes.pull.secrets" -}}
  {{- if .Values.imagePullSecrets -}}
    {{- $secrets := .Values.imagePullSecrets -}}
    {{- $secretNames := list -}}
    {{- range $secrets }}
      {{- $secretNames = append $secretNames .name -}}
    {{- end }}
    {{- join ";" $secretNames -}}
  {{- end -}}
{{- end -}}

{{/*Flink Optimizer Image Tag*/}}
{{- define "amoro.optimizer.container.flink.tag" -}}
{{- if .Values.optimizer.flink.image.tag -}}
  {{ .Values.optimizer.flink.image.tag }}
{{- else -}}
  {{ include "amoro.image.tag" . }}
{{- end -}}
{{- end -}}

{{/*Spark Optimizer Image Tag*/}}
{{- define "amoro.optimizer.container.spark.tag" -}}
{{- if .Values.optimizer.spark.image.tag -}}
  {{ .Values.optimizer.spark.image.tag }}
{{- else -}}
  {{ include "amoro.image.tag" . }}
{{- end -}}
{{- end -}}

{{/*Flink Optimizer Image repo*/}}
{{- define "amoro.optimizer.container.flink.image" }}
{{- .Values.optimizer.flink.image.repository }}:{{ include "amoro.optimizer.container.flink.tag" . }}
{{- end -}}

{{/*Spark Optimizer Image repo*/}}
{{- define "amoro.optimizer.container.spark.image" }}
{{- .Values.optimizer.spark.image.repository }}:{{ include "amoro.optimizer.container.spark.tag" . }}
{{- end -}}



{{- define "amoro.optimizer.container.flink" -}}
container-impl: org.apache.amoro.server.manager.FlinkOptimizerContainer
properties:
  target: kubernetes-application
  job-uri: {{ .Values.optimizer.flink.image.jobUri | quote }}
  ams-optimizing-uri: {{include "amoro.svc.optimizing.uri" . | quote}}
  flink-home: /opt/flink
  export.FLINK_HOME: /opt/flink
  flink-conf.kubernetes.container.image: {{ include "amoro.optimizer.container.flink.image" .  | quote }}
  flink-conf.kubernetes.container.image.pull-policy: {{ .Values.optimizer.flink.image.pullPolicy | quote }}
  flink-conf.kubernetes.service-account: {{ include "amoro.sa.name" . | quote }}
  {{- if .Values.imagePullSecrets }}
  flink-conf.kubernetes.container.image.pull-secrets: {{ include "amoro.optimizer.container.flink.pull.secrets" . }}
  {{- end }}
  {{- with .Values.optimizer.flink.properties -}}
    {{- toYaml . | nindent 2 }}
  {{- end -}}
{{- end -}}

{{- define "amoro.optimizer.container.spark" -}}
container-impl: org.apache.amoro.server.manager.SparkOptimizerContainer
properties:
  job-uri: {{ .Values.optimizer.spark.image.jobUri | quote }}
  ams-optimizing-uri: {{include "amoro.svc.optimizing.uri" . | quote}}
  spark-home: /opt/spark
  export.SPARK_HOME: /opt/spark
  spark-conf.spark.kubernetes.container.image: {{ include "amoro.optimizer.container.spark.image" .  | quote }}
  spark-conf.spark.kubernetes.container.image.pullPolicy: {{ .Values.optimizer.spark.image.pullPolicy | quote }}
  spark-conf.spark.kubernetes.authenticate.driver.serviceAccountName: {{ include "amoro.sa.name" . | quote }}
  {{- if .Values.imagePullSecrets }}
  spark-conf.spark.kubernetes.container.image.pullSecrets: {{ include "amoro.optimizer.container.spark.pull.secrets" . }}
  {{- end }}
  {{- with .Values.optimizer.spark.properties -}}
    {{- toYaml . | nindent 2 }}
  {{- end -}}
{{- end -}}


{{- define "amoro.optimizer.container.local" -}}
container-impl: org.apache.amoro.server.manager.LocalOptimizerContainer
properties:
  export.JAVA_HOME: "/opt/java"   # JDK environment
  {{- with .Values.optimizer.local.properties -}}
    {{- toYaml . | nindent 2 }}
  {{- end -}}
{{- end -}}

{{- define "amoro.optimizer.container.kubernetes" -}}
container-impl: org.apache.amoro.server.manager.KubernetesOptimizerContainer
properties:
  ams-optimizing-uri: {{include "amoro.svc.optimizing.uri" . | quote}}
  {{- with .Values.optimizer.kubernetes.properties -}}
    {{- toYaml . | nindent 2 }}
  {{- end -}}
  {{- if .Values.imagePullSecrets }}
  imagePullSecrets: {{ include "amoro.optimizer.container.kubernetes.pull.secrets" . }}
  {{- end }}
{{- end -}}

