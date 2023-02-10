#!/bin/bash

###################
### Parse input ###
###################

while (( "$#" )); do
  case "$1" in
    --platform)
      platform="$2"
      shift
      ;;
    --build)
      build="true"
      shift
      ;;
    *)
      shift
      ;;
  esac
done

# Docker platform
if [[ $platform == "" ]]; then
  # Default is amd
  platform="amd64"
else
  if [[ $platform != "amd64" && $platform != "arm64" ]]; then
    echo "Platform can either be 'amd64' or 'arm64'."
    exit 1
  fi
fi

#####################
### Set variables ###
#####################

repoName="nr-vs-otel"
clusterName="nr-vs-otel"

# certmanager
declare -A certmanager
certmanager["name"]="cert-manager"
certmanager["namespace"]="cert-manager"

# oteloperator
declare -A oteloperator
oteloperator["name"]="otel-operator"
oteloperator["namespace"]="monitoring"

# prometheus
declare -A prometheus
prometheus["name"]="prometheus"
prometheus["namespace"]="monitoring"

# kafka
declare -A kafka
kafka["name"]="kafka"
kafka["namespace"]="common"
kafka["topicnr"]="newrelic"
kafka["topicotel"]="otel"

# mysql
declare -A mysql
mysql["name"]="mysql"
mysql["namespace"]="common"
mysql["username"]="root"
mysql["password"]="verysecretpassword"
mysql["port"]=3306
mysql["database"]="nrvsotel"

# persistencenr
declare -A persistencenr
persistencenr["name"]="persistencenr"
persistencenr["imageName"]="${repoName}:${persistencenr[name]}-${platform}"
persistencenr["namespace"]="newrelic"
persistencenr["replicas"]=1
persistencenr["port"]=8080

# proxynr
declare -A proxynr
proxynr["name"]="proxynr"
proxynr["imageName"]="${repoName}:${proxynr[name]}-${platform}"
proxynr["namespace"]="newrelic"
proxynr["replicas"]=1
proxynr["port"]=8080

# otelcollector
declare -A otelcollector
otelcollector["name"]="otel-collector"
otelcollector["namespace"]="otel"
otelcollector["mode"]="deployment"
otelcollector["prometheusPort"]=9464

# persistenceotel
declare -A persistenceotel
persistenceotel["name"]="persistenceotel"
persistenceotel["imageName"]="${repoName}:${persistenceotel[name]}-${platform}"
persistenceotel["namespace"]="otel"
persistenceotel["replicas"]=1
persistenceotel["port"]=8080

# proxyotel
declare -A proxyotel
proxyotel["name"]="proxyotel"
proxyotel["imageName"]="${repoName}:${proxyotel[name]}-${platform}"
proxyotel["namespace"]="otel"
proxyotel["replicas"]=1
proxyotel["port"]=8080

####################
### Build & Push ###
####################

if [[ $build == "true" ]]; then
  # persistencenr
  docker build \
    --platform "linux/${platform}" \
    --tag "${DOCKERHUB_NAME}/${persistencenr[imageName]}" \
    "../../apps/persistencenr/."
  docker push "${DOCKERHUB_NAME}/${persistencenr[imageName]}"

  # proxynr
  docker build \
    --platform "linux/${platform}" \
    --tag "${DOCKERHUB_NAME}/${proxynr[imageName]}" \
    "../../apps/proxynr/."
  docker push "${DOCKERHUB_NAME}/${proxynr[imageName]}"

  # persistenceotel
  docker build \
    --platform "linux/${platform}" \
    --tag "${DOCKERHUB_NAME}/${persistenceotel[imageName]}" \
    "../../apps/persistenceotel/."
  docker push "${DOCKERHUB_NAME}/${persistenceotel[imageName]}"

  # proxyotel
  docker build \
    --platform "linux/${platform}" \
    --tag "${DOCKERHUB_NAME}/${proxyotel[imageName]}" \
    "../../apps/proxyotel/."
  docker push "${DOCKERHUB_NAME}/${proxyotel[imageName]}"
fi

###################
### Deploy Helm ###
###################

# Add helm repos
helm repo add jetstack https://charts.jetstack.io
helm repo add open-telemetry https://open-telemetry.github.io/opentelemetry-helm-charts
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

# cert-manager
helm upgrade ${certmanager[name]} \
  --install \
  --wait \
  --debug \
  --create-namespace \
  --namespace ${certmanager[namespace]} \
  --version v1.11.0 \
  --set installCRDs=true \
  "jetstack/cert-manager"

# otel-operator
helm upgrade ${oteloperator[name]} \
  --install \
  --wait \
  --debug \
  --create-namespace \
  --namespace ${oteloperator[namespace]} \
  "open-telemetry/opentelemetry-operator"

# prometheus
helm upgrade ${prometheus[name]} \
  --install \
  --wait \
  --debug \
  --create-namespace \
  --namespace ${prometheus[namespace]} \
  --set alertmanager.enabled=false \
  --set prometheus-pushgateway.enabled=false \
  --set kubeStateMetrics.enabled=true \
  --set nodeExporter.enabled=true \
  --set nodeExporter.tolerations[0].effect="NoSchedule" \
  --set nodeExporter.tolerations[0].operator="Exists" \
  --set server.remoteWrite[0].url="https://metric-api.eu.newrelic.com/prometheus/v1/write?prometheus_server=${clusterName}" \
  --set server.remoteWrite[0].bearer_token=$NEWRELIC_LICENSE_KEY \
  "prometheus-community/prometheus"

