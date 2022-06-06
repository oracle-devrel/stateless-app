# Instructions


## Pre-requisites

1. Install step

```
wget https://raw.githubusercontent.com/CloudTestDrive/helidon-kubernetes/master/setup/common/download-step.sh 

chmod +x download-step.sh

./download-step.sh
```

These instructions assume that you have created your OKE cluster and configured kubeconfig.

## Setup Ingress Controller

1. Create the ingress namespace

`kubectl create namespace ingress-nginx`

2. Add the helm repo

`helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx`

3. Install the Ingress Controller

 `helm install ingress-nginx ingress-nginx/ingress-nginx \
  --namespace ingress-nginx \
  --version 4.1.0 \
  --set rbac.create=true  \
  --set controller.service.annotations."service\.beta\.kubernetes\.io/oci-load-balancer-protocol"=TCP --set controller.service.annotations."service\.beta\.kubernetes\.io/oci-load-balancer-shape"=10Mbps`
 
4. Get the LB external IP address, set it in the env var EXTERNAL_IP

`export EXTERNAL_IP=$(kubectl -n ingress-nginx get svc ingress-nginx-controller --output jsonpath='{.status.loadBalancer.ingress[0].ip}')`

5. Create the stateless app namespace

`kubectl create ns stateless`

5.1 Create the certs for the service

`$HOME/keys/step certificate create statelessback.$EXTERNAL_IP.nip.io tls-statelessback-$EXTERNAL_IP.crt tls-statelessback-$EXTERNAL_IP.key --profile leaf  --not-after 8760h --no-password --insecure --kty=RSA --ca $HOME/keys/root.crt --ca-key $HOME/keys/root.key`

`$HOME/keys/step certificate create statelessfront.$EXTERNAL_IP.nip.io tls-statelessfront-$EXTERNAL_IP.crt tls-statelessfront-$EXTERNAL_IP.key --profile leaf  --not-after 8760h --no-password --insecure --kty=RSA --ca $HOME/keys/root.crt --ca-key $HOME/keys/root.key`
 
`$HOME/keys/step certificate create zipkin.$EXTERNAL_IP.nip.io tls-zipkin-$EXTERNAL_IP.crt tls-zipkin-$EXTERNAL_IP.key --profile leaf  --not-after 8760h --no-password --insecure --kty=RSA --ca $HOME/keys/root.crt --ca-key $HOME/keys/root.key`

5.2 Create the secrets with the certs

`kubectl create secret tls tls-statelessback --key tls-statelessback-$EXTERNAL_IP.key --cert tls-statelessback-$EXTERNAL_IP.crt --namespace serverless`

`kubectl create secret tls tls-statelessfront --key tls-statelessfront-$EXTERNAL_IP.key --cert tls-statelessfront-$EXTERNAL_IP.crt --namespace serverless`

`kubectl create secret tls tls-zipkin --key tls-zipkin-$EXTERNAL_IP.key --cert tls-zipkin-$EXTERNAL_IP.crt --namespace serverless`

5.3 Deploy the services

`kubectl apply -f serviceStatelessBack.yaml --namespace serverless`

`kubectl apply -f serviceStatelessFront.yaml --namespace serverless`

`kubectl apply -f serviceZipkin.yaml --namespace serverless`


5.4 Edit the ingress rules files, replace ${EXTERNAL_IP} with the IP Address of the ingress controller. there are multiple occurences in each file

5.5 Deploy the ingress rules

`kubectl apply -f ingressStatelessBack.yaml --namespace serverless`

`kubectl apply -f ingressStatelessFront.yaml --namespace serverless`

`kubectl apply -f ingressZipkinRules.yaml --namespace serverless`

to create the config maps / secrets run the following ** IN THE DIRECTORY this file is in

`kubectl create secret generic sb-secret --from-file=./confsecure --namespace serverless`

