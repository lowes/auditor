package com.lowes.auditor.app.infrastructure.event.config

import com.lowes.auditor.app.entities.util.DOT
import com.lowes.auditor.app.entities.util.EVENT
import com.lowes.auditor.core.entities.constants.AUDITOR
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Data class containing Audit event related config
 *
 * @property kafkaConsumer instance [AuditEventConsumerConfig]
 */
@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = AUDITOR.plus(DOT).plus(EVENT))
data class AuditEventConfig(
    val kafkaConsumer: AuditEventConsumerConfig = AuditEventConsumerConfig(),
)

/**
 * Data class containing configs related to kafka receiver. Property values are kept as var for easy java interoperability.
 * @property enabled [Boolean] flag to enable/disable the kafka receiver
 * @property bootstrapServers comma separated list of host/port pairs of kafka brokers.
 * @property topic name of the kafka topic
 * @property configs additional configs to be used in kafka receiver
 * @property preConfiguredConsumerConfig  preconfigured configs to ensure high throughput and better resiliency. can be overridden by [configs]
 */
@Configuration(proxyBeanMethods = false)
data class AuditEventConsumerConfig(
    var enabled: Boolean? = false,
    var bootstrapServers: String? = null,
    var topic: String? = null,
    var configs: Map<String, String>? = null,
    val preConfiguredConsumerConfig: Map<String, String> =
        mapOf(
            "enable.auto.commit" to "false",
            "fetch.min.bytes" to "102400",
            "session.timeout.ms" to "60000",
            "heartbeat.interval.ms" to "10000",
            "auto.offset.reset" to "earliest",
            "key.deserializer" to "org.apache.kafka.common.serialization.StringDeserializer",
            "value.deserializer" to "org.apache.kafka.common.serialization.StringDeserializer",
        ),
) {
    /**
     * Provides merged view of [preConfiguredConsumerConfig] and [configs].
     * In case same config is present in both, [configs] values takes precedence.
     *@return instance of [Map] containing merged kafka configs
     */
    fun getMergedConfig(): Map<String, String> {
        return preConfiguredConsumerConfig.plus(configs.orEmpty())
    }
}
