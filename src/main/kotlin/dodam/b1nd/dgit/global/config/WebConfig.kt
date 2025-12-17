package dodam.b1nd.dgit.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebConfig {

    @Bean
    fun webClientBuilder(): WebClient.Builder {
        // REST API 사용 시 커밋 데이터가 큰 경우를 대비해 버퍼 크기 16MB로 증가
        val exchangeStrategies = ExchangeStrategies.builder()
            .codecs { configurer: ClientCodecConfigurer ->
                configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) // 16MB
            }
            .build()

        return WebClient.builder()
            .exchangeStrategies(exchangeStrategies)
    }
}
