package dodam.b1nd.dgit.global.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
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
                    .description("""
                        DGIT 백엔드 API 문서

                        ## 주요 기능
                        - GitHub 계정 연동 및 커밋 데이터 수집
                        - 사용자 랭킹 시스템 (커밋 수, 스트릭, 레포지토리)
                        - DAuth 기반 인증/인가

                        ## GitHub 데이터 수집
                        - **GraphQL API 사용**: GitHub REST API 대비 99% 응답 크기 감소 (약 300배 효율적)
                        - **실시간 수집**: 계정 등록 즉시 데이터 수집
                        - **자동 갱신**: 매시간(07:00-23:00) 자동 데이터 갱신

                        ## 인증 방식
                        - Bearer Token (JWT) 사용
                        - `/auth/login` 엔드포인트를 통해 토큰 발급
                    """.trimIndent())
                    .version("v1.0.0")
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