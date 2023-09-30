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
{{include "common.names.fullname" . }}.{{.Release.Namespace}}.svc.{{.Values.clusterDomain}}
{{- end -}}

{{- define "amoro.svc.optimizing.uri" -}}
thrift://{{ include "amoro.svc.optimizing.fullname" .}}:{{ .Values.server.optimizing.port }}
{{- end -}}