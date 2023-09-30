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
  command: ["cp", "/opt/flink/*", "/opt/flink_install/", "-R"]
  volumeMounts:
    - name: flinkInstall
      mountPath: /opt/flink_install
{{- end -}}


{{- define "amoro.pod.initContainers" -}}
{{- if .Values.optimizer.flink.enabled -}}
{{- include "amoro.pod.initContainer.flink" .}}
{{- end -}}
{{- end }}



{{- define "amoro.pod.container.mounts" }}
{{- /* config.yaml from config-map*/ -}}
- name: conf
  mountPath: {{ .Values.amoroDir }}/amoro-{{ .Chart.AppVersion }}/conf/config.yaml
  readOnly: true
  subPath: "config.yaml"
  {{- with .Values.volumeMounts }}
    {{- tpl (toYaml .) $ | nindent 12 }}
  {{- end }}
{{- if or .Values.amoroConf.log4j2 }}
{{- /* log4j2.yaml from config-map*/ -}}
- name: conf
  mountPath: {{ .Values.amoroDir }}/amoro-{{ .Chart.AppVersion }}/conf/log4j2.xml
  readOnly: true
  subPath: "log4j2.xml"
  {{- with .Values.volumeMounts }}
    {{- tpl (toYaml .) $ | nindent 12 }}
  {{- end }}
{{- end }}
{{- /* flink install dir. if flink optimizer container enabled.
flink distribution package will be installed to here*/ -}}
{{- if .Values.optimizer.flink.enabled }}
- name: flinkInstall
  mountPath: /opt/flink
{{- end -}}
{{- end -}}
{{- /* define amoro.pod.container.mounts end */ -}}

{{/* defined volumes for pod */}}
{{- define "amoro.pod.volumes" -}}
- name: conf
  configMap:
    name: {{ include "common.names.fullname" . }}

{{- /* volume for flink distribution package install from init container. */ -}}
{{- if .Values.optimizer.flink.enabled }}
- name: flinkInstall
  emptyDir: {}
{{- end -}}
{{- end -}}
{{- /* define "amoro.pod.volumes" end */ -}}
