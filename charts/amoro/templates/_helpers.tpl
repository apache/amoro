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

{{/*
Expand the name of the chart.
*/}}
{{- define "amoro.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "amoro.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*AmoroHome*/}}
{{- define "amoro.home" -}}
{{ .Values.amoroHome | default "/usr/local/amoro" }}
{{- end -}}
{{/* Amoro Home end */}}

{{/*
Common labels
*/}}
{{- define "amoro.labels" -}}
helm.sh/chart: {{ include "amoro.chart" . }}
{{ include "amoro.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "amoro.selectorLabels" -}}
app.kubernetes.io/name: {{ include "amoro.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}

{{/*Amoro Image Tag*/}}
{{- define "amoro.image.tag" -}}
{{ .Values.image.tag | default .Chart.AppVersion }}
{{- end -}}

{{- define "amoro.image" -}}
{{ .Values.image.repository }}:{{include "amoro.image.tag" .}}
{{- end -}}

{{- define "amoro.svc.optimizing.fullname" -}}
{{include "common.names.fullname" . }}-optimizing.{{.Release.Namespace}}.svc.{{.Values.clusterDomain}}
{{- end -}}

{{- define "amoro.svc.optimizing.uri" -}}
thrift://{{ include "amoro.svc.optimizing.fullname" .}}:{{ .Values.server.optimizing.port }}
{{- end -}}


{{- define "amoro.sa.name" -}}
{{ if .Values.serviceAccount.create }}
{{- include "common.names.fullname" . -}}
{{- else -}}
{{- .Values.serviceAccount.name -}}
{{ end }}
{{- end -}}