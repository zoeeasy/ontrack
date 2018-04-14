## Deploying

    kubectl create namespace test
    kubectl apply --namespace test -f ontrack.yaml

## Deploying on Minikube

To open in a browser:

    minikube service --namespace test ontrack-v2-service

Or to get its URL:

    minikube service --namespace test --url ontrack-v2-service

## Upgrading the version of Ontrack

Change the Ontrack version in `ontrack.yml`.

Redeploy:

    kubectl apply --namespace test -f ontrack.yaml
