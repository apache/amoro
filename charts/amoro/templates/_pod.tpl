{{- define "amoro.pod.initContainer.flink" -}}
- name: flinkInstall

{{- end -}}


{{- define "amoro.pod.initContainers" -}}
{{- if .Values.optimizer.flink.enabled -}}
{{- end -}}
{{- end }}