`kubectl create configmap generic sf-config-map --from-file=./conf --namespace serverless`

5.6 Setup your image pull secret (this assumes a federated user)

`kubectl create secret docker-registry stateless-image-pull --docker-server=iad.ocir.io --docker-username=<your storage namespace>/oracleidentitycloudservice/<your login> --docker-password='<your-auth token>' --docker-email=<your-email> --namespace serverless`

5.7 Run the actual deployments

`kubectl apply -f deploymentZipkin.yaml --namespace serverless`

`kubectl apply -f deploymentStatelessBack.yaml --namespace serverless`

`kubectl apply -f deploymentStatelessFront.yaml --namespace serverless`

To test a request with no name (assuming you haven't changed the prefix)

Note that this calls the serverlessfront service, which then makes an internal call to the serverlessback service

`curl -i -k http://serverlessfront.$EXTERNAL_IP.nio/io:8080/greet`

should return 

```
HTTP/1.1 200 OK
Content-Type: application/json
Date: Mon, 30 May 2022 14:07:02 GMT
connection: keep-alive
content-length: 52

{"language":"English","name":"","prefix":"Hello"}
```
To test a request with a specified name (assuming you haven't changed the prefix)

`curl -i -k http://serverlessfront.$EXTERNAL_IP.nio.io/greet/Tim -X POST`

should return 

```
HTTP/1.1 200 OK
Content-Type: application/json
Date: Mon, 30 May 2022 14:07:02 GMT
connection: keep-alive
content-length: 52

{"language":"English","name":"Tim","prefix":"Hello"}
```

to confirm the prefix (this goes direct to the backend service

`curl -i http://serverlessback.$EXTERNAL_IP.nio.io/prefix`

```
HTTP/1.1 200 OK
Content-Type: application/json
Date: Mon, 30 May 2022 18:02:01 GMT
connection: keep-alive
content-length: 18

{"prefix":"Hello"}
```
to change the prefix

`curl -i http://serverlessback.$EXTERNAL_IP.nio.io/prefix -X PUT -d '{"prefix": "new prefix"}' -H 'Content-type: application/json'`

```
HTTP/1.1 200 OK
Content-Type: application/json
Date: Mon, 30 May 2022 18:03:51 GMT
connection: keep-alive
content-length: 68

{"newPrefix":{"prefix":"new prefix"},"oldPrefix":{"prefix":"Hello"}
```

To test a request with a specified name after you changed the prefix

`curl -i -k http://serverlessfront.$EXTERNAL_IP.nio.io/greet/Tim -X POST`

should return 

```
HTTP/1.1 200 OK
Content-Type: application/json
Date: Mon, 30 May 2022 14:07:02 GMT
connection: keep-alive
content-length: 52

{"language":"English","name":"Tim","prefix":"new prefix"}
```

Endpoints available on both serverlessfront and serverlessback services are

GET on `/status` - returns a status object (e.g. for is it alive type checks) this contains a bunch of potentially useful info if you want to control the ready, status or service

POST to `/autohang/<duration seconds>` after the specified time period requests to the /health endpoints will hang for 60 seconds before responding to simulate degenerating service responsiveness (e.g. a deadlock)

GET on `/autohang` provides information in if the timer is active, and if so when it will kick in

POST to `/autocrash/seconds` after the specified time perid calls to the /greet or /prefix endpoints (as appropriate) will cause the apoplication to terminate simulating a application crash

GET on `/autocrash` provides information in if the timer is active, and if so when it will kick in


POST to `/autoready/seconds` after the specified time period a call to the /health/ready endpoint will cause it to return a state indicating that the servcie is no longer ready (it will however actually respond to all requests.

GET on `/autoready` provides information in if the timer is active, and if so when it will kick in


On startup all of these are disabled.

For any of the "auto" endpoints setting a negative duration will remove the times so that the action will be disabled - most usefull for the autoready one.

GET on `/openapi` - generates an open api description