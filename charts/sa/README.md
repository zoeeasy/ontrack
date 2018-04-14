## Deploying on GCE

    kubectl create -f h2.yaml
    kubectl create -f ontrack.yaml

## Deploying on Minikube

Creating a namespace:

    kubectl create namespace test

Deploying:

    kubectl create --namespace test -f h2.yaml
    kubectl create --namespace test -f ontrack.yaml

To open in a browser:

    minikube service --namespace test ontrack-v2-service


Or to get its URL:

    minikube service --namespace test --url ontrack-v2-service

## TODOs

* Upgrading the version of Ontrack
