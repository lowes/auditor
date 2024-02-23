package com.lowes.auditor.app.infrastructure.event.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.lowes.auditor.core.entities.util.JsonObject
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import reactor.kafka.receiver.ReceiverOptions
import org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG as CONSUMER_BOOTSTRAP

/**
 * Initializes and configures underlying kafka receiver to be used to send audit events
 * @property eventConfig instance of [AuditEventConfig]
 */
@Configuration(proxyBeanMethods = false)
class AuditEventConsumerModule(
    private val eventConfig: AuditEventConfig,
) {
    /**
     * Gets the object mapper instance. primarily used for index generation
     */
    @Bean
    @Primary
    fun objectMapper(): ObjectMapper {
        return JsonObject.objectMapper
    }

    /**
     * Provides bean instance of [ReceiverOptions] used for sending audit events.
     */
    @Bean
    fun auditEventReceiver(): ReceiverOptions<String, String> {
        return ReceiverOptions.create(configs(eventConfig.kafkaConsumer))
    }

    /**
     * Provides configs to underlying kafka receiver
     * @param consumerConfig instance of [AuditEventConsumerConfig]
     * @return instance of [Map] containing kafka configs
     */
    private fun configs(consumerConfig: AuditEventConsumerConfig?): Map<String, String> {
        return mapOf(CONSUMER_BOOTSTRAP to consumerConfig?.bootstrapServers.orEmpty())
            .plus(consumerConfig?.getMergedConfig().orEmpty())
    }
}