# kafka
helm upgrade ${kafka[name]} \
  --install \
  --wait \
  --debug \
  --create-namespace \
  --namespace=${kafka[namespace]} \
  "bitnami/kafka"

# mysql
helm upgrade ${mysql[name]} \
  --install \
  --wait \
  --debug \
  --create-namespace \
  --namespace=${mysql[namespace]} \
  --set auth.rootPassword=${mysql[password]} \
  --set auth.database=${mysql[database]} \
    "bitnami/mysql"

# persistencenr
helm upgrade ${persistencenr[name]} \
  --install \
  --wait \
  --debug \
  --create-namespace \
  --namespace=${persistencenr[namespace]} \
  --set dockerhubName=$DOCKERHUB_NAME \
  --set imageName=${persistencenr[imageName]} \
  --set imagePullPolicy="Always" \
  --set name=${persistencenr[name]} \
  --set replicas=${persistencenr[replicas]} \
  --set port=${persistencenr[port]} \
  --set kafka.address="${kafka[name]}.${kafka[namespace]}.svc.cluster.local:9092" \
  --set kafka.topic=${kafka[topicnr]} \
  --set kafka.groupId=${persistencenr[name]} \
  --set newrelic.appName=${persistencenr[name]} \
  --set newrelic.licenseKey=$NEWRELIC_LICENSE_KEY \
  --set mysql.server="${mysql[name]}.${mysql[namespace]}.svc.cluster.local" \
  --set mysql.username=${mysql[username]} \
  --set mysql.password=${mysql[password]} \
  --set mysql.port=${mysql[port]} \
  --set mysql.database=${mysql[database]} \
  "../helm/persistencenr"

# proxynr
helm upgrade ${proxynr[name]} \
  --install \
  --wait \
  --debug \
  --create-namespace \
  --namespace=${proxynr[namespace]} \
  --set dockerhubName=$DOCKERHUB_NAME \
  --set imageName=${proxynr[imageName]} \
  --set imagePullPolicy="Always" \
  --set name=${proxynr[name]} \
  --set replicas=${proxynr[replicas]} \
  --set port=${proxynr[port]} \
  --set kafka.address="${kafka[name]}-0.${kafka[name]}-headless.${kafka[namespace]}.svc.cluster.local:9092" \
  --set kafka.topic=${kafka[topicnr]} \
  --set newrelic.appName=${proxynr[name]} \
  --set newrelic.licenseKey=$NEWRELIC_LICENSE_KEY \
  --set endpoints.persistence="http://${persistencenr[name]}.${persistencenr[namespace]}.svc.cluster.local:${persistencenr[port]}/persistence" \
  "../helm/proxynr"

# otelcollector
helm upgrade ${otelcollector[name]} \
  --install \
  --wait \
  --debug \
  --create-namespace \
  --namespace ${otelcollector[namespace]} \
  --set name=${otelcollector[name]} \
  --set mode=${otelcollector[mode]} \
  --set prometheus.port=${otelcollector[prometheusPort]} \
  --set newrelicOtlpEndpoint="otlp.eu01.nr-data.net:4317" \
  --set newrelicLicenseKey=$NEWRELIC_LICENSE_KEY \
  "../helm/otelcollector"

# persistenceotel
helm upgrade ${persistenceotel[name]} \
  --install \
  --wait \
  --debug \
  --create-namespace \
  --namespace=${persistenceotel[namespace]} \
  --set dockerhubName=$DOCKERHUB_NAME \
  --set imageName=${persistenceotel[imageName]} \
  --set imagePullPolicy="Always" \
  --set name=${persistenceotel[name]} \
  --set replicas=${persistenceotel[replicas]} \
  --set port=${persistenceotel[port]} \
  --set kafka.address="${kafka[name]}.${kafka[namespace]}.svc.cluster.local:9092" \
  --set kafka.topic=${kafka[topicotel]} \
  --set kafka.groupId=${persistenceotel[name]} \
  --set endpoints.otelcollector="http://${otelcollector[name]}-collector.${otelcollector[namespace]}.svc.cluster.local:4317" \
  --set mysql.server="${mysql[name]}.${mysql[namespace]}.svc.cluster.local" \
  --set mysql.username=${mysql[username]} \
  --set mysql.password=${mysql[password]} \
  --set mysql.port=${mysql[port]} \
  --set mysql.database=${mysql[database]} \
  "../helm/persistenceotel"

# proxyotel
helm upgrade ${proxyotel[name]} \
  --install \
  --wait \
  --debug \
  --create-namespace \
  --namespace=${proxyotel[namespace]} \
  --set dockerhubName=$DOCKERHUB_NAME \
  --set imageName=${proxyotel[imageName]} \
  --set imagePullPolicy="Always" \
  --set name=${proxyotel[name]} \
  --set replicas=${proxyotel[replicas]} \
  --set port=${proxyotel[port]} \
  --set kafka.address="${kafka[name]}-0.${kafka[name]}-headless.${kafka[namespace]}.svc.cluster.local:9092" \
  --set kafka.topic=${kafka[topicotel]} \
  --set endpoints.otelcollector="http://${otelcollector[name]}-collector.${otelcollector[namespace]}.svc.cluster.local:4317" \
  --set endpoints.persistence="http://${persistenceotel[name]}.${persistenceotel[namespace]}.svc.cluster.local:${persistenceotel[port]}/persistence" \
  "../helm/proxyotel"
