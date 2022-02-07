# Auditor-v1 client

### Api/Configurations
It offers primarily two flavours for auditing events. 
- `audit`: This api lets application pass two objects and perform an automatic data comparison of both objects. The observed differences are structured into an audit event message with decorated metadata and sent over to kafka. 
- `log`: This api lets application pass only a single object.
 The object is then serialized into json text and decorated with additional metadata and thereby converted to an audit message which is then sent over to kafka.

These api(s) also accepts following parameters which customizes the auditor behaviour while capturing audits:
- [auditorEventConfig](./src/main/kotlin/com/lowes/auditor/client/entities/domain/AuditorEventConfig.kt): Contains configurations for auditor.
- [context](https://projectreactor.io/docs/core/release/api/reactor/util/context/ContextView.html): Reactor's context containing relevant metadata, traceId etc
<br/><br/>

### Using auditor-v1 client library 
Refer the gradle/maven dependency detail below to use the client library in stand-alone mode:
   ##### Gradle:
   ```
   implementation("com.lowes.auditor:client:${version}")
   ```
   ##### Maven:
   ```
   <dependency>
        <groupId>com.lowes.auditor</groupId>
        <artifactId>client</artifactId>
        <version>${version}</version>
    </dependency>
   ``` 