package org.example.nosql_lab1.controller

import jakarta.validation.Valid
import org.example.nosql_lab1.dto.user.request.UpsertUserRequest
import org.example.nosql_lab1.dto.user.response.UserResponse
import org.example.nosql_lab1.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
) {
    @GetMapping("/by-email")
    fun getByEmail(@RequestParam email: String): UserResponse = userService.getByEmail(email)

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): UserResponse = userService.getById(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid @RequestBody request: UpsertUserRequest
    ): UserResponse =
        userService.upsert(request.copy(id = null))

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpsertUserRequest,
    ): UserResponse = userService.upsert(request.copy(id = id))
}
