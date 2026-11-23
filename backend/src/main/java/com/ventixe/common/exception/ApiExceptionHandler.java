package com.ventixe.common.exception;

import com.ventixe.common.dto.ApiErrorResponse;
import com.ventixe.identity.exception.AccountUnavailableException;
import com.ventixe.identity.exception.EmailAlreadyRegisteredException;
import com.ventixe.identity.exception.InvalidCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleNotFound(
      ResourceNotFoundException exception,
      HttpServletRequest request) {
    return response(HttpStatus.NOT_FOUND, exception.getMessage(), request);
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
      InvalidCredentialsException exception,
      HttpServletRequest request) {
    return response(HttpStatus.UNAUTHORIZED, exception.getMessage(), request);
  }

  @ExceptionHandler(AccountUnavailableException.class)
  public ResponseEntity<ApiErrorResponse> handleUnavailableAccount(
      AccountUnavailableException exception,
      HttpServletRequest request) {
    return response(HttpStatus.FORBIDDEN, exception.getMessage(), request);
  }

  @ExceptionHandler({EmailAlreadyRegisteredException.class, DataIntegrityViolationException.class})
  public ResponseEntity<ApiErrorResponse> handleConflict(
      Exception exception,
      HttpServletRequest request) {
    String message = exception instanceof EmailAlreadyRegisteredException
        ? exception.getMessage()
        : "The request conflicts with an existing record";
    return response(HttpStatus.CONFLICT, message, request);
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ApiErrorResponse> handleStateConflict(
      IllegalStateException exception,
      HttpServletRequest request) {
    return response(HttpStatus.CONFLICT, exception.getMessage(), request);
  }

  @ExceptionHandler({
      IllegalArgumentException.class,
      ConstraintViolationException.class,
      MethodArgumentTypeMismatchException.class,
      MethodArgumentNotValidException.class
  })
  public ResponseEntity<ApiErrorResponse> handleBadRequest(
      Exception exception,
      HttpServletRequest request) {
    return response(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
  }

  private ResponseEntity<ApiErrorResponse> response(
      HttpStatus status,
      String message,
      HttpServletRequest request) {
    return ResponseEntity.status(status).body(new ApiErrorResponse(
        Instant.now(),
        status.value(),
        status.getReasonPhrase(),
        message,
        request.getRequestURI()));
  }
}
