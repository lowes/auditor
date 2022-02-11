package com.lowes.auditor.app.infrastructure.repository.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Data class to initialize repository configurations.
 * @property auditEventIndexPrefix prefix to be used to generate index/table name while saving the audit event data
 * @property auditLogIndexAlias Alias to be used to generate index/table name while saving the audit log data
 */
@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = "repository")
data class RepositoryConfig(
    var auditEventIndexPrefix: String = "item-",
    var auditLogIndexAlias: String = "log-item-rollover-alias"
)
