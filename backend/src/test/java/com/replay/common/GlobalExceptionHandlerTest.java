package com.replay.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should map ResourceNotFoundException to 404 ProblemDetail")
    void handleNotFound_Returns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Entity not found");
        ProblemDetail problem = exceptionHandler.handleNotFound(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problem.getTitle()).isEqualTo("Resource Not Found");
        assertThat(problem.getDetail()).isEqualTo("Entity not found");
    }

    @Test
    @DisplayName("Should map BadRequestException to 400 ProblemDetail")
    void handleBadRequest_Returns400() {
        BadRequestException ex = new BadRequestException("Invalid payload supplied");
        ProblemDetail problem = exceptionHandler.handleBadRequest(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getTitle()).isEqualTo("Bad Request");
        assertThat(problem.getDetail()).isEqualTo("Invalid payload supplied");
    }

    @Test
    @DisplayName("Should map UnauthorizedException to 401 ProblemDetail")
    void handleUnauthorized_Returns401() {
        UnauthorizedException ex = new UnauthorizedException("User not authenticated");
        ProblemDetail problem = exceptionHandler.handleUnauthorized(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(problem.getTitle()).isEqualTo("Unauthorized");
    }

    @Test
    @DisplayName("Should map BadCredentialsException to 401 ProblemDetail")
    void handleBadCredentials_Returns401() {
        BadCredentialsException ex = new BadCredentialsException("Bad creds");
        ProblemDetail problem = exceptionHandler.handleBadCredentials(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(problem.getTitle()).isEqualTo("Authentication Failed");
    }

    @Test
    @DisplayName("Should map AccessDeniedException to 403 ProblemDetail")
    void handleAccessDenied_Returns403() {
        AccessDeniedException ex = new AccessDeniedException("Forbidden action");
        ProblemDetail problem = exceptionHandler.handleAccessDenied(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(problem.getTitle()).isEqualTo("Forbidden");
    }

    @Test
    @DisplayName("Should map generic Exception to 500 ProblemDetail")
    void handleGeneric_Returns500() {
        Exception ex = new RuntimeException("Unexpected error");
        ProblemDetail problem = exceptionHandler.handleGeneric(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problem.getTitle()).isEqualTo("Internal Server Error");
    }
}
