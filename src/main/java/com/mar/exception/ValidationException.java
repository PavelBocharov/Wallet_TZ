package com.mar.exception;

import com.mar.api.dto.ExceptionResponse;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class ValidationException extends WalletException {

    private final List<ExceptionResponse.Errors> errors;

    public ValidationException(List<ExceptionResponse.Errors> errors) {
        super("Validation exception. Error list: " + errors);
        this.errors = Collections.unmodifiableList(errors);
    }

    public ValidationException(ExceptionResponse.Errors error) {
        super("Validation exception. Error: " + error);
        this.errors = List.of(error);
    }

}
