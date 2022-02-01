# Auditor-v1 client-example

## Configurations
Refer `client`'s README.md for detailed configuration list.

## Running locally
All the below commands are supposed to be executed inside `client-example` directory
This examples uses containers behind the scene to spin up kafka and zookeeper containers.
You can run the containers by executing either of the following commands:
#### Using docker:
```
docker compose up -d
```
#### Using lima-vm:
```
lima nerdctl compose up -d
```
Once the containers are up you can run the Example applications.<br/>
For java, run: [StandalonePriceApplication.java](./src/main/java/fooprice/StandalonePriceApplication.java)<br/>
For kotlin, run: [StandaloneItemApplication.kt](./src/main/kotlin/foo/StandaloneItemApplication.kt)

Above examples, will publish both `audit` and `log` information to `auditTopic` in kafka. To view the results in kafka,
you can use any available kafka explorers tool. For purpose of this readme, we will rely on [kafka drop](https://github.com/obsidiandynamics/kafdrop).
Once you have the kafka drop ready and installed in your system, you can view the audit results by checking the `auditTopic` in kafka.
