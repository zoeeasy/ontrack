## Deploying Ontrack V2 on Minikube

Creating a namespace:

    kubectl create namespace test

Deploying an creating a service:

    kubectl create --namespace test -f deployment.yaml
    kubectl create --namespace test -f service.yaml

Enabling the Ingress mode in Minikube:

    minikube addons enable ingress

Change the host name in `ingress.yaml` using an address like

    ontrack.$(minikube ip).nip.io

Registering the ingress:

    kubectl create --namespace test -f ingress.yaml

Service is now available:

    curl -v http://ontrack.$(minikube ip).nip.io

## Upgrading the version of Ontrack

Upgrading the image:

    kubectl --namespace test set image deployment/ontrack-v2-deployment ontrack=nemerosa/ontrack:2.34.6

After a while, the new version is available and the old one has been discarded.
