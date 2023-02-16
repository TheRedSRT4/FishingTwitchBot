package dev.theredsrt4.fish

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming


@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Configuration(
    val debug: Boolean = false,
    val heartbeat: Boolean = false,
    val bot: Map<String, String> = mapOf(),
    val api: Map<String, String> = mapOf(),
    val credentials: Map<String,String> = mapOf(),
    val channels: List<String> = listOf(),
    val database: Map<String, String> = mapOf()
)
