package dodam.b1nd.dgit.domain.user.service

import dodam.b1nd.dgit.domain.user.entity.User
import dodam.b1nd.dgit.domain.user.repository.UserRepository
import dodam.b1nd.dgit.global.exception.CustomException
import dodam.b1nd.dgit.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository
) {

    /**
     * 이메일로 사용자 저장 또는 업데이트
     * - 기존 사용자가 있으면 반환 (업데이트하지 않음)
     * - 없으면 새로 생성
     */
    @Transactional
    fun saveOrUpdate(user: User): User {
        return userRepository.findByEmail(user.email)
            ?: userRepository.save(user)
    }

    /**
     * 이메일로 사용자 조회
     */
    fun getUserByEmail(email: String): User {
        return userRepository.findByEmail(email)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)
    }
}
