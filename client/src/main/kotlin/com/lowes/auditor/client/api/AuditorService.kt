package com.lowes.auditor.client.api

import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.entities.util.orDefault
import com.lowes.auditor.client.library.service.AuditEventGeneratorService
import reactor.core.scheduler.Scheduler
import reactor.util.context.Context
import reactor.util.context.ContextView

internal class AuditorService(
    private val auditEventGeneratorService: AuditEventGeneratorService,
    private val auditorServiceScheduler: Scheduler,
) : Auditor {

    override fun audit(oldObject: Any?, newObject: Any?) {
        audit(oldObject, newObject, null, null)
    }

    override fun audit(oldObject: Any?, newObject: Any?, context: ContextView?) {
        audit(oldObject, newObject, null, context)
    }

    override fun audit(oldObject: Any?, newObject: Any?, auditorEventConfig: AuditorEventConfig?) {
        audit(oldObject, newObject, auditorEventConfig, null)
    }

    override fun audit(oldObject: Any?, newObject: Any?, auditorEventConfig: AuditorEventConfig?, context: ContextView?) {
        auditEventGeneratorService.audit(oldObject, newObject, auditorEventConfig)
            .contextWrite { it.putAll(context.orDefault(Context.empty())) }
            .subscribeOn(auditorServiceScheduler)
            .then()
            .subscribe()
    }
}
