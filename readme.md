<img src="doc/logo.png" width="75" height="75" align="right">

# Prometeo

Prometeo is a scalable containerised Ansible control node running on OpenShift.

It exposes a Web API accepting a YAML payload and logs the execution steps and errors to a MongoDb database.

<img src="doc/prometeo.png"/>


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
