package com.example.demo

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.coRouter

@Configuration("demoRouter")
class DevRouter : ValidationRouter("/demo") {
    @Bean
    fun router() = coRouter {
        basePath.nest {
            GETV("/hello") {
                object {
                    val hello = "world"
                }
            }
            POSTV("/hello") { _, body ->
                object {
                    val hello = body["hello"]
                }
            }
        }
    }
}
