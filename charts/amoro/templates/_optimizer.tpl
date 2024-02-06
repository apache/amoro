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

{{/*Flink Optimizer Image Tag*/}}
{{- define "amoro.optimizer.container.flink.tag" -}}
{{- if .Values.optimizer.flink.image.tag -}}
  {{ .Values.optimizer.flink.image.tag }}
{{- else -}}
  {{ include "amoro.image.tag" . }}
{{- end -}}
{{- end -}}

{{/*Flink Optimizer Image repo*/}}
{{- define "amoro.optimizer.container.flink.image" }}
{{- .Values.optimizer.flink.image.repository }}:{{ include "amoro.optimizer.container.flink.tag" . }}
{{- end -}}



{{- define "amoro.optimizer.container.flink" -}}
container-impl: com.netease.arctic.server.manager.FlinkOptimizerContainer
properties:
  target: kubernetes-application
  job-uri: {{ .Values.optimizer.flink.image.jobUri | quote }}
  ams-optimizing-uri: {{include "amoro.svc.optimizing.uri" . | quote}}
  flink-home: /opt/flink
  export.FLINK_HOME: /opt/flink
  flink-conf.kubernetes.container.image: {{ include "amoro.optimizer.container.flink.image" .  | quote }}
  flink-conf.kubernetes.service-account: {{ include "amoro.sa.name" . | quote }}
  {{- with .Values.optimizer.flink.properties -}}
    {{- toYaml . | nindent 2 }}
  {{- end -}}
{{- end -}}


{{- define "amoro.optimizer.container.local" -}}
container-impl: com.netease.arctic.server.manager.LocalOptimizerContainer
properties:
  export.JAVA_HOME: "/opt/java"   # JDK environment
  {{- with .Values.optimizer.local.properties -}}
    {{- toYaml . | nindent 2 }}
  {{- end -}}
{{- end -}}

