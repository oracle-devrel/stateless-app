# Instructions


## Pre-requisites

These instructions assume that you have:
- created your OKE cluster and configured kubeconfig.
- Installed Java 17 and Maven >= 3.8.5

1. Install step

``` bash
wget https://raw.githubusercontent.com/CloudTestDrive/helidon-kubernetes/master/setup/common/download-step.sh 

chmod +x download-step.sh

./download-step.sh

```

2. Clone this repo

``` bash

git clone https://github.com/oracle-devrel/stateless-app.git


```

3. Set a few environment variables

``` bash

export EMAIL=<your OCI IDCS user email>
export TOKEN=<your OCI auth token>

export NS=`oci os ns get --query "data" --raw-output`

export OCIR_USER=$NS/oracleidentitycloudservice/$EMAIL

``` 


## Build applications images

If you want to create the repository in a specific compartment other than the root run:

```
oci artifacts container repository create --display-name stateless/com.oracle.dlp.stateless.front --compartment-id ocid1.compartment.oc1......

oci artifacts container repository create --display-name stateless/com.oracle.dlp.stateless.back --compartment-id ocid1.compartment.oc1......

```
Build the images for both front and back applications and push them into the repos

```
cd stateless-app/StatelessFront

. ./buildStatelessFrontPushToRepo.sh

cd ../StatelessBack

. ./buildStatelessBackPushToRepo.sh

cd ../StatelessYaml/

```

## Setup Ingress Controller

1. Create the ingress namespace

``` bash
kubectl create namespace ingress-nginx
```

2. Add the helm repo

``` bash
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
```

3. Install the Ingress Controller

 ``` bash
 helm install ingress-nginx ingress-nginx/ingress-nginx \
  --namespace ingress-nginx \
  --version 4.1.0 \
  --set rbac.create=true  \
  --set controller.service.annotations."service\.beta\.kubernetes\.io/oci-load-balancer-protocol"=TCP --set controller.service.annotations."service\.beta\.kubernetes\.io/oci-load-balancer-shape"=10Mbps
  ```
 
4. Get the LB external IP address, set it in the env var EXTERNAL_IP. Set the domain name (either using ocilabs.cloud or nip.io)

``` bash
export EXTERNAL_IP=$(kubectl -n ingress-nginx get svc ingress-nginx-controller --output jsonpath='{.status.loadBalancer.ingress[0].ip}')
echo load balancer IP: $EXTERNAL_IP

export DOMAIN=okehadr.ocilabs.cloud
# if you are using nip.io use the below instead
# export DOMAIN=$EXTERNAL_IP.nip.io
echo $DOMAIN

```

5. Create the stateless app namespace

``` bash
kubectl create ns stateless
```

5.1 Create the certs for the service

``` bash
$HOME/keys/step certificate create statelessback.$DOMAIN tls-statelessback.crt tls-statelessback.key --profile leaf  --not-after 8760h --no-password --insecure --kty=RSA --ca $HOME/keys/root.crt --ca-key $HOME/keys/root.key

$HOME/keys/step certificate create statelessfront.$DOMAIN tls-statelessfront.crt tls-statelessfront.key --profile leaf  --not-after 8760h --no-password --insecure --kty=RSA --ca $HOME/keys/root.crt --ca-key $HOME/keys/root.key
 
$HOME/keys/step certificate create zipkin.$DOMAIN tls-zipkin.crt tls-zipkin.key --profile leaf  --not-after 8760h --no-password --insecure --kty=RSA --ca $HOME/keys/root.crt --ca-key $HOME/keys/root.key
```

5.2 Create the secrets with the certs

``` bash
kubectl create secret tls tls-statelessback --key tls-statelessback.key --cert tls-statelessback.crt --namespace stateless

kubectl create secret tls tls-statelessfront --key tls-statelessfront.key --cert tls-statelessfront.crt --namespace stateless

kubectl create secret tls tls-zipkin --key tls-zipkin.key --cert tls-zipkin.crt --namespace stateless
```

5.3 Deploy the services

``` bash
kubectl apply -f yaml/service.yaml --namespace stateless
```


5.4 Deploy the ingress rules

``` bash
envsubst <yaml/ingress.yaml >yaml/ingress_final.yaml

kubectl apply -f yaml/ingress_final.yaml --namespace stateless
```

5.5 Create the config maps / secrets 

``` bash
kubectl create secret generic sb-secret --from-file=./confsecure --namespace stateless

kubectl create configmap sf-config-map --from-file=./conf --namespace stateless
```

5.6 Setup your image pull secret (this assumes a federated user) Note that this is for iad.ocir.io, if running in Pheonix then need to switch that to phx.ocir.io, but keep the secret name the same (will also need to update the deployments file somehow)

``` bash
kubectl create secret docker-registry stateless-image-pull --docker-server=$REGION_PRIMARY.ocir.io --docker-username=$OCIR_USER --docker-password="$TOKEN" --docker-email=$EMAIL --namespace stateless
```


5.7 Run the actual deployments

``` bash

envsubst <yaml/deployment.yaml >yaml/deployment_final.yaml

kubectl apply -f yaml/deployment_final.yaml --namespace stateless
```

To test a request with no name (assuming you haven't changed the prefix)

Note that this calls the statelessfront service, which then makes an internal call to the statelessback service

`curl -i -k https://statelessfront.$DOMAIN/greet`

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

`curl -i -k https://statelessfront.$DOMAIN/greet/Tim -X POST`

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

`curl -i -k https://statelessback.$DOMAIN/prefix`

```
HTTP/1.1 200 OK
Content-Type: application/json
Date: Mon, 30 May 2022 18:02:01 GMT
connection: keep-alive
content-length: 18

{"prefix":"Hello"}
```
to change the prefix

`curl -ik https://statelessback.$DOMAIN/prefix -X PUT -d '{"prefix": "new prefix"}' -H "Content-type: application/json"`

```
HTTP/1.1 200 OK
Content-Type: application/json
Date: Mon, 30 May 2022 18:03:51 GMT
connection: keep-alive
content-length: 68

{"newPrefix":{"prefix":"new prefix"},"oldPrefix":{"prefix":"Hello"}
```

To test a request with a specified name after you changed the prefix

`curl -i -k https://statelessfront.$DOMAIN/greet/Tim -X POST`

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


POST to `/autoready/seconds` after the specified time period a call to the /health/ready endpoint will cause it to return a state indicating that the service is no longer ready (it will however actually respond to all requests.

GET on `/autoready` provides information in if the timer is active, and if so when it will kick in


On startup all of these are disabled.

For any of the "auto" endpoints setting a negative duration will remove the times so that the action will be disabled - most usefull for the autoready one.

GET on `/openapi` - generates an open api description
