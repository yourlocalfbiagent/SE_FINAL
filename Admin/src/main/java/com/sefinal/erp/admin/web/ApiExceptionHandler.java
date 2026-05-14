package com.sefinal.erp.admin.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail notFound(NotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ProblemDetail badRequest(BadRequestException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail conflict(ConflictException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail runtime(RuntimeException e) {
        Throwable cause = e.getCause();
        if (cause instanceof SQLException sql && "23505".equals(sql.getSQLState())) {
            return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                    "uniqueness violation: " + sql.getMessage());
        }
        if (cause instanceof SQLException sql && "23503".equals(sql.getSQLState())) {
            return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                    "foreign key violation: " + sql.getMessage());
        }
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
    }
}
