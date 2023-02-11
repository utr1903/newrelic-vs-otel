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

# simulator
declare -A simulator
simulator["name"]="simulator"
simulator["imageName"]="${repoName}:${simulator[name]}-${platform}"
simulator["namespace"]="sim"
simulator["interval"]="500"
simulator["newrelicEndpoint"]="http://proxynr.newrelic.svc.cluster.local:8080/proxy"
simulator["otelEndpoint"]="http://proxyotel.otel.svc.cluster.local:8080/proxy"
simulator["replicas"]=1

####################
### Build & Push ###
####################

if [[ $build == "true" ]]; then
  # simulator
  docker build \
    --platform "linux/${platform}" \
    --tag "${DOCKERHUB_NAME}/${simulator[imageName]}" \
    "../app/."
  docker push "${DOCKERHUB_NAME}/${simulator[imageName]}"
fi

###################
### Deploy Helm ###
###################

# simulator
helm upgrade ${simulator[name]} \
  --install \
  --wait \
  --debug \
  --create-namespace \
  --namespace=${simulator[namespace]} \
  --set dockerhubName=$DOCKERHUB_NAME \
  --set imageName=${simulator[imageName]} \
  --set imagePullPolicy="Always" \
  --set name=${simulator[name]} \
  --set replicas=${simulator[replicas]} \
  --set endpoints.newrelic=${simulator[newrelicEndpoint]} \
  --set endpoints.otel=${simulator[otelEndpoint]} \
  --set simulation.interval=${simulator[interval]} \
  "../helm"