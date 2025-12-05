package com.rin.hlsserver.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BaseErrorCode implements ErrorCode {
    // 🔹 Common errors
    INTERNAL_SERVER_ERROR(9000, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST(9001, "Invalid Request", HttpStatus.BAD_REQUEST),
    NO_ACCESS(9003, "No Access", HttpStatus.FORBIDDEN),
    RESOURCE_NOT_FOUND(9004, "Resource Not Found", HttpStatus.NOT_FOUND),
    BAD_REQUEST(9005, "Bad Request", HttpStatus.BAD_REQUEST),
    METHOD_NOT_ALLOWED(9007, "Method Not Allowed", HttpStatus.METHOD_NOT_ALLOWED),
    CONFLICT(9008, "Conflict", HttpStatus.CONFLICT),
    UNAUTHENTICATED(9009, "Unauthenticated", HttpStatus.UNAUTHORIZED),

    ;


    private final int code;
    private final String message;
    private final HttpStatus status;
}
