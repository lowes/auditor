package com.lowes.auditor.core.entities.util

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.afterburner.AfterburnerModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

/**
 * Provides a default instance of JsonObject and its properties to be used across modules
 */
object JsonObject {
    /**
     * Returns a lazily initialized [ObjectMapper] singleton instance
     */
    val objectMapper: ObjectMapper by lazy {
        jacksonObjectMapper()
            .registerModules(AfterburnerModule(), JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
    }

    /**
     * Returns a lazily initialized [objectWriter] singleton instance
     */
    val objectWriter: ObjectWriter by lazy {
        objectMapper.writer()
    }
}
