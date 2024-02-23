package com.lowes.auditor.client.listeners

import io.kotest.core.listeners.ProjectListener
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.admin.NewTopic
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration

/**
 * Kafka listener to initialise the Kafka test container
 */
object KafkaListener : ProjectListener {
    const val TOPIC = "auditorFunctionalTestTopic"

    /**
     * lazily initiated instance of Kafka Test Container
     */
    val cluster by lazy {
        KafkaContainer(
            DockerImageName
                .parse("confluentinc/cp-kafka:5.2.1")
                .asCompatibleSubstituteFor("confluentinc/cp-kafka:5.2.1"),
        )
            .withReuse(true)
            .withLabel("application", "auditor-client-functional-test")
            .withStartupTimeout(Duration.ofSeconds(120))
            .withStartupAttempts(3)
    }

    override suspend fun beforeProject() {
        cluster.start()
        createTopics(TOPIC)
    }

    override suspend fun afterProject() {
        cluster.stop()
    }

    /**
     * Method to create topics
     * @param topics Name of the Kafka Topic to create
     *
     */
    private fun createTopics(vararg topics: String) {
        val topicsToCreate =
            arrayOf(*topics).map {
                NewTopic(it, 1, 1)
            }
        val adminClient =
            AdminClient.create(
                mapOf(
                    BOOTSTRAP_SERVERS_CONFIG to cluster.bootstrapServers,
                ),
            )
        adminClient.createTopics(topicsToCreate)
        adminClient.close()
    }
}
