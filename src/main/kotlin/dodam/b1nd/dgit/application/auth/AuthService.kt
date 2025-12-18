package dodam.b1nd.dgit.application.auth

import dodam.b1nd.dgit.infrastructure.client.DAuthClient
import dodam.b1nd.dgit.presentation.auth.dto.external.DAuthTokenResponse
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val dAuthClient: DAuthClient
) {

    fun exchangeCodeForToken(code: String): DAuthTokenResponse {
        return dAuthClient.exchangeCodeForToken(code)
    }

    fun getUserInfo(accessToken: String) = dAuthClient.getUserInfo(accessToken)
}
