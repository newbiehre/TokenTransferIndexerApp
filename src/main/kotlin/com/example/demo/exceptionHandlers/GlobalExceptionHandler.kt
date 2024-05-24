package com.example.demo.exceptionHandlers

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Global exception handler for REST controllers.
 *
 * Handles different exception types and prepares a standardized ErrorResponse for the client.
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    private var logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * Handles any exception thrown by REST controllers.
     * Maps exception type to ErrorResponse with messages.
     *
     * @param ex Exception Thrown exception
     * @param request HttpServletRequest Request details
     * @return Response entity with exception details
     */
    @ExceptionHandler(Exception::class)
    fun handleExceptions(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        var status = HttpStatus.BAD_REQUEST
        val errorMessage = when (ex) {
            is MethodArgumentNotValidException -> ex.bindingResult.allErrors.map { error ->
                error.defaultMessage ?: "Invalid value"
            }

            is HttpMessageNotReadableException -> listOf(ex.localizedMessage ?: "Invalid value")
            else -> {
                status = HttpStatus.INTERNAL_SERVER_ERROR
                listOf(ex.localizedMessage ?: "Unexpected error")
            }
        }
        val errorResponse = ErrorResponse(
            status = status.value(),
            error = status.reasonPhrase,
            message = errorMessage,
            path = request.requestURI
        )
        logger.error("Exception error from REST controller: $errorResponse")
        return ResponseEntity.badRequest().body(errorResponse)
    }
}

/**
 * Response returned for error conditions.
 *
 * Contains status code, reason phrase and failure messages.
 */
data class ErrorResponse(
    /** Status code */
    val status: Int,
    /** Reason phrase */
    val error: String,
    /** Validation error messages */
    val message: List<String>,
    /** Path that caused error */
    val path: String
)