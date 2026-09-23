package org.example.nosql_lab1.controller.advice

import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(OptimisticLockingFailureException::class)
    fun handleOptimisticLock(): ProblemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            "событие на прошло - КОНФЛИКТ ЗАПИСИ... попробуйте еще...",
        )
}
