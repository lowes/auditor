package com.lowes.auditor.client.infrastructure.event.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.lowes.auditor.client.entities.interfaces.infrastructure.event.EventPublisher
import com.lowes.auditor.client.entities.util.JsonObject
import com.lowes.auditor.client.infrastructure.event.service.AuditEventProducerService
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG as PRODUCER_BOOTSTRAP

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AuditEventProducerConfig::class)
internal class AuditEventAutoConfigureModule(
    private val producerConfig: AuditEventProducerConfig
) {
    @Bean
    fun auditorObjectWriter(): ObjectWriter {
        return JsonObject.objectWriter
    }

    @Bean
    fun auditorObjectMapper(): ObjectMapper {
        return JsonObject.objectMapper
    }

    @Bean
    fun auditEventSender(): KafkaSender<String, String> {
        return KafkaSender.create(SenderOptions.create(configs(producerConfig)))
    }

    @Bean
    fun auditEventProducerService(
        auditEventSender: KafkaSender<String, String>,
        auditorObjectWriter: ObjectWriter
    ): EventPublisher {
        return AuditEventProducerService(producerConfig, auditEventSender, auditorObjectWriter)
    }

    fun configs(producerConfig: AuditEventProducerConfig?): Map<String, String?> {
        return mapOf(PRODUCER_BOOTSTRAP to producerConfig?.bootstrapServers)
            .plus(producerConfig?.configs ?: mapOf())
    }
}
