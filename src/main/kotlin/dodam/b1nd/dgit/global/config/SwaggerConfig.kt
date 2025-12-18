package dodam.b1nd.dgit.global.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        val securitySchemeName = "Bearer Authentication"

        return OpenAPI()
            .info(
                Info()
                    .title("DGIT API")
                    .description("DGIT 백엔드 API 문서\n\n" +
                            "## 인증\n" +
                            "- Bearer Token (JWT) 방식\n" +
                            "- 로그인 후 받은 Access Token을 Authorization 헤더에 포함하여 요청\n\n" +
                            "## 주요 기능\n" +
                            "- GitHub 계정 연동 및 커밋 통계 조회\n" +
                            "- 사용자/레포지토리 랭킹 조회\n" +
                            "- 명예의 전당")
                    .version("v1.0.0")
            )
            .servers(
                listOf(
                    Server()
                        .url("https://dgitapi.b1nd.com")
                        .description("Production server"),
                    Server()
                        .url("http://localhost:8080")
                        .description("Local server")
                )
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .`in`(SecurityScheme.In.HEADER)
                            .name("Authorization")
                    )
            )
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
    }
}