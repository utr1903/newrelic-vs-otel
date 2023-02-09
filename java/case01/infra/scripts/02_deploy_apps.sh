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

# proxynr
declare -A proxynr
proxynr["name"]="proxynr"
proxynr["platform"]="amd64"
proxynr["imageName"]="${repoName}:${proxynr[name]}-${proxynr[platform]}"
proxynr["namespace"]="newrelic"
proxynr["replicas"]=1
proxynr["port"]=8080

# persistencenr
declare -A persistencenr
persistencenr["name"]="persistencenr"
persistencenr["platform"]="amd64"
persistencenr["imageName"]="${repoName}:${persistencenr[name]}-${persistencenr[platform]}"
persistencenr["namespace"]="newrelic"
persistencenr["replicas"]=1
persistencenr["port"]=8080

####################
### Build & Push ###
####################

if [[ $build == "true" ]]; then
  # proxynr
  docker build \
    --platform "linux/${proxynr[platform]}" \
    --tag "${DOCKERHUB_NAME}/${proxynr[imageName]}" \
    "../../apps/proxynr/."
  docker push "${DOCKERHUB_NAME}/${proxynr[imageName]}"

  # persistencenr
  docker build \
    --platform "linux/${persistencenr[platform]}" \
    --tag "${DOCKERHUB_NAME}/${persistencenr[imageName]}" \
    "../../apps/persistencenr/."
  docker push "${DOCKERHUB_NAME}/${persistencenr[imageName]}"
fi

###################
### Deploy Helm ###
###################

# Add helm repos
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

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
