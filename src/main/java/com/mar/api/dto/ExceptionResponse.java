package com.mar.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExceptionResponse {

    private LocalDateTime time;
    private List<ErrorResponse> errors;

    @Getter
    @AllArgsConstructor
    public enum Errors {
        GET_WALLET__ID_IS_NULL(1, "Cannot get wallet - ID is NULL."),
        NOT_FIND_WALLET_BY_ID(2, "Cannot find wallet by ID."),
        WITHDRAW_LOW_AMOUNT(3, "Cannot WITHDRAW amount. There are not enough funds on deposit."),
        REMOVE_WALLET__ID_IS_NULL(4, "Cannot remove wallet - ID is NULL."),
        UPDATE_WALLET__OPERATION_TYPE_IS_NULL(5, "Update wallet request.operationType is NULL."),
        AMOUNT_IS_NULL(6, "Create/Update wallet request.amount is NULL."),
        AMOUNT_IS_NEGATIVE(7, "Amount cannot be a negative value."),
        UPDATE_WALLET__RQ_IS_NULL(8, "Update wallet request is NULL."),

        CREATE_WALLET__RQ_IS_NULL(9, "Create wallet request is NULL."),
        CREATE_WALLET__ID_IS_NOT_NULL(10, "When creating a deposit, the ID must be empty."),

        WALLET_ID_IS_NULL(11, "Update wallet request.id is NULL."),
        WALLET_ID_IS_NOT_UUID(12, "Update wallet Id is NOT UUID.");

        Integer errorCode;
        String errorMsg;

    }

    public record ErrorResponse(Integer errorCode, String errorMessage) {
    }

}
