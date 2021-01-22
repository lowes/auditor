package foo

import com.lowes.auditor.core.infrastructure.event.model.AuditEventDTO
import com.lowes.iss.springboot.coremodule.entities.utilities.info
import com.lowes.iss.springboot.kafkamodule.entities.interfaces.api.KafkaConsumer
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import javax.inject.Named

@Named
class Consumer : KafkaConsumer<AuditEventDTO> {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun consume(message: AuditEventDTO): Mono<AuditEventDTO> {
        logger.info {
            op("consume")
            message("consumed message")
            kv("message", message.toString())
        }
        return Mono.just(message)
    }

    override fun consumeError(error: Throwable): Mono<Void> {
        return Mono.empty()
    }

    override fun getConsumerName(): String {
        return "auditor-consumer"
    }
}
