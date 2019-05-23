{{/*
For local web-deployment, export RUNTIME_PATH to be mounted read-only at /srv/runtime/current.
Defaults to an emptyDir.
See https://kubernetes.io/docs/concepts/storage/volumes/#types-of-volumes
*/}}
{{- define "service-template.volumes.runtime" }}
  {{- if .Values.web.runtime.hostPath }}
        hostPath:
          path: {{ .Values.web.runtime.hostPath }}
  {{- else }}
        emptyDir: {}
  {{- end }}
{{- end }}


{{/*
For local web-deployment, export SECRETS with a value like "env.KEY=value,env.KEY2=value2" and all keys
will be exported as environment variables to the container by the environment-configmap.yaml.
For non-local web-deployment, use aws credentials to get and export secrets as environment variables.
*/}}
{{- define "service-template.containers.web.envFrom.secretRef.aws-credentials" }}
  {{- if .Values.web.secretRef.awsCredentialsEnabled }}
        - secretRef:
            name: {{ .Chart.Name }}-aws-credentials
  {{- end }}
{{- end }}


{{/*
For non-local web-deployment, include a runtime container to refresh runtime variables every minute.
*/}}
{{- define "service-template.containers.runtime" }}
  {{- if .Values.web.runtime.containerEnabled }}
      - name: runtime
        image: ddartifacts-docker.jfrog.io/runtime:v0.1.3
        imagePullPolicy: Always
        resources:
          requests:
            cpu: 25m
            memory: 128Mi
          limits:
            cpu: 25m
            memory: 128Mi
        envFrom:
        - configMapRef:
            name: global-runtime-environment
        volumeMounts:
        - name: runtime-volume
          mountPath: /srv/runtime
  {{- end }}
{{- end }}
