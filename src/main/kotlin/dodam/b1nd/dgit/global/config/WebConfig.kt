package dodam.b1nd.dgit.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebConfig {

    @Bean
    fun webClientBuilder(): WebClient.Builder {
        // GraphQL 사용으로 응답 크기 99% 감소 → 기본 버퍼(256KB)로 충분
        return WebClient.builder()
    }
}
