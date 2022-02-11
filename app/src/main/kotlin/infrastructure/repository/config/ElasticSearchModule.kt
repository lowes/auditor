package com.lowes.auditor.app.infrastructure.repository.config

import org.elasticsearch.client.RestHighLevelClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.RestClients
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * Connecting to the Elasticsearch using [RestHighLevelClient] and setting custom WritingConverter for handling timestamp conversion
 * by overriding [ElasticsearchCustomConversions] conversion
 *
 * @property elasticSearchConfig instance of [ElasticsearchConfig]
 * @see AbstractElasticsearchConfiguration
 */
@Configuration(proxyBeanMethods = false)
class ElasticSearchModule(
    private val elasticSearchConfig: ElasticsearchConfig
) : AbstractElasticsearchConfiguration() {

    /**
     * custom method to convert OffsetDateTime to string
     *
     */
    @WritingConverter
    internal class TimeConverts : Converter<OffsetDateTime, String> {
        override fun convert(source: OffsetDateTime): String? {
            return source.format(DateTimeFormatter.ISO_DATE_TIME)
        }
    }

    /**
     * Elasticsearch custom conversions for WritingConverter and ReadingConverter
     * @return ElasticsearchCustomConversions
     * @see AbstractElasticsearchConfiguration.elasticsearchCustomConversions
     */
    @Bean
    override fun elasticsearchCustomConversions(): ElasticsearchCustomConversions {
        return ElasticsearchCustomConversions(
            listOf(TimeConverts())
        )
    }

    /**
     * Initializing RestHighLevelClient with given ClientConfiguration
     * @return RestHighLevelClient
     * @see AbstractElasticsearchConfiguration.elasticsearchClient
     */
    @Bean
    override fun elasticsearchClient(): RestHighLevelClient {
        val clientConfiguration = ClientConfiguration.builder()
            .connectedTo(elasticSearchConfig.clusterUrl.orEmpty())
            .withConnectTimeout(elasticSearchConfig.timeouts.connect)
            .withSocketTimeout(elasticSearchConfig.timeouts.socket)
            .build()
        return RestClients.create(clientConfiguration).rest()
    }
}
