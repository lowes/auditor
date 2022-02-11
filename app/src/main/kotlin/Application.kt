package com.lowes.auditor.app

import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder

@EnableAutoConfiguration
@SpringBootApplication
open class Application

/**
 * Runs the app server
 */
fun main(args: Array<String>) {
    SpringApplicationBuilder(Application::class.java)
        .bannerMode(Banner.Mode.OFF)
        .build()
        .run(*args)
}
