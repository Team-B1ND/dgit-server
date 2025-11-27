package dodam.b1nd.dgit.global.client

import dodam.b1nd.dgit.domain.auth.dto.external.*
import dodam.b1nd.dgit.global.config.DAuthProperties
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class DAuthClient(
    private val webClientBuilder: WebClient.Builder,
    private val dAuthProperties: DAuthProperties
) {

    companion object {
        private const val DAUTH_BASE_URL = "https://dauth.b1nd.com/api"
        private const val OPENAPI_BASE_URL = "https://opendodam.b1nd.com/api"
    }

    /**
     * Authorization Code를 DAuth AccessToken으로 교환
     */
    fun exchangeCodeForToken(code: String): String {
        val webClient = webClientBuilder
            .baseUrl(DAUTH_BASE_URL)
            .build()

        val request = DAuthTokenRequest(
            code = code,
            client_id = dAuthProperties.clientId,
            client_secret = dAuthProperties.clientSecret
        )

        val response = webClient.post()
            .uri("/token")
            .bodyValue(request)
            .retrieve()
            .bodyToMono<DAuthTokenResponse>()
            .block() ?: throw IllegalStateException("DAuth 토큰 발급 실패")

        return response.accessToken
    }

    /**
     * DAuth AccessToken으로 사용자 정보 조회
     */
    fun getUserInfo(accessToken: String): UserInfo {
        val webClient = webClientBuilder
            .baseUrl(OPENAPI_BASE_URL)
            .build()

        val response = webClient.get()
            .uri("/user")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .bodyToMono<OpenApiResponse>()
            .block() ?: throw IllegalStateException("사용자 정보 조회 실패")

        return response.data
    }
}
