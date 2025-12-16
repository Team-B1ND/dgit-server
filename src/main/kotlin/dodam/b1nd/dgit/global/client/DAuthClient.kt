package dodam.b1nd.dgit.global.client

import dodam.b1nd.dgit.domain.auth.dto.external.*
import dodam.b1nd.dgit.global.config.DAuthProperties
import dodam.b1nd.dgit.global.exception.CustomException
import dodam.b1nd.dgit.global.exception.ErrorCode
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class DAuthClient(
    private val webClientBuilder: WebClient.Builder,
    private val dAuthProperties: DAuthProperties
) {

    companion object {
        private const val DAUTH_BASE_URL = "https://dauthapi.b1nd.com"
    }

    /**
     * Authorization Code를 DAuth Token으로 교환
     */
    fun exchangeCodeForToken(code: String): DAuthTokenResponse {
        val webClient = webClientBuilder
            .baseUrl(DAUTH_BASE_URL)
            .build()

        val request = DAuthTokenRequest(
            code = code,
            clientSecret = dAuthProperties.clientSecret
        )

        val response = webClient.post()
            .uri("/oauth/token")
            .bodyValue(request)
            .retrieve()
            .bodyToMono<BasicResponse<DAuthTokenResponse>>()
            .block()?.data ?: throw CustomException(ErrorCode.INTERNAL_SERVER_ERROR)

        return response
    }

    /**
     * DAuth AccessToken으로 사용자 정보 조회
     */
    fun getUserInfo(accessToken: String): UserInfo {
        val webClient = webClientBuilder
            .baseUrl(DAUTH_BASE_URL)
            .build()

        println("asdsadasdsadasd: " + accessToken)

        val response = webClient.get()
            .uri("/oauth/userinfo")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .bodyToMono< BasicResponse<UserInfo>>()
            .block()?.data ?: throw CustomException(ErrorCode.INTERNAL_SERVER_ERROR)

        return response
    }
}
