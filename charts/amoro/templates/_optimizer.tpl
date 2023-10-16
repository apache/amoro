
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

