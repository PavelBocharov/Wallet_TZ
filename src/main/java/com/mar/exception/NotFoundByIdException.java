package com.mar.exception;

import lombok.Getter;

import java.util.UUID;

import static com.mar.api.dto.ExceptionResponse.Errors.NOT_FIND_WALLET_BY_ID;

@Getter
public class NotFoundByIdException extends WalletException {

    private final UUID id;

    public NotFoundByIdException(UUID id) {
        super(NOT_FIND_WALLET_BY_ID.getErrorMsg());
        this.id = id;
    }

}
