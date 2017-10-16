<img src="doc/logo.png" width="75" height="75" align="right">

# Prometeo

Prometeo is a scalable containerised Ansible control node running on OpenShift.

It exposes a Web API accepting a YAML payload and logs the execution steps and errors to a MongoDb database.

<img src="doc/prometeo.png"/>

## Getting Started

The easiest way to get Prometeo up and running is to use docker-compose to get the Prometeo web API and the Log containers running.

In order to build the required Docker images the [docker-build.sh](https://github.com/prometeo-cloud/prometeo/blob/master/docker-build.sh) script can be executed as follows:

```bash
$ curl https://raw.githubusercontent.com/prometeo-cloud/prometeo/master/docker-build.sh -O docker-build.sh && sh docker-build.sh
```
[Docker Compose](https://docs.docker.com/compose/) needs to be installed so that the following command can be run:

```bash
$ curl https://raw.githubusercontent.com/prometeo-cloud/prometeo/master/docker-compose.yml -O docker-compose.yml && docker-compose up
```
To test the Web API is running click on the following link: [Web API](http://localhost:8080).

## Testing the control node

To test the control node, a payload needs to be posted to the Web API to the **/run** URL. An application such as [Postman](https://www.getpostman.com/) can be used to post the payload.

The parameters for the request are:
- **URI**: http://localhost:8080/run
- **Method**: POST
- **Content-Type header**: "application/x-yaml"
- **Body format**: RAW (with content as per the example below)

The service responds immediately (as it executes asynchronously) passing back a global unique identifier that can be used to query the state of the process later.

The results can be obtained by inspecting the mongo database. [Robo 3T](https://robomongo.org/) can be used to query Mongo with a connection **localhost:27017**.

### Payload format

The payload needs to be in YAML format, and contain two main elements namely **command** and **vars**, as shown in the following example:

```YAML
---
- command:
    repoUri: "https://github.com/prometeo-cloud/prometeo_cfg_test"
    tag: ""
    verbosity: "vvv"
    callbackUri: "https://myapp/callme/"
    project: "PO123"
- vars:
    test_url: "http://repo1.maven.org/maven2/io/swagger/swagger-core/1.5.9/swagger-core-1.5.9.pom"
    test_dictionary:
        name: "Martin"
        job: "Elite Developer"
        skill: "Elite"
...
```

The **command** element contains information used by Prometeo to retrieve and execute an Ansible playbook.


| Variable  | Description  | Mandatory  |  
|---|---|---|
| repoUri  | The URI of the git repository containing the Ansible scripts to run.  | yes  |  
| tag  | The tag in the git repository to use or empty if the master is used.  | no  |   
| verbosity  | The level of verbosity of the Ansible execution output that is recorded in the Log database. The verbosity can be v, vv, vvv or vvvv. The default value is v. | no  |   
| callbackUri  | The URI Prometeo will call back when the process complete.  | no  |  
| project  | The unique reference identifying the project associated with the request. | yes  |    

The **vars** element contains all the configuration variables required by the executing Ansible playbook.

The format for this section is basically a list of variables in YAML format. The variables in this section are the ones required by the scripts in the repoUri to run.

## Querying process status

In order to find the log entries associated with a particular process, execute the following query passing the identifier of the process (processId) retrieved when the execution was requested:

- **URI**: http://localhost:8080/logs/{processId}
- **Method**: GET
- **Accept header**: "application/x-yaml" or "application/json"

## Web API documentation

Prometeo uses Swagger to document its web API.

To access the Swagger UI try the following link http://localhost:8080/swagger-ui.html


## Ansible Project format

Prometeo makes explicit assumptions about the format of the Ansible projects that runs.

Two types of git repositories are required:
- **Configuration Repository**: it contains a playbook, an inventory and a requirements file. The purpose of this repository is to glue together a series of roles to execute the required automation. See the [prometeo_cfg_test](https://github.com/prometeo-cloud/prometeo_cfg_test) repository for an example. This is the repository specified in the **repoUri** variable in the payload. Prometeo clones this repository and executes ansible-galxy using the requirements.yml file to pull any roles used by the playbook.
- **Role Repository**: it contains an Ansible role. Variables are automatically passed by Prometeo to the role. See the [prometeo_role_test](https://github.com/prometeo-cloud/prometeo_role_test) for an example of a role repository.

## Configuration Variables
The prometeo docker image can be configured by changing the following environment variables:

| Variable  | Description  | Default Value  |
|---|---|---|
| HTTP_PORT  | The port number the Web API is listening for incoming HTTP connections.  | 8080  |  
| LOG_DB_HOST  | The hostname of the MongoDb database used for logging.  | log  |   
| LOG_DB_PORT  | The port number of the MongoDb database used for logging.  | 27017  |   
| LOG_DB_NAME  | The name of the log database. | prometeo |
| CORE_POOL_SIZE | Set the ThreadPoolExecutor's core pool size used to manage Ansible tasks.  | 2 |
| MAX_POOL_SIZE | Set the ThreadPoolExecutor's maximum pool size used to manage Ansible tasks. | 2 |
| QUEUE_CAPACITY | Set the capacity for the ThreadPoolExecutor's BlockingQueue. | 500 |
