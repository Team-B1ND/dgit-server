package dodam.b1nd.dgit.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
data class JwtProperties(
    var secretKey: String = ""
)
