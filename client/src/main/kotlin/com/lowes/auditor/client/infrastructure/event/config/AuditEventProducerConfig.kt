package com.lowes.auditor.client.infrastructure.event.config

/**
 * Data class containing configs related to kafka sender.
 * @property enabled [Boolean] flag to enable/disable the kafka sender
 * @property bootstrapServers comma separated list of host/port pairs of kafka brokers.
 * @property topic name of the kafka topic
 * @property configs additional configs to be used in kafka sender
 * @property preConfiguredProducerConfig  preconfigured configs to ensure high throughput and better resiliency. can be overridden by [configs]
 */
data class AuditEventProducerConfig(
    val enabled: Boolean? = false,
    val bootstrapServers: String? = null,
    val topic: String? = null,
    val configs: Map<String, String>? = null,
    val preConfiguredProducerConfig: Map<String, String> = mapOf(
        "retries" to "2147483647",
        "request.timeout.ms" to "300000",
        "delivery.timeout.ms" to "600000",
        "enable.idempotence" to "true",
        "acks" to "all",
        "linger.ms" to "100",
        "batch.size" to "102400",
        "compression.type" to "lz4",
        "buffer.memory" to "67108864",
        "key.serializer" to "org.apache.kafka.common.serialization.StringSerializer",
        "value.serializer" to "org.apache.kafka.common.serialization.StringSerializer",
        "max.block.ms" to "60000",
        "max.in.flight.requests.per.connection" to "5"
    )
) {
    /**
     * Provides merged view of [preConfiguredProducerConfig] and [configs].
     * In case same config is present in both, [configs] values takes precedence.
     *@return instance of [Map] containg merged kafka configs
     */
    fun getMergedConfig(): Map<String, String> {
        return preConfiguredProducerConfig.plus(configs.orEmpty())
    }
}
