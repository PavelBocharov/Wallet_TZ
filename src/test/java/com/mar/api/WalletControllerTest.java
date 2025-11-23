package com.mar.api;

import com.mar.api.dto.ExceptionResponse;
import com.mar.api.dto.UpdateWallet;
import com.mar.api.dto.WalletDto;
import com.mar.exception.ValidationException;
import com.mar.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.mar.api.dto.ExceptionResponse.Errors.AMOUNT_IS_NEGATIVE;
import static com.mar.api.dto.ExceptionResponse.Errors.UPDATE_WALLET__RQ_IS_NULL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class WalletControllerTest {

    @MockitoBean
    private WalletService walletService;

    @Autowired
    private WalletController controller;

    @Test
    void getWallet_test() {
        when(walletService.get(anyString())).thenReturn(new WalletDto());
        WalletDto dto = controller.getWallet(UUID.randomUUID().toString());
        assertNotNull(dto);
        verify(walletService, times(1)).get(anyString());
    }

    @Test
    void getAllWallet_test() {
        when(walletService.getAll()).thenReturn(Collections.emptyList());
        List<WalletDto> dtos = controller.getAll();
        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());
        verify(walletService, times(1)).getAll();

        when(walletService.getAll()).thenReturn(Collections.singletonList(new WalletDto()));
        dtos = controller.getAll();
        assertNotNull(dtos);
        assertFalse(dtos.isEmpty());
        verify(walletService, times(2)).getAll();
    }

    @Test
    void createWallet_test() {
        when(walletService.create(any(WalletDto.class))).thenReturn(new WalletDto());
        WalletDto dto = controller.create(new WalletDto());
        assertNotNull(dto);
        verify(walletService, times(1)).create(any());
    }

    @Test
    void updWallet_test() {
        when(walletService.updateAmount(any(UpdateWallet.class))).thenReturn(new WalletDto());
        WalletDto dto = controller.update(new UpdateWallet());
        assertNotNull(dto);
        verify(walletService, times(1)).updateAmount(any());
    }

    @Test
    void removeWallet_test() {
        when(walletService.updateAmount(any(UpdateWallet.class))).thenReturn(new WalletDto());
        controller.remove(UUID.randomUUID().toString());
        verify(walletService, times(1)).remove(anyString());
    }

    @Test
    void handleException_test() {
        ValidationException validationException = new ValidationException(
                List.of(
                        UPDATE_WALLET__RQ_IS_NULL,
                        AMOUNT_IS_NEGATIVE
                )
        );

        ExceptionResponse rs = controller.handleException(validationException);
        assertNotNull(rs);
        assertNotNull(rs.getTime());
        assertNotNull(rs.getErrors());
        assertFalse(rs.getErrors().isEmpty());
        assertEquals(2, rs.getErrors().size());

        ExceptionResponse.ErrorResponse error = rs.getErrors().stream()
                .filter(errorResponse -> UPDATE_WALLET__RQ_IS_NULL.getErrorCode().equals(errorResponse.errorCode()))
                .findFirst()
                .orElseThrow();
        assertEquals(UPDATE_WALLET__RQ_IS_NULL.getErrorMsg(), error.errorMessage());

        error = rs.getErrors().stream()
                .filter(errorResponse -> AMOUNT_IS_NEGATIVE.getErrorCode().equals(errorResponse.errorCode()))
                .findFirst()
                .orElseThrow();
        assertEquals(AMOUNT_IS_NEGATIVE.getErrorMsg(), error.errorMessage());

        // some exception
        String errMsg = "Some error.";
        rs = controller.handleException(new Exception(errMsg));
        assertNotNull(rs);
        assertNotNull(rs.getTime());
        assertNotNull(rs.getErrors());
        assertFalse(rs.getErrors().isEmpty());
        assertEquals(1, rs.getErrors().size());

        error = rs.getErrors().get(0);
        assertEquals(errMsg, error.errorMessage());
        assertEquals(0, error.errorCode());
    }
}