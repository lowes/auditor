# Auditor-v1 app

### Using auditor-v1 app server
To build it locally, run the following commands from root project location.
- ##### Build:
    ```
    ./gradlew clean app:build
    ```
After the build, an all dependency inclusive fat jar will be produced in location:`app/build/libs` with name `app-<version-info>.jar`.
you can use the jar for deployment.


## Configurations
This module comes with following configurations:
```
auditor:
  event:
    kafkaConsumer:
      bootstrapServers: "localhost:9092"        # comma separated list of host:port pairs of kafka brokers.
      configs:                                  # additional configs to be used in kafka consumer
        client.id: auditor-consumer-client-id
        group.id: auditor-consumer-group-id
      enabled: true                             # enable/disable kafka producer. Default value: false
      topic: auditTopic                         # name of the kafka topic
  elasticsearch:
    clusterUrl: "localhost:9200"                # host:port url of elastic search cluster.

repository:
  auditEventIndexPrefix: "item-"                 # prefix to be used to generate index/table name while saving the audit event data
  auditLogIndexAlias: "log-item-rollover-alias"  # alias to be used to generate index/table name while saving the audit log data
```
Refer [application.yml](./src/main/resources/application.yml) for configurations.

## Running locally
All the below commands are supposed to be executed inside `app` directory
This examples uses containers behind the scene to spin up elastic-search and kibana containers.
You can run the containers by executing either of the following commands:
#### Using docker:
```
docker compose up -d
```
#### Using lima-vm:
```
lima nerdctl compose up -d
```
Once the containers are up you can run the app server locally by executing following command:
```
 ../gradlew clean run
``` 