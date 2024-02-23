package com.lowes.auditor.app.infrastructure.repository.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchConfiguration
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * Connecting to the Elasticsearch using [ReactiveElasticsearchConfiguration] and setting custom WritingConverter for handling timestamp conversion
 * by overriding [ElasticsearchCustomConversions] conversion
 *
 * @property elasticSearchConfig instance of [ElasticsearchConfig]
 * @see ReactiveElasticsearchConfiguration
 */
@Configuration(proxyBeanMethods = false)
class ElasticSearchModule(
    private val elasticSearchConfig: ElasticsearchConfig,
) : ReactiveElasticsearchConfiguration() {
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
     * @see ElasticsearchCustomConversions
     */
    @Bean
    override fun elasticsearchCustomConversions(): ElasticsearchCustomConversions {
        return ElasticsearchCustomConversions(
            listOf(TimeConverts()),
        )
    }

    override fun clientConfiguration(): ClientConfiguration {
        var clientConfiguration =
            ClientConfiguration
                .builder()
                .connectedTo(elasticSearchConfig.clusterUrl.orEmpty())
                .withConnectTimeout(elasticSearchConfig.timeouts.connect)
                .withSocketTimeout(elasticSearchConfig.timeouts.socket)
        clientConfiguration = elasticSearchConfig.username?.let {
            elasticSearchConfig.password?.let { it1 ->
                clientConfiguration.withBasicAuth(it, it1)
            }
        } ?: clientConfiguration
        return clientConfiguration.build()
    }
}
