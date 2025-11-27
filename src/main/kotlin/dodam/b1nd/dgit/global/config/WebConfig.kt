package dodam.b1nd.dgit.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebConfig {

    @Bean
    fun webClientBuilder(): WebClient.Builder {
        return WebClient.builder()
    }
}
