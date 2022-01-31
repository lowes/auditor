package com.lowes.auditor.client.api

import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.util.orDefault
import com.lowes.auditor.client.library.service.AuditEventGeneratorService
import reactor.core.scheduler.Scheduler
import reactor.util.context.Context
import reactor.util.context.ContextView

/**
 * Default implementation class for [Auditor] interface.
 */
class AuditorService(
    private val auditEventGeneratorService: AuditEventGeneratorService,
    private val auditorServiceScheduler: Scheduler,
) : Auditor {

    /**
     * Performs an audit by comparing old and new objects
     * @param oldObject previous instance of the object to be compared against
     * @param newObject latest instance of the object that needs to be audited
     */
    override fun audit(oldObject: Any?, newObject: Any?) {
        audit(oldObject, newObject, null, null)
    }

    /**
     * Performs an audit by comparing old and new objects
     * @param oldObject previous instance of the object to be compared against
     * @param newObject latest instance of the object that needs to be audited
     * @param context an instance of [ContextView] containing context relevant metadata
     */
    override fun audit(oldObject: Any?, newObject: Any?, context: ContextView?) {
        audit(oldObject, newObject, null, context)
    }

    /**
     * Performs an audit by comparing old and new objects
     * @param oldObject previous instance of the object to be compared against
     * @param newObject latest instance of the object that needs to be audited
     * @param auditorEventConfig an instance of [AuditorEventConfig] containing audit related configurations
     */
    override fun audit(oldObject: Any?, newObject: Any?, auditorEventConfig: AuditorEventConfig?) {
        audit(oldObject, newObject, auditorEventConfig, null)
    }

    /**
     * Performs an audit by comparing old and new objects
     * @param oldObject previous instance of the object to be compared against
     * @param newObject latest instance of the object that needs to be audited
     * @param auditorEventConfig an instance of [AuditorEventConfig] containing audit related configurations
     * @param context an instance of [ContextView] containing context relevant metadata
     */
    override fun audit(oldObject: Any?, newObject: Any?, auditorEventConfig: AuditorEventConfig?, context: ContextView?) {
        auditEventGeneratorService.audit(oldObject, newObject, auditorEventConfig)
            .contextWrite { it.putAll(context.orDefault(Context.empty())) }
            .subscribeOn(auditorServiceScheduler)
            .then()
            .subscribe()
    }

    /**
     * Generates an audit log containing an entity
     * @param entity entity of type [Any] that needs to ve logged
     */
    override fun log(entity: Any) {
        log(entity, null, null)
    }

    /**
     * Generates an audit log containing an entity
     * @param entity entity of type [Any] that needs to ve logged
     * @param context an instance of [ContextView] containing context relevant metadata
     */
    override fun log(entity: Any, context: ContextView?) {
        log(entity, null, context)
    }

    /**
     * Generates an audit log containing an entity
     * @param entity entity of type [Any] that needs to ve logged
     * @param auditorEventConfig an instance of [AuditorEventConfig] containing audit related configurations
     */
    override fun log(entity: Any, auditorEventConfig: AuditorEventConfig?) {
        log(entity, auditorEventConfig, null)
    }

    /**
     * Generates an audit log containing an entity
     * @param entity entity of type [Any] that needs to ve logged
     * @param auditorEventConfig an instance of [AuditorEventConfig] containing audit related configurations
     * @param context an instance of [ContextView] containing context relevant metadata
     */
    override fun log(entity: Any, auditorEventConfig: AuditorEventConfig?, context: ContextView?) {
        auditEventGeneratorService.log(entity, auditorEventConfig)
            .contextWrite { it.putAll(context.orDefault(Context.empty())) }
            .subscribeOn(auditorServiceScheduler)
            .then()
            .subscribe()
    }
}
