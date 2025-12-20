package dodam.b1nd.dgit.infrastructure.client

import dodam.b1nd.dgit.presentation.auth.dto.external.*
import dodam.b1nd.dgit.infrastructure.config.DAuthProperties
import dodam.b1nd.dgit.infrastructure.exception.CustomException
import dodam.b1nd.dgit.infrastructure.exception.ErrorCode
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
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

    fun exchangeCodeForToken(code: String, redirectUri: String = ""): DAuthTokenResponse {
        val webClient = webClientBuilder
            .baseUrl(DAUTH_BASE_URL)
            .build()

        val formData = LinkedMultiValueMap<String, String>()
        formData.add("grant_type", "authorization_code")
        formData.add("code", code)
        formData.add("client_id", dAuthProperties.clientId)
        formData.add("client_secret", dAuthProperties.clientSecret)
        if (redirectUri.isNotEmpty()) {
            formData.add("redirect_uri", redirectUri)
        }

        val response = webClient.post()
            .uri("/oauth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(formData))
            .retrieve()
            .bodyToMono<DAuthTokenResponse>()
            .block() ?: throw CustomException(ErrorCode.INTERNAL_SERVER_ERROR)

        return response
    }

    fun getUserInfo(accessToken: String): UserInfo {
        val webClient = webClientBuilder
            .baseUrl(DAUTH_BASE_URL)
            .build()

        try {
            return webClient.get()
                .uri("/userinfo")
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .bodyToMono<UserInfo>()
                .block() ?: throw CustomException(ErrorCode.INTERNAL_SERVER_ERROR)
        } catch (e: Exception) {
            val response = webClient.get()
                .uri("/oauth/userinfo")
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .bodyToMono<BasicResponse<UserInfo>>()
                .block()?.data ?: throw CustomException(ErrorCode.INTERNAL_SERVER_ERROR)

            return response
        }
    }
}
