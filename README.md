# Auditor-v1
![GitHub Workflow Status (event)](https://img.shields.io/github/workflow/status/lowes/auditor/Gradle%20Package?event=push)
![Maven Central](https://img.shields.io/maven-central/v/io.github.lowes/auditor-core)
![GitHub](https://img.shields.io/github/license/lowes/auditor)
[![Percentage of issues still open](http://isitmaintained.com/badge/open/lowes/auditor.svg)](http://isitmaintained.com/project/lowes/auditor "Percentage of issues still open")

Solves the audit needs for any JVM based application.
### Version
The latest release `version` is `<//todo-will be updated once the maven central is integrated>`
<br/><br/>
### Motivation
Auditing is a cross-cutting concerns of many applications supporting business process/workflows.
There are some solutions out there like [Javers](https://javers.org/), [Audit4J](https://github.com/audit4j), [Log4j Audit](https://logging.apache.org/log4j-audit/latest/index.html) etc.. that aims to provide auditing functionality for java applications. 
However, most of them focus on capturing the audit information and storing in some sort of database or filesystem.
While this is desirable in most traditional use cases, it becomes a challenge when microservices are serving high request throughput.
There are other challenges when aggregating audit information generated from hundreds of microservices.
In such cases, there's a need to decouple audits from business flows yet guarantee eventual consistency and performance.
Auditor-v1 is an attempt to solve this.It offers a client library, and an app server that captures audit events at high throughput in distributed way.
<br/><br/>
### Highlights
- It's easily pluggable: A client library that can be integrated into any jvm application, directly or via spring boot starter module.
Client library offers simple one liner api for seamless integration.
- It's async: Auditing happens asynchronously on a separate thread pool with retries to ensure eventual consistency.
- It's efficient: Auditor uses [Project reactor](https://projectreactor.io/) behind the scene to utilize system's hardware efficiently.
- It's performant: Auditor uses [jackson-afterburner](https://github.com/FasterXML/jackson-modules-base/tree/master/afterburner) for serialization needs which is both fast and performant.
- It's scalable: It leverages [Kafka](https://kafka.apache.org/) as event streaming platform so benefit from the inherent scalability feature of kafka. 
- It's configurable: Highly configurable to meet different application custom needs ranging from logging, filtering, decorating(static data and dynamic templating) audit events.
 Supports both startup as well as runtime/dynamic configurations.
<br/><br/>
### Architecture
![image info](./docs/auditor-v1-architecture.png)
<br/><br/>
### Modules
The project is composed of following modules:
- [core](./core): Contains reusable code across other modules, primarily mappers, openapi spec auto-generated code etc.
- [client](./client): Contains API and implementation code for the auditor library that will get plugged into applications thats needs audit feature.
- [client-spring-boot-starter](./client-spring-boot-starter): It's a spring boot starter over client API(s). Useful for application that uses spring boot.
- [client-example](./client-example): Example application to showcase standalone client usage.
- [client-example-springboot](./client-example-springboot): Example application to showcase spring boot starter usage.
- [app](./app): Contains deployable app that will listen to kafka and inserts the audit events and logs to elastic search.
<br/><br/>
### Integration:
You will have to point the maven/gradle repo to Maven release repository: link<//todo-will be updated once the maven central is integrated> to download any of the client libraries.
##### Gradle:
```
repositories {
    maven {
        url = uri("<<//todo Maven repo link here-will be updated once the maven central is integrated>>")
    }
}
```
##### Maven:
```
<repositories>
    <repository>
      <id>//todo artifactory-id</id>
      <name>//todo artifactory-name</name>
      <url>//todo artifiactor-url</url>
    </repository>
  </repositories>
```
<br/><br/>
### Using auditor-v1 client library
It can be integrated in either of following two ways.
- #### Standalone mode:
   Refer the [client module's README.md](./client/README.md) for how to use the client in standalone mode
- #### Spring boot starter mode:
    Spring boot starter module is built for applications that uses spring boot framework and comes with additional benefit of pre-initialized `Auditor` instance.
    Refer the [client-spring-boot-starter module's README.md](./client-spring-boot-starter/README.md) for how to use the spring-boot-starter mode
 <br/><br/>
### Examples: Integration & Configurations
you can refer to the example modules:
- [client-example](./client-example),
- [client-example-springboot](./client-example-springboot)

on how to integrate with auditor client APIs.
Auditor client configurations: [AuditorEventConfig.kt](./client/src/main/kotlin/com/lowes/auditor/client/entities/domain/AuditorEventConfig.kt)
can be provided both at startup time and during runtime.
You can also refer the `application.yml` in example modules to understand how to use different configurations at startup time.
Runtime configurations can be also seen in examples modules code.

Refer the [client-spring-boot-starter's README.md](./client-spring-boot-starter/README.md) to see full configuration options.
<br/><br/>
 
### Using auditor-v1 app server
Auditor server is present inside `app` module. Refer the [app module's README.md](./app/README.md) for how to use the app server.
<br/><br/>
### Contributing: local-setup, development and testing:

#### Setup
All commands mentioned below are expected to be run from your project root location.

After cloning the repository in your local, you need to run the following command to install `ktlintFormat` git pre-commit hook:
```
./gradlew addKtlintFormatGitPreCommitHook
```
This ensures the files are formatted before checking in.

#### Build and test
To build the project, you need to run the following:
```
 ./gradlew clean build
``` 
This will build and test all the modules(core, client, app, etc).
If you need to run test specifically, run following commands as per your needs:
```
./gradlew clean test -> Runs all unit tests
./gradlew clean integrationTest -> Runs all integrataion tests
./gradlew clean functionalTest -> Runs all functional tests
```

#### Adding/Updating dependencies
We are using [refreshVersions](https://github.com/jmfayard/refreshVersions) to manage version upgrades.
When you add a new dependency, please run the following:
```
./gradlew refreshVersionsMigrate
```
This will migrate the versions to [versions.properties](./versions.properties).
To upgrade a specific dependency or all dependencies, run the following:
```
./gradlew refreshVersions
```
Above command will fetch the latest versions of all dependencies used in the project.
you should pick the appropriate version to upgrade to. It can be done by copying the given version under comments and putting it against the said dependency property key.

#### Pull requests
Pull requests are always welcomed!
Once your local setup is completed, and you have tested out your changes,
you can raise PR against `main` branch.

For any queries/community support reach out to stack overflow channel: <//todo>


#### Running the app sever
Refer the [app module's README.md](./app/README.md) for how to run the app server locally
