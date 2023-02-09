#!/bin/bash

###################
### Infra Setup ###
###################

kind create cluster \
  --name nrvsotel \
  --config ./helpers/kind-config.yaml
  # --image=kindest/node:v1.24.0
