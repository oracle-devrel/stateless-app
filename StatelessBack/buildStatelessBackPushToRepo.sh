 #!/bin/bash
mvn clean package
. ./repoConfig1.sh
docker build  --tag "$REPO":latest --tag "$REPO":0.0.1 --file Dockerfile .
docker push "$REPO":latest
docker push "$REPO":0.0.1
echo build and pushed to $REPO with tags 0.0.1

. ./repoConfig2.sh
docker build  --tag "$REPO":latest --tag "$REPO":0.0.1 --file Dockerfile .
docker push "$REPO":latest
docker push "$REPO":0.0.1
echo build and pushed to $REPO with tags 0.0.1
