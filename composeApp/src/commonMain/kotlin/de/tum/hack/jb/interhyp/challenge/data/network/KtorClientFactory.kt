package de.tum.hack.jb.interhyp.challenge.data.network

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Factory object for creating configured Ktor HTTP client.
 * Includes ContentNegotiation with JSON serialization and logging.
 */
object KtorClientFactory {
    
    /**
     * Create and configure a Ktor HttpClient instance
     */
    fun create(): HttpClient {
        return HttpClient {
            // Content Negotiation for JSON serialization
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }
            
            // Logging plugin for debugging
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }
        }
    }
}
