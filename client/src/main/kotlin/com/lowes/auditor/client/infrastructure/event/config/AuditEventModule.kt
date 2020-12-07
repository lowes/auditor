package com.lowes.auditor.client.infrastructure.event.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG as PRODUCER_BOOTSTRAP

@Configuration(proxyBeanMethods = false)
internal class AuditEventModule constructor(
    private val producerConfig: AuditEventProducerConfig?
) {
    @Bean
    fun auditEventSender(): KafkaSender<String, String> {
        return KafkaSender.create(SenderOptions.create(configs(producerConfig)))
    }

    fun configs(producerConfig: AuditEventProducerConfig?): Map<String, String?> {
        return mapOf(PRODUCER_BOOTSTRAP to producerConfig?.bootstrapServers)
            .plus(producerConfig?.configs ?: mapOf())
    }
}
