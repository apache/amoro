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
{{- end }}
