## Developing the Helm charts

Make sure `helm` is correctly configured (see appendixes below).

To check the content of the charts:

    helm lint --strict ./ontrack

To test the chart without actually running it:

    helm install --dry-run ./ontrack

To deploy it:

    helm install ./ontrack

Note the name of the release being deployed. To get its status:

    helm status <release-name>

To upgrade the release after a change:

    helm upgrade <release-name> ./ontrack

## Appendixes

### Setting up Helm

Create a namespace and set it as default, `test` in this example:

    kubectl create namespace test
    kubectl config set-context $(kubectl config current-context) \
        --namespace=test

Tiller must be secured. Follow instructions in the
[Helm documentation](https://docs.helm.sh/using_helm/#role-based-access-control):

Finally, run, for example:

    helm init --service-account tiller    

(assuming `tiller` is the service account to use)

