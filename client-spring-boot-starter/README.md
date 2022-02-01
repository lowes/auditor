# Auditor-v1 client-spring-boot-starter
It's a spring boot starter over client api(s). Useful for application that uses spring boot.

## Configurations
Auditor client can be customized via `AuditorEventConfig` class either during start-up or during runtime invocation of auditor's audit/log api(s).
During startup time following configurations can be added to application's yml:

```
auditor:
  config:
    applicationName: "client-example"         # name of the application that will be sent as part of audit. Default value: NOT_CONFIGURED
    eventSource:
      type: USER                              # source type of the audit event. Available values: SYSTEM,USER. Default value: SYSTEM
      metadata:
        id: "id" or "${rand.id}"              # id of the system/user causing the event. String substition can also be used.
        name: "name" or "${rand.name}"        # name of the system/user causing the event. String substition can also be used.
        email: "email" or "${rand.email}"     # email of the system/user causing the event. String substition can also be used.
    eventSubType: "subType"                   # subtype(free form text) of the audit event that will be sent as part of audit
    metadata:                                 # Additional metadata can be provided here. Any key value pair that needs to decorated with audit data. String substition can also be used.
      itemIdAnything: ${itemNumber}-AND-MODEL-${model}
      noId: NoFetchingHere!
    filters:
      event:                                  # filters audit events based on event type
        enabled: false                        # flag to enable/disable the feature. Default value: false
        type:
          - UPDATED                           # capture events based on this type, Available values: CREATED,UPDATED,DELETED. By default everything is captured
      element:                                # Include/Exclude elements filter based on type, Available values: InclusionFilter,ExclusionFilter
        enabled: false                        # flag to enable/disable the feature. Default value: false
        types:
          - InclusionFilter                   # Inclusion filter includes all elements which are configured in options sections
          - ExclusionFilter                   # Inclusion filter includes all elements which are configured in options sections
        options:
          excludes:                           # Matches each element name against each excludes list. If match is found, the given element will be excluded.
            - description
          includes:                           # Matches each element name against each excludes list. If match is found, the given element will be excluded.
            - model
            - price
      logging:                                # flag to enable/disable loggig filter. Default value: false
        enabled: true
    maxElements: 500                          # maximum number of elements to be audited, elements above this number will be ignored. Default value: 500
    retry:
      enabled: true                           # flag to enable/disable the publisher retry feature. Default value: true
      count: 10                               # number of times a retry will be attempted for failed event publish. Default value: 10
      delay: 30s                              # instance of [Duration] signifying delay between consecutive retry attempts. Default value: 30 seconds

  producer:
    enabled: true                             # enable/disable kafka producer. Default value: false
    bootstrapServers: "localhost:9092"        # comma separated list of host/port pairs of kafka brokers.
    topic: auditTopic                         # name of the kafka topic
    configs:                                  # additional configs to be used in kafka sender
      client.id: client-example
```