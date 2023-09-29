
{{/*Flink Optimizer Image Tag*/}}
{{- define "amoro.optimizer.container.flink.tag" -}}
{{- if .container.tag -}}
  {{ .container.tag }}
{{- else -}}
  {{ include "amoro.image.tag" .context }}
{{- end -}}
{{- end -}}

{{/*Flink Optimizer Image repo*/}}
{{- define "amoro.optimizer.container.flink.image" }}
{{- .container.image.repository }}:{{ include "amoro.optimizer.container.flink.tag" . }}
{{- end -}}



{{- define "amoro.optimizer.container.flink" -}}
container-impl: com.netease.arctic.optimizer.FlinkOptimizerContainer
properties:
  target: kubernetes-application
  job-uri: {{ .container.image.jobUri | quote }}
  ams-optimizing-uri: {{include "amoro.svc.optimizing.uri" .context }}
  flink-conf.kubernetes.container.image: {{ include "amoro.optimizer.container.flink.image" .  | quote }}
  {{- with .container.properties -}}
    {{- toYaml . | nindent 2 }}
  {{- end -}}
{{- end -}}


{{- define "amoro.optimizer.container.local" -}}
container-impl: com.netease.arctic.optimizer.LocalOptimizerContainer
properties:
  {{- with .container.properties -}}
    {{- toYaml . | nindent 2 }}
  {{- end -}}
{{- end -}}


{{- define "amoro.optimizer.container.custom" -}}
container-impl: {{ .impl }}
properties:
  {{- with .container.properties -}}
    {{- toYaml . | nindent 2 }}
  {{- end -}}
{{- end -}}
