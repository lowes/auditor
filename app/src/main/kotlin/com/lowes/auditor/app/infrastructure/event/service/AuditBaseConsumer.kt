package com.lowes.auditor.app.infrastructure.event.service

import com.lowes.auditor.app.entities.interfaces.infrastructure.repository.RepositoryService
import com.lowes.auditor.core.entities.util.AuditEventMapper
import com.lowes.auditor.core.infrastructure.event.model.AuditEventDTO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Base class for consuming audit data from kafka
 * @property repositoryService instance of [RepositoryService]
 */
@Service
class AuditBaseConsumer(
    private val repositoryService: RepositoryService
) {
    private val logger = LoggerFactory.getLogger(AuditBaseConsumer::class.java)

    /**
     * Consume messages from kafka and save to underlying repository
     * @param auditEventDTO of type [AuditEventDTO]
     * @return mono of [AuditEventDTO]
     */
    fun consumeMessage(auditEventDTO: AuditEventDTO): Mono<AuditEventDTO> {
        return repositoryService.save(AuditEventMapper.toAuditEvent(auditEventDTO))
            .doOnNext {
                logger.info("inserted message into elastic search successfully")
            }.thenReturn(auditEventDTO)
    }
}
