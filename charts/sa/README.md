Creating a namespace:

    kubectl create namespace test

Deploying an creating a service:

    kubectl create --namespace test -f deployment.yaml
    kubectl create --namespace test -f service.yaml

Enabling the Ingress mode in Minikube:

    minikube addons enable ingress

Registering the ingress:

    kubectl create --namespace test -f ingress.yaml

Adding the host in the local `/etc/hosts`:

    echo "$(minikube ip) ontrack.local" | sudo tee -a /etc/hosts

Service is now available:

    curl -v http://ontrack.local
