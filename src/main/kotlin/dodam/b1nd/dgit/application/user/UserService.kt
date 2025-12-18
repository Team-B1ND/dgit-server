package dodam.b1nd.dgit.application.user

import dodam.b1nd.dgit.domain.user.entity.User
import dodam.b1nd.dgit.domain.user.repository.UserRepository
import dodam.b1nd.dgit.infrastructure.exception.CustomException
import dodam.b1nd.dgit.infrastructure.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository
) {

    /**
     * 이메일로 사용자 저장 또는 업데이트
     * - 기존 사용자가 있으면 dodamRefreshToken 업데이트
     * - 없으면 새로 생성
     */
    @Transactional
    fun saveOrUpdate(user: User): User {
        val existingUser = userRepository.findByEmail(user.email)
        return if (existingUser != null) {
            existingUser.dodamRefreshToken = user.dodamRefreshToken
            userRepository.save(existingUser)
        } else {
            userRepository.save(user)
        }
    }

    /**
     * 이메일로 사용자 조회
     */
    fun getUserByEmail(email: String): User {
        return userRepository.findByEmail(email)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)
    }
}
