#!/usr/bin/env bash
# Variables
# =========

# paste the token for a cluster-admin service account below
TOKEN=''

DEV_PROJECT_NAME='prometeo-dev'
TEST_PROJECT_NAME='prometeo-test'
DEMO_PROJECT_NAME='prometeo-demo'

CLUSTER_IP='192.168.99.100'

WEB_APP_PWD='test'
API_AUTH_HEADER='test'

# CD Pipeline Projects
# ====================

#oc new-project $DEMO_PROJECT_NAME --token=$TOKEN
#oc new-project $DEMO_PROJECT_NAME --token=$TOKEN
#oc new-project $TEST_PROJECT_NAME --token=$TOKEN
oc new-project $DEV_PROJECT_NAME --token=$TOKEN

# set the project to DEV
oc project $DEV_PROJECT_NAME --token=$TOKEN

# Jenkins
# =======

# create a persistent jenkins
#oc process -n openshift jenkins-persistent --token=$TOKEN | oc create -f- -n $DEV_PROJECT_NAME --token=$TOKEN
oc process -n openshift jenkins-ephemeral --token=$TOKEN | oc create -f- -n $DEV_PROJECT_NAME --token=$TOKEN


# Build Configurations
# ====================

echo 'Creating the java image, please wait...'

oc new-build https://github.com/gatblau/ocp_s2i_java --to=java --strategy=docker --name=java --token=$TOKEN

while [ $(oc get is java | grep -c latest) -eq "0" ]; do
    sleep 1;
done

echo 'Creating the jansible image, please wait...'

oc new-build https://github.com/gatblau/ocp_s2i_java_ansible --to=jansible --strategy=docker --name=jansible --token=$TOKEN

while [ $(oc get is jansible | grep -c latest) -eq "0" ]; do
    sleep 1;
done

echo 'Linking the build of the ansible image if the parent changes'

oc set triggers bc/jansible --from-image=java:latest --token=$TOKEN

echo 'Creting a build configuration to build the jar file'

oc new-build  -i jansible --binary=true --to=prometeo --strategy=source --token=$TOKEN

# MongoDb
# =======

echo 'Creating a persistent mongodb instance from a template'

oc process -n openshift mongodb-persistent --token=$TOKEN | oc create -f- --token=$TOKEN

# Prometeo
# ============

echo 'Cloning the application repository'

git clone https://github.com/prometeo-cloud/prometeo

echo 'Compiling the source code'

mvn package -f ./prometeo

echo 'Starting a new build with the application jar file'

oc start-build prometeo --from-file=./prometeo/target/prometeo-0.0.1-SNAPSHOT.jar --follow --token=$TOKEN

echo 'Creating the prometeo application'

oc new-app prometeo-dev/prometeo:latest --token=$TOKEN

echo 'Mounting the mongodb secret into the prometeo pod'

oc volume dc/prometeo --add -t secret -m /tmp/secrets --secret-name=mongodb --name=mongodb-secret --token=$TOKEN

echo 'Generating SSH keys'

ssh-keygen -f id_rsa -N ''

echo 'Creating a secret to store the key'

oc create secret generic sshkey --from-file=id_rsa --token=$TOKEN

echo 'Mounting the secret as a persistent volume'

oc volume dc/prometeo --add -t secret -m /app/.ssh/keys --secret-name='sshkey' --default-mode='0600' --token=$TOKEN


# Prometeo-Web
# ============

echo 'Creating a build for the Web Application'

oc new-build  -i java --binary=true --to=prometeoweb --strategy=source --token=$TOKEN

echo 'Cloning the application repository'

git clone https://github.com/prometeo-cloud/prometeo_web

echo 'Compiling the source code'

mvn package -f ./prometeo_web

echo 'Starting a new build with the application jar file'

oc start-build prometeoweb --from-file=./prometeo_web/target/prometeo_web-0.0.1-SNAPSHOT.jar --follow --token=$TOKEN

echo 'Creating the prometeo application'

oc new-app prometeo-dev/prometeoweb:latest -e "ADMIN_PASSWORD=test PROMETEO_AUTHORIZATION=test PROMETEO_URL=http://prometeo-myproject.$CLUSTER_IP.nip.io" --token=$TOKEN

echo 'Exposing the service with a route'

oc expose svc prometeoweb --token=$TOKEN

