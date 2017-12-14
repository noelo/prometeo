#!/usr/bin/env bash
# Variables
# =========

# paste the token for a cluster-admin service account below
TOKEN=''

DEV_PROJECT_NAME='myproject-dev'
TEST_PROJECT_NAME='myproject-test'
DEMO_PROJECT_NAME='myproject-demo'

CLUSTER_IP='192.168.99.100'

WEB_APP_PWD='test'
API_AUTH_HEADER='test'


# define output colours
RED='\033[0;31m'; GREEN='\033[0;32m'; CYAN='\033[0;36m'; YELLOW='\033[0;33m'; NC='\033[0m'

out() {
   echo $1$2"${NC}"
}

# CD Pipeline Projects
# ====================

#oc new-project $DEMO_PROJECT_NAME --token=$TOKEN
#oc new-project $DEMO_PROJECT_NAME --token=$TOKEN
#oc new-project $TEST_PROJECT_NAME --token=$TOKEN

# set the project to DEV
oc project $DEV_PROJECT_NAME --token=$TOKEN

# Jenkins
# =======

out ${YELLOW} 'Creating Jenkins server'
# create a persistent jenkins
#oc process -n openshift jenkins-persistent --token=$TOKEN | oc create -f- -n $DEV_PROJECT_NAME --token=$TOKEN
oc process -n openshift jenkins-ephemeral --token=$TOKEN | oc create -f- -n $DEV_PROJECT_NAME --token=$TOKEN

read -p "Press enter to continue"

# Build Configurations
# ====================

out ${YELLOW} 'Creating the java image, please wait...'

oc new-build https://github.com/gatblau/ocp_s2i_java --to=java --strategy=docker --name=java --token=$TOKEN

while [ $(oc get is java | grep -c latest) -eq "0" ]; do
    sleep 1;
done

read -p "Press enter to continue"

out ${YELLOW} 'Creating the jansible image, please wait...'

oc new-build https://github.com/gatblau/ocp_s2i_java_ansible --to=jansible --strategy=docker --name=jansible --token=$TOKEN

while [ $(oc get is jansible | grep -c latest) -eq "0" ]; do
    sleep 1;
done

read -p "Press enter to continue"

out ${YELLOW} 'Linking the build of the Ansible image if the parent changes'

oc set triggers bc/jansible --from-image=java:latest --token=$TOKEN

out ${YELLOW} 'Creating a build configuration to build the jar file'

oc new-build  -i jansible --binary=true --to=prometeo --strategy=source --token=$TOKEN

read -p "Press enter to continue"

# MongoDb
# =======

out ${YELLOW} 'Creating a persistent mongodb instance from a template'

oc process -n openshift mongodb-persistent --token=$TOKEN | oc create -f- --token=$TOKEN

read -p "Press enter to continue"

# Prometeo
# ============

out ${YELLOW} 'Cloning the application repository'

git clone https://github.com/prometeo-cloud/prometeo

out ${YELLOW} 'Compiling the source code'

mvn package -f ./prometeo

out ${YELLOW} 'Starting a new build with the application jar file'

oc start-build prometeo --from-file=./prometeo/target/prometeo-0.0.1-SNAPSHOT.jar --follow --token=$TOKEN

read -p "Press enter to continue"

out ${YELLOW} 'Creating the prometeo application'

oc new-app prometeo:latest --token=$TOKEN

read -p "Press enter to continue"

out ${YELLOW} 'Mounting the mongodb secret into the prometeo pod'

oc volume dc/prometeo --add -t secret -m /tmp/secrets --secret-name=mongodb --name=mongodb-secret --token=$TOKEN

read -p "Press enter to continue"

out ${YELLOW} 'Generating SSH keys'

ssh-keygen -f id_rsa -N ''

out ${YELLOW} 'Creating a secret to store the key'

oc create secret generic sshkey --from-file=id_rsa --token=$TOKEN

read -p "Press enter to continue"

out ${YELLOW} 'Mounting the secret as a persistent volume'

oc volume dc/prometeo --add -t secret -m /app/.ssh/keys --secret-name='sshkey' --default-mode='0600' --token=$TOKEN

read -p "Press enter to continue"

out ${YELLOW} 'Exposing the service with a route'

oc expose svc prometeo --token=$TOKEN


# Prometeo-Web
# ============

out ${YELLOW} 'Creating a build for the Web Application'

oc new-build -i java --binary=true --to=prometeoweb --strategy=source --token=$TOKEN

read -p "Press enter to continue"

out ${YELLOW} 'Cloning the application repository'

git clone https://github.com/prometeo-cloud/prometeo_web

out ${YELLOW} 'Compiling the source code'

mvn package -f ./prometeo_web

out ${YELLOW} 'Starting a new build with the application jar file'

oc start-build prometeoweb --from-file=./prometeo_web/target/prometeo_web-0.0.1-SNAPSHOT.jar --follow --token=$TOKEN

read -p "Press enter to continue"

out ${YELLOW} 'Creating the prometeo application'

oc new-app prometeoweb:latest --token=$TOKEN

read -p "Press enter to continue"

out ${YELLOW} 'Updating environment variables'

oc env dc/prometeoweb ADMIN_PASSWORD=test PROMETEO_AUTHORIZATION=test PROMETEO_URL=http://prometeo-$DEV_PROJECT_NAME.$CLUSTER_IP.nip.io

out ${YELLOW} 'Exposing the service with a route'

oc expose svc prometeoweb --token=$TOKEN

