package com.mar.utils;

import com.mar.api.dto.ExceptionResponse;
import com.mar.api.dto.UpdateWallet;
import com.mar.api.dto.WalletDto;
import com.mar.api.dto.WalletOperations;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
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
import static com.mar.utils.VerificationUtils.verifi;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VerificationUtilsTest {

    @Test
    void verifi_update_amount_test() {
        UpdateWallet upd = null;
        List<ExceptionResponse.Errors> errors = verifi(upd);
        assertNotNull(errors);
        assertFalse(errors.isEmpty());
        assertEquals(1, errors.size());
        assertEquals(UPDATE_WALLET__RQ_IS_NULL, errors.get(0));

        errors = verifi(new UpdateWallet(null, null, null));
        assertNotNull(errors);
        assertFalse(errors.isEmpty());
        assertEquals(3, errors.size());
        assertTrue(errors.contains(WALLET_ID_IS_NULL));
        assertTrue(errors.contains(UPDATE_WALLET__OPERATION_TYPE_IS_NULL));
        assertTrue(errors.contains(AMOUNT_IS_NULL));

        errors = verifi(new UpdateWallet("trash", WalletOperations.DEPOSIT, BigDecimal.valueOf(-1)));
        assertNotNull(errors);
        assertFalse(errors.isEmpty());
        assertEquals(2, errors.size());
        assertTrue(errors.contains(WALLET_ID_IS_NOT_UUID));
        assertTrue(errors.contains(AMOUNT_IS_NEGATIVE));

        errors = verifi(new UpdateWallet(UUID.randomUUID().toString(), WalletOperations.DEPOSIT, BigDecimal.ONE));
        assertNotNull(errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    void verifi_wallet_test() {
        WalletDto dto = null;
        List<ExceptionResponse.Errors> errors = verifi(dto, true);
        assertNotNull(errors);
        assertFalse(errors.isEmpty());
        assertEquals(1, errors.size());
        assertEquals(CREATE_WALLET__RQ_IS_NULL, errors.get(0));

        errors = verifi(dto, false);
        assertNotNull(errors);
        assertFalse(errors.isEmpty());
        assertEquals(1, errors.size());
        assertEquals(UPDATE_WALLET__RQ_IS_NULL, errors.get(0));

        errors = verifi(new WalletDto("need null", BigDecimal.TEN), true);
        assertNotNull(errors);
        assertFalse(errors.isEmpty());
        assertEquals(1, errors.size());
        assertEquals(CREATE_WALLET__ID_IS_NOT_NULL, errors.get(0));

        errors = verifi(new WalletDto("need null", null), true);
        assertNotNull(errors);
        assertFalse(errors.isEmpty());
        assertEquals(2, errors.size());
        assertTrue(errors.contains(CREATE_WALLET__ID_IS_NOT_NULL));
        assertTrue(errors.contains(AMOUNT_IS_NULL));

        errors = verifi(new WalletDto(UUID.randomUUID().toString(), BigDecimal.valueOf(-1)), false);
        assertNotNull(errors);
        assertFalse(errors.isEmpty());
        assertEquals(1, errors.size());
        assertTrue(errors.contains(AMOUNT_IS_NEGATIVE));

        // OK
        errors = verifi(new WalletDto(null, BigDecimal.TEN), true);
        assertNotNull(errors);
        assertTrue(errors.isEmpty());

        errors = verifi(new WalletDto(UUID.randomUUID().toString(), BigDecimal.TEN), false);
        assertNotNull(errors);
        assertTrue(errors.isEmpty());
    }

}