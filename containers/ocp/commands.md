## Useful OC commands

#### Create a project and add a user as admin
```bash
$ oc adm new-project test --admin=developer
```

#### Import a template into a project
```bash
$ oc create -f template.yml -n <project-name> (--dry-run --validate)
```

#### Log into the minishift registry
```bash
docker login -u developer -p $(oc whoami -t) $(minishift openshift registry)
```

#### Tag an image before pushing
```bash
docker tag my-app $(minishift openshift registry)/myproject/my-app
```

#### Untag an image
```bash
docker rmi $(minishift openshift registry)/myproject/my-app
```

#### Push image 
```bash
docker push $(minishift openshift registry)/myproject/my-app
```

- oc delete project test Expose 

#### Expose the Docker Registry service

- oc expose service docker-registry -n default (docker-registry-default.router.default.svc.cluster.local:5000)
- oc whoam -t 

- oc expose service docker-registry -n default (docker-registry-default.router.default.svc.cluster.local:5000)
- oc create serviceaccount pusher
- oc policy add-role-to-user system:image-builder system:serviceaccount:pushed:pusher
- oc describe sa pusher
- oc describe secret pusher-token-?????
- oc get svc/docker-registry (get the registry IP)
- oc login --username gatblau --password <token-value> <registry_IP:5000>

# Useful Links
- [Push & Pull Container Images to OCP](https://blog.openshift.com/remotely-push-pull-container-images-openshift/)