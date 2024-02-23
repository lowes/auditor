package com.lowes.auditor.app.infrastructure.repository.service

import com.lowes.auditor.app.entities.interfaces.infrastructure.repository.RepositoryService
import com.lowes.auditor.app.infrastructure.repository.config.RepositoryConfig
import com.lowes.auditor.core.entities.domain.AuditEvent
import com.lowes.auditor.core.entities.util.AuditEventDTOMapper
import org.slf4j.LoggerFactory
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Implementation of [RepositoryService] interface. Provides capabilities to interact with underlying database.
 * @property repositoryConfig instance of [RepositoryConfig]
 * @property reactiveElasticsearchOperations instance of [ReactiveElasticsearchOperations]
 * @see RepositoryService
 */
@Service
class AuditEventRepositoryService(
    private val repositoryConfig: RepositoryConfig,
    private val reactiveElasticsearchOperations: ReactiveElasticsearchOperations,
) : RepositoryService {
    private val logger = LoggerFactory.getLogger(AuditEventRepositoryService::class.java)

    /**
     * Saves the [event] to elasticSearch
     * @param event instance of [AuditEvent]
     * @return mono of saved [AuditEvent]
     * @see RepositoryService.save
     */
    override fun save(event: AuditEvent): Mono<AuditEvent> {
        return Mono.just(event)
            .map { AuditEventDTOMapper.toAuditEventDTO(event) }
            .flatMap {
                saveIndex(getIndexName(event), event)
            }
    }

    /**
     * Gets the index name depending on the type of [event].
     * @param event instance of [AuditEvent]
     * @return audit log alias index if audit log is present, audit event index name otherwise
     */
    private fun getIndexName(event: AuditEvent): String {
        return if (!event.log.isNullOrEmpty()) {
            repositoryConfig.auditLogIndexAlias
        } else {
            repositoryConfig.auditEventIndexPrefix.plus(event.applicationName)
        }
    }

    /**
     * Save event to database with specified indexName
     *
     * @param indexName instance of [String] index for which the doc will be saved
     * @param doc audit event to persist in database
     * @return
     */
    private fun <DocType> saveIndex(
        indexName: String,
        doc: DocType,
    ): Mono<DocType> {
        return reactiveElasticsearchOperations.save(doc, IndexCoordinates.of(indexName))
            .doOnNext { logger.info("Record saved for index {}", indexName) }
    }
}
