package com.mar.utils;

import com.mar.api.dto.ExceptionResponse;
import com.mar.api.dto.UpdateWallet;
import com.mar.api.dto.WalletDto;
import com.mar.exception.ValidationException;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.mar.api.dto.ExceptionResponse.Errors.AMOUNT_IS_NEGATIVE;
import static com.mar.api.dto.ExceptionResponse.Errors.AMOUNT_IS_NULL;
import static com.mar.api.dto.ExceptionResponse.Errors.CREATE_WALLET__ID_IS_NOT_NULL;
import static com.mar.api.dto.ExceptionResponse.Errors.CREATE_WALLET__RQ_IS_NULL;
import static com.mar.api.dto.ExceptionResponse.Errors.UPDATE_WALLET__OPERATION_TYPE_IS_NULL;
import static com.mar.api.dto.ExceptionResponse.Errors.UPDATE_WALLET__RQ_IS_NULL;
import static com.mar.api.dto.ExceptionResponse.Errors.WALLET_ID_IS_NOT_UUID;
import static com.mar.api.dto.ExceptionResponse.Errors.WALLET_ID_IS_NULL;

@UtilityClass
public class VerificationUtils {

    public static void verifiWithException(UpdateWallet dto) {
        List<ExceptionResponse.Errors> errors = VerificationUtils.verifi(dto);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    public static List<ExceptionResponse.Errors> verifi(UpdateWallet dto) {
        List<ExceptionResponse.Errors> errors = new ArrayList<>(3);

        if (dto != null) {
            // Check wallet ID
            errors.addAll(verifiId(dto.getWalletId()));
            // Check operation type
            if (dto.getOperationType() == null) {
                errors.add(UPDATE_WALLET__OPERATION_TYPE_IS_NULL);
            }
            // Check amount
            if (dto.getAmount() == null) {
                errors.add(AMOUNT_IS_NULL);
            } else {
                if (dto.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                    errors.add(AMOUNT_IS_NEGATIVE);
                }
            }
        } else {
            errors.add(UPDATE_WALLET__RQ_IS_NULL);
        }

        return errors;
    }

    public static void verifiWithException(WalletDto dto, boolean isNew) {
        List<ExceptionResponse.Errors> errors = VerificationUtils.verifi(dto, isNew);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    public static List<ExceptionResponse.Errors> verifi(WalletDto dto, boolean isNew) {
        List<ExceptionResponse.Errors> errors = new ArrayList<>(2);

        if (dto != null) {
            // Check wallet ID
            if (isNew) {
                if (dto.getId() != null) {
                    errors.add(CREATE_WALLET__ID_IS_NOT_NULL);
                }
            } else {
                errors.addAll(verifiId(dto.getId()));
            }
            // Check amount
            if (dto.getAmount() == null) {
                errors.add(AMOUNT_IS_NULL);
            } else {
                if (dto.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                    errors.add(AMOUNT_IS_NEGATIVE);
                }
            }
        } else {
            errors.add(isNew ? CREATE_WALLET__RQ_IS_NULL : UPDATE_WALLET__RQ_IS_NULL);
        }

        return errors;
    }

    public static List<ExceptionResponse.Errors> verifiId(String id) {
        if (id == null) {
            return Collections.singletonList(WALLET_ID_IS_NULL);
        } else {
            try {
                UUID.fromString(id);
            } catch (IllegalArgumentException e) {
                return Collections.singletonList(WALLET_ID_IS_NOT_UUID);
            }
        }
        return Collections.emptyList();
    }

}
