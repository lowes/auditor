package com.lowes.auditor.app.infrastructure.repository.config

import com.lowes.auditor.app.entities.util.DOT
import com.lowes.auditor.app.entities.util.EIGHT
import com.lowes.auditor.app.entities.util.ELASTIC_SEARCH
import com.lowes.auditor.app.entities.util.ONE
import com.lowes.auditor.app.entities.util.THIRTY
import com.lowes.auditor.core.entities.constants.AUDITOR
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.time.Duration

/**
 * Data class containing Elasticsearch related config
 *
 * @property clusterUrl instance of [String] in host:port format
 * @property timeouts instance of [Timeout]
 * @property connectionsPerRoute instance of [Int]
 * @property totalConnections instance of [Int]
 * @property username instance of [String]
 * @property password instance of [String]
 */
@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = AUDITOR.plus(DOT).plus(ELASTIC_SEARCH))
data class ElasticsearchConfig(
    var clusterUrl: String? = null,
    var timeouts: Timeout = Timeout(),
    var connectionsPerRoute: Int = EIGHT,
    var totalConnections: Int = THIRTY,
    var username: String? = null,
    var password: String? = null,
)

/**
 * Timeout configuration for elastic search
 *
 * @property connect instance of [Duration]
 * @property socket instance of [Duration]
 * @constructor Create empty Timeout
 */
@Configuration(proxyBeanMethods = false)
data class Timeout(
    var connect: Duration = Duration.ofSeconds(ONE.toLong()),
    var socket: Duration = Duration.ofSeconds(THIRTY.toLong()),
)
