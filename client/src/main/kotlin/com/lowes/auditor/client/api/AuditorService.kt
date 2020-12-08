package com.lowes.auditor.client.api

import com.lowes.auditor.client.entities.domain.AuditorEventConfig
import com.lowes.auditor.client.library.service.AuditEventGeneratorService
import reactor.core.scheduler.Schedulers
import javax.inject.Inject
import javax.inject.Named

@Named
internal class AuditorService @Inject constructor(
    private val auditEventGeneratorService: AuditEventGeneratorService
) : Auditor {
    override fun audit(oldObject: Any?, newObject: Any?, auditorEventConfig: AuditorEventConfig) {
        auditEventGeneratorService.audit(oldObject, newObject, auditorEventConfig)
            .subscribeOn(Schedulers.newParallel("AuditorScheduler"))
    }
}
