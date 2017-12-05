## Commands to build and deploy


### Base JRE Image
```
oc new-build https://github.com/noelo/prometeo-docker --context-dir=jre8  --to=prometeojre --strategy=docker --name=prometejre8
```

### Ansible layer
```
oc new-build https://github.com/noelo/prometeo-docker --context-dir=prometeo  --to=prometeo --strategy=docker --name=prometeo
```

### Prometeo App
```
oc new-build  -i prometeo --binary=true --to=prometeoapp --strategy=source
```

#### Link the Base and Ansible build triggers
```
oc set triggers bc/prometeo --from-image=myproject/prometeojre:latest
```

#### Start the application build with an existing jar file
```
oc start-build prometeoapp --from-file=./target/prometeo-0.0.1-SNAPSHOT.jar --follow
```

### Add role priviledge to ensure that spring-cloud-kubernetes can access the configmap
```
oc policy add-role-to-user view system:serviceaccount:$(oc project -q):default -n $(oc project -q)
```

### Add the configmap to OSE
```
oc create configmap prometeo --from-file=application.properties
```

### Create the application
```
oc new-app prometeoapp
```

## SSH Key handling

### Create the secret
```
oc create secret generic sshkey --from-file=id_rsa=../.ssh/id_rsa
```

### Mount the secret into the pod
```
oc volume dc/prometeoapp --add -t secret -m /prometeo/.ssh --secret-name='sshkey' --default-mode='0600'
```