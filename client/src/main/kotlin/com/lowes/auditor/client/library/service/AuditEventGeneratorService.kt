package com.lowes.auditor.client.library.service

import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.interfaces.infrastructure.event.EventPublisher
import com.lowes.auditor.client.entities.util.TEN
import com.lowes.auditor.client.entities.util.THIRTY
import com.lowes.auditor.client.entities.util.merge
import com.lowes.auditor.client.entities.util.orDefault
import com.lowes.auditor.client.usecase.ElementAggregatorUseCase
import com.lowes.auditor.client.usecase.EventLogUseCase
import com.lowes.auditor.client.usecase.LoggingFilterUseCase
import com.lowes.auditor.core.entities.domain.AuditEvent
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.util.retry.RetryBackoffSpec
import reactor.util.retry.RetrySpec
import java.time.Duration

class AuditEventGeneratorService(
    private val elementAggregatorUseCase: ElementAggregatorUseCase,
    private val initialConfig: AuditorEventConfig,
    private val eventPublisher: EventPublisher,
    private val auditEventFilterService: AuditEventFilterService,
    private val auditEventDecoratorService: AuditEventDecoratorService,
    private val eventLogUseCase: EventLogUseCase,
) {
    private val logger = LoggerFactory.getLogger(LoggingFilterUseCase::class.java)

    fun audit(oldObject: Any?, newObject: Any?, requestConfig: AuditorEventConfig?): Flux<AuditEvent> {
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

    fun log(entity: Any, requestConfig: AuditorEventConfig?): Flux<AuditEvent> {
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

    private fun doRetry(config: AuditorEventConfig, message: String): RetryBackoffSpec {
        return RetrySpec.fixedDelay(
            config.retry?.count.orDefault(TEN.toLong()),
            config.retry?.delay.orDefault(Duration.ofSeconds(THIRTY.toLong()))
        )
            .doBeforeRetry {
                logger.info(
                    "$message, hence retrying. exception:{}, retryCount:{}",
                    it.failure(),
                    it.totalRetries()
                )
            }
    }
}
