package com.experian.devicematcher.exceptions;

import com.experian.devicematcher.dto.ApiErrorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@ControllerAdvice
public class AppExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(AppExceptionHandler.class);

    @ExceptionHandler(DeviceProfileNotFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleDeviceProfileNotFoundException(DeviceProfileNotFoundException ex) {
        logger.error("DeviceProfileNotFoundException: {}", ex.getMessage(), ex);
        var error = new ApiErrorDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(UserAgentParsingException.class)
    public ResponseEntity<ApiErrorDTO> handleUserAgentParsingException(UserAgentParsingException ex) {
        logger.error("UserAgentParsingException: {}", ex.getMessage(), ex);
        var error = new ApiErrorDTO(HttpStatus.BAD_REQUEST.value(), ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DeviceProfileException.class)
    public ResponseEntity<ApiErrorDTO> handleDeviceProfileException(DeviceProfileException ex) {
        logger.error("DeviceProfileException: {}", ex.getMessage(), ex);
        var error = new ApiErrorDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(DeviceProfileMatchException.class)
    public ResponseEntity<ApiErrorDTO> handleDeviceProfileMatchException(DeviceProfileMatchException ex) {
        logger.error("DeviceProfileMatchException: {}", ex.getMessage(), ex);
        var error = new ApiErrorDTO(HttpStatus.BAD_REQUEST.value(), ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorDTO> handleHandlerMethodValidationException(HandlerMethodValidationException ex) {
        logger.error("Validation error: {}", ex.getMessage(), ex);
        var error = new ApiErrorDTO(HttpStatus.BAD_REQUEST.value(), ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> handleGenericException(Exception ex) {
        logger.error("{}: {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        var error = new ApiErrorDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorDTO> handleRuntimeException(RuntimeException ex) {
        logger.error("{}: {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        var error = new ApiErrorDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
