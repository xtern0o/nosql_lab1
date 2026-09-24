package org.example.nosql_lab1.service

import org.example.nosql_lab1.dto.user.request.UpsertUserRequest
import org.example.nosql_lab1.dto.user.response.UserResponse
import org.example.nosql_lab1.entity.User
import org.example.nosql_lab1.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    @Transactional(readOnly = true)
    fun getById(id: UUID): UserResponse =
        findUser(id).toResponse()

    @Transactional(readOnly = true)
    fun getByEmail(email: String): UserResponse =
        userRepository.findByEmailIgnoreCase(email.trim())?.toResponse()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден")

    @Transactional
    fun upsert(request: UpsertUserRequest): UserResponse {
        val normalizedEmail = request.email.trim().lowercase()
        val existingByEmail = userRepository.findByEmailIgnoreCase(normalizedEmail)

        val user = if (request.id == null) {
            if (existingByEmail != null) {
                throw ResponseStatusException(HttpStatus.CONFLICT, "ЕМЕИЛ используется")
            }

            User()
        } else {
            findUser(request.id).also {
                if (existingByEmail != null && existingByEmail.id != it.id) {
                    throw ResponseStatusException(HttpStatus.CONFLICT, "ЕМЕИЛ используется")
                }
            }
        }

        user.name = request.name.trim()
        user.email = normalizedEmail
        user.role = request.role

        return userRepository.save(user).toResponse()
    }

    private fun findUser(id: UUID): User =
        userRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден")
        }

    private fun User.toResponse() = UserResponse(
        id = requireNotNull(id),
        name = name,
        email = email,
        role = role,
    )
}
