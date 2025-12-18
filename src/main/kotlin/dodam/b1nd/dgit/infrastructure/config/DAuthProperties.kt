package dodam.b1nd.dgit.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "app.oauth")
data class DAuthProperties(
    var clientId: String = "",
    var clientSecret: String = ""
)
