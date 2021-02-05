package com.lowes.auditor.client.infrastructure.event.config

data class AuditEventProducerConfig(
    var enabled: Boolean? = false,
    var bootstrapServers: String? = null,
    var topic: String? = null,
    var configs: Map<String, String>? = null,
    internal val preConfiguredProducerConfig: Map<String, String> = mapOf(
        "retries" to "2147483647",
        "delivery.timeout.ms" to "240000",
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
    fun getMergedConfig(): Map<String, String> {
        return preConfiguredProducerConfig.plus(configs.orEmpty())
    }
}
