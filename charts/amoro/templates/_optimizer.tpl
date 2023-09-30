
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
container-impl: com.netease.arctic.optimizer.FlinkOptimizerContainer
properties:
  target: kubernetes-application
  job-uri: {{ .Values.optimizer.flink.image.jobUri | quote }}
  ams-optimizing-uri: {{include "amoro.svc.optimizing.uri" . }}
  flink-conf.kubernetes.container.image: {{ include "amoro.optimizer.container.flink.image" .  | quote }}
  {{- with .Values.optimizer.flink.properties -}}
    {{- toYaml . | nindent 2 }}
  {{- end -}}
{{- end -}}


{{- define "amoro.optimizer.container.local" -}}
container-impl: com.netease.arctic.optimizer.LocalOptimizerContainer
properties:
  {{- with .Values.optimizer.local.properties -}}
    {{- toYaml . | nindent 2 }}
  {{- end -}}
{{- end -}}

