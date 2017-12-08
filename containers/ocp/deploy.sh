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

oc new-project prometeo-demo --token=$TOKEN
oc new-project prometeo-test --token=$TOKEN
oc new-project prometeo-dev --token=$TOKEN

# set the project to DEV
oc project prometeo-dev --token=$TOKEN

# Jenkins
# =======

# create a persistent jenkins
oc process -n openshift jenkins-persistent --token=$TOKEN | oc create -f- -n prometeo-dev --token=$TOKEN


# Build Configurations
# ====================

# create a build configuration for the JRE image
oc new-build https://github.com/prometeo-cloud/prometeo-docker --context-dir=jre8  --to=prometeojre --strategy=docker --name=prometejre8 --token=$TOKEN

# create a build configuration for the image containing Ansible
oc new-build https://github.com/prometeo-cloud/prometeo-docker --context-dir=prometeo  --to=prometeo --strategy=docker --name=prometeo --token=$TOKEN

# create a build configuration to build the jar file
oc new-build  -i prometeo --binary=true --to=prometeoapp --strategy=source --token=$TOKEN

# create a build configuration for the jenkins pipeline to build prometeo_web from source
oc new-build --env="APP_GIT_URL=https://github.com/prometeo-cloud/prometeo_web.git" https://github.com/prometeo-cloud/prometeo_web --strategy=pipeline --name=prometeo-web-pipeline --token=$TOKEN

# updates the bc to add repo URLs
oc patch bc prometeo-web-pipeline -p '{"spec":{"strategy":{"jenkinsPipelineStrategy":{"env": [{"name":"APP_GIT_URL","value":"https://github.com/prometeo-cloud/prometeo_web"}]}}}}' --token=$TOKEN

# create a build configuration for the jenkins pipeline to build prometeo from source
oc new-build --env="API_GIT_URL=https://github.com/prometeo-cloud/prometeo.git" https://github.com/prometeo-cloud/prometeo --strategy=pipeline --name=prometeo-pipeline --token=$TOKEN

# Triggers
# ========

# triggers build of the ansible image if the parent changes
oc set triggers bc/prometeo --from-image=myproject/prometeojre:latest --token=$TOKEN


# MongoDb
# =======

# create a persistent mongodb instance from a template
oc process -n openshift mongodb-persistent | oc create -f- --token=$TOKEN

# Prometeo
# ============

# create the prometeo
oc new-app prometeoapp --token=$TOKEN

# mount the mongodb secret into the prometeoapp pod
oc volume dc/prometeoapp --add -t secret -m /tmp/secrets --secret-name=mongodb --name=mongodb-secret --token=$TOKEN

# generate keys
ssh-keygen -f id_rsa -N ''

# create secret for the key
oc create secret generic sshkey --from-file=id_rsa=../.ssh/id_rsa --token=$TOKEN

# mount the secret
oc volume dc/prometeoapp --add -t secret -m /prometeo/.ssh/keys --secret-name='sshkey' --default-mode='0600' --token=$TOKEN


# Prometeo-Web
# ============

# create the prometeo_web application with environment variables
oc new-app prometeoweb --env="ADMIN_PASSWORD=$WEB_APP_PWD PROMETEO_AUTHORIZATION=$API_AUTH_HEADER PROMETEO_WEB_URL=http://prometeoapp-$DEV_PROJECT_NAME.$CLUSTER_IP.nip.io" --token=$TOKEN

# create a route
oc expose svc prometeoweb --token=$TOKEN

