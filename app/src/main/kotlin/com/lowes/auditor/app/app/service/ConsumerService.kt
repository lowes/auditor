package com.lowes.auditor.app.app.service

import com.lowes.auditor.app.entities.interfaces.infrastructure.event.EventSubscriber
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Service

/**
 * Subscribe to Kafka receiver on Application start up using [ApplicationListener] on ApplicationReadyEvent
 * @property eventSubscriber instance of [EventSubscriber]
 * @see ApplicationListener
 */
@Service
class ConsumerService(
    private val eventSubscriber: EventSubscriber,
) : ApplicationListener<ApplicationReadyEvent> {
    /**
     * On application event
     * @param event
     * @see ApplicationListener.onApplicationEvent
     */
    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        eventSubscriber.consumeEvents().subscribe()
    }
}
