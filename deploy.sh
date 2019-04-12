#!/usr/bin/env bash
set -euo pipefail

export TILLER_NAMESPACE=slack-scraper
export KUBECONFIG=deploy/kubeconfig

export HELM_TLS_ENABLE=true
export HELM_TLS_VERIFY=true
export HELM_TLS_CA_CERT=deploy/tiller-keys/ca.crt
export HELM_TLS_CERT=deploy/tiller-keys/tls.crt
export HELM_TLS_KEY=deploy/tiller-keys/tls.key

mkdir -p deploy/tiller-certs
kubectl get secret/tiller-secret --output=go-template='{{index .data "ca.crt"}}' | base64 -d > $HELM_TLS_CA_CERT
kubectl get secret/tiller-secret --output=go-template='{{index .data "tls.crt"}}' | base64 -d > $HELM_TLS_CERT
kubectl get secret/tiller-secret --output=go-template='{{index .data "tls.key"}}' | base64 -d > $HELM_TLS_KEY

# Don't muck around with registry certs on non-CI machines, since Docker requires us to set it system-wide
if [ "$CI" = 1 ]; then
    sudo mkdir -p /etc/docker/certs.d
    sudo cp --recursive deploy/registry-certs /etc/docker/certs.d/registry.kubernetes.etimo.se
fi

sbt docker:publish kubernetesHelmImageValues
helm init --client-only
helm upgrade slack-scraper charts/etimo-slack-scraper --install --namespace slack-scraper --values target/helm-images.yaml
