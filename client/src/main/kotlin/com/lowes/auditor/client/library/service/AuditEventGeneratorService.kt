package com.lowes.auditor.client.library.service

import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.interfaces.infrastructure.event.EventPublisher
import com.lowes.auditor.client.entities.util.TEN
import com.lowes.auditor.client.entities.util.THIRTY
import com.lowes.auditor.client.entities.util.ZERO
import com.lowes.auditor.client.entities.util.merge
import com.lowes.auditor.client.entities.util.orDefault
import com.lowes.auditor.client.usecase.ElementAggregatorUseCase
import com.lowes.auditor.client.usecase.EventLogUseCase
import com.lowes.auditor.core.entities.domain.AuditEvent
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.util.retry.Retry
import reactor.util.retry.RetrySpec
import java.time.Duration

/**
 * Audit event generator service that generates audits and logs and publishes them to underlying event publisher
 * @property elementAggregatorUseCase instance of [ElementAggregatorUseCase]
 * @property initialConfig instance of [AuditorEventConfig] configured during startup
 * @property eventPublisher instance of [EventPublisher]
 * @property auditEventFilterService instance of [AuditEventFilterService]
 * @property auditEventDecoratorService instance of [AuditEventDecoratorService]
 * @property eventLogUseCase instance of [EventLogUseCase]
 */
class AuditEventGeneratorService(
    private val elementAggregatorUseCase: ElementAggregatorUseCase,
    private val initialConfig: AuditorEventConfig,
    private val eventPublisher: EventPublisher,
    private val auditEventFilterService: AuditEventFilterService,
    private val auditEventDecoratorService: AuditEventDecoratorService,
    private val eventLogUseCase: EventLogUseCase,
) {
    private val logger = LoggerFactory.getLogger(AuditEventGeneratorService::class.java)

    /**
     * Generates audits by comparing [oldObject] and [newObject] properties and decorates them with metadata based on [requestConfig]
     * @param oldObject instance of [Any]
     * @param newObject instance of [Any]
     * @param requestConfig instance of [AuditorEventConfig]
     * @return flux of generated [AuditEvent]
     */
    fun audit(
        oldObject: Any?,
        newObject: Any?,
        requestConfig: AuditorEventConfig?,
    ): Flux<AuditEvent> {
        val config = requestConfig?.let { initialConfig merge it } ?: initialConfig
        val events = elementAggregatorUseCase.aggregate(oldObject, newObject, config)
        val decoratedEvents = auditEventDecoratorService.decorate(events, newObject)
        val filteredEvents = auditEventFilterService.filter(decoratedEvents, config)
        val logMessage = "audit event failed"
        return eventPublisher.publishEvents(filteredEvents)
            .retryWhen(doRetry(config, logMessage))
            .doOnError { logger.error(logMessage, it) }
            .flatMap { events }
    }

    /**
     * Generates logs by logging current [entity] while decorating it with metadata based on [requestConfig]
     * @param entity instance of [Any] that needs to be logged
     * @param requestConfig instance of [AuditorEventConfig]
     * @return flux of generated [AuditEvent]
     */
    fun log(
        entity: Any,
        requestConfig: AuditorEventConfig?,
    ): Flux<AuditEvent> {
        val config = requestConfig?.let { initialConfig merge it } ?: initialConfig
        val events = Flux.from(eventLogUseCase.logEvent(entity, config))
        val decoratedEvents = auditEventDecoratorService.decorate(events, entity)
        val filteredEvents = auditEventFilterService.filter(decoratedEvents, config)
        val logMessage = "audit log event failed"
        return eventPublisher.publishEvents(filteredEvents)
            .retryWhen(doRetry(config, logMessage))
            .doOnError { logger.error(logMessage, it) }
            .flatMap { events }
    }

    /**
     * Performs retry in case any of the chain fails. In most cases this is not required as kafka producer is expected to perform retries.
     * This is especially used during some edge use cases(when kafka/network is flaky and when kafka producer does not retry)
     */
    private fun doRetry(
        config: AuditorEventConfig,
        message: String,
    ): Retry {
        return if (config.retry?.enabled == true) {
            RetrySpec.fixedDelay(
                config.retry?.count.orDefault(TEN.toLong()),
                config.retry?.delay.orDefault(Duration.ofSeconds(THIRTY.toLong())),
            )
                .doBeforeRetry {
                    logger.info(
                        "op:doRetry.FailureMessage:{}, exception:{}, retryCount:{}",
                        message,
                        it.failure(),
                        it.totalRetries(),
                    )
                }
        } else {
            RetrySpec.max(ZERO.toLong())
        }
    }
}
