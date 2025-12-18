package dodam.b1nd.dgit.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "swagger")
data class SwaggerProperties(
    val username: String,
    val password: String
)
