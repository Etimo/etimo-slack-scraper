#!/usr/bin/env bash
set -euo pipefail

export TILLER_NAMESPACE=slack-scraper
export KUBECONFIG=deploy/kubeconfig

export HELM_TLS_ENABLE=true
export HELM_TLS_VERIFY=true
export HELM_TLS_CA_CERT=deploy/tiller-certs/ca.crt
export HELM_TLS_CERT=deploy/tiller-certs/tls.crt
export HELM_TLS_KEY=deploy/tiller-certs/tls.key

mkdir -p deploy/tiller-certs
kubectl get secret/tiller-secret --output=go-template='{{index .data "ca.crt"}}' | base64 -d > $HELM_TLS_CA_CERT
kubectl get secret/tiller-secret --output=go-template='{{index .data "tls.crt"}}' | base64 -d > $HELM_TLS_CERT
kubectl get secret/tiller-secret --output=go-template='{{index .data "tls.key"}}' | base64 -d > $HELM_TLS_KEY

sbt docker:publish kubernetesHelmImageValues
helm init --client-only
helm upgrade slack-scraper charts/etimo-slack-scraper --install --namespace slack-scraper --values target/helm-images.yaml
