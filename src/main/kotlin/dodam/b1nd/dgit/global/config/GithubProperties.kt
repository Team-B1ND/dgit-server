package dodam.b1nd.dgit.global.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app.github")
data class GithubProperties(
    var token: String = ""
)