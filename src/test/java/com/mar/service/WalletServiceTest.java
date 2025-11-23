package com.mar.service;

import com.mar.api.dto.UpdateWallet;
import com.mar.api.dto.WalletDto;
import com.mar.api.dto.WalletOperations;
import com.mar.db.entity.Wallet;
import com.mar.db.mapper.WalletMapper;
import com.mar.db.repository.WalletRepository;
import com.mar.exception.NotFoundByIdException;
import com.mar.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class WalletServiceTest {

    WalletRepository walletRepository;
    WalletMapper walletMapper;
    WalletService walletService;

    @BeforeEach
    void init() {
        walletRepository = mock(WalletRepository.class);
        walletMapper = Mappers.getMapper(WalletMapper.class);
        walletService = new WalletServiceImpl(walletRepository, walletMapper);
    }

    @Test
    void get_wallet_test() {
        // Invalid args
        assertThrows(ValidationException.class, () -> {
            String strUuid = null;
            walletService.get(strUuid);
        });
        assertThrows(ValidationException.class, () -> {
            UUID uuid = null;
            walletService.get(uuid);
        });
        // DB empty
        when(walletRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThrows(NotFoundByIdException.class, () -> walletService.get(UUID.randomUUID()));

        // Positive
        UUID uuid = UUID.randomUUID();
        String strUuid = uuid.toString();
        Wallet wallet = new Wallet(uuid, BigDecimal.ZERO);

        when(walletRepository.findById(eq(uuid))).thenReturn(Optional.of(wallet));

        WalletDto dto = walletService.get(uuid);
        assertNotNull(dto);
        assertEquals(strUuid, dto.getId());
        assertEquals(BigDecimal.ZERO, dto.getAmount());

        dto = walletService.get(strUuid);
        assertNotNull(dto);
        assertEquals(strUuid, dto.getId());
        assertEquals(BigDecimal.ZERO, dto.getAmount());
    }

    @Test
    void get_all_wallet_test() {
        when(walletRepository.findAll()).thenReturn(Collections.emptyList());
        List<WalletDto> dtos = walletService.getAll();
        assertNotNull(dtos);
        assertTrue(dtos.isEmpty());

        when(walletRepository.findAll()).thenReturn(List.of(
                new Wallet(UUID.randomUUID(), BigDecimal.ZERO),
                new Wallet(UUID.randomUUID(), BigDecimal.ONE),
                new Wallet(UUID.randomUUID(), BigDecimal.TEN)
        ));
        dtos = walletService.getAll();
        assertNotNull(dtos);
        assertFalse(dtos.isEmpty());
        assertEquals(3, dtos.size());
    }

    @Test
    void create_wallet_test() {
        assertThrows(ValidationException.class, () -> {
            walletService.create(null); // Other in test utils
        });

        UUID id = UUID.randomUUID();
        String idStr = id.toString();
        BigDecimal amount = BigDecimal.valueOf(new Random().nextDouble(0, 100));
        Wallet createdWallet = new Wallet(id, amount);
        when(walletRepository.save(any(Wallet.class))).thenReturn(createdWallet);

        WalletDto dto = walletService.create(new WalletDto(null, amount));
        assertNotNull(dto);
        assertEquals(idStr, dto.getId());
        assertEquals(amount, dto.getAmount());
    }

    @Test
    void upd_amount_test() {
        assertThrows(ValidationException.class, () -> {
            walletService.updateAmount(null);
        });

        UUID id = UUID.randomUUID();
        String idStr = id.toString();
        BigDecimal amount = BigDecimal.valueOf(new Random().nextDouble(0, 100));

        // not found wallet
        when(walletRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThrows(NotFoundByIdException.class, () -> walletService.updateAmount(
                new UpdateWallet(UUID.randomUUID().toString(), WalletOperations.DEPOSIT, BigDecimal.ZERO))
        );

        // check DEPOSIT
        Wallet wallet = mock(Wallet.class);
        when(wallet.getAmount()).thenReturn(BigDecimal.ZERO);
        when(walletRepository.findById(any(UUID.class))).thenReturn(Optional.of(wallet));

        walletService.updateAmount(new UpdateWallet(idStr, WalletOperations.DEPOSIT, amount));
        verify(wallet, times(1)).getAmount();
        verify(wallet, times(1)).setAmount(any());

        // check WITHDRAW
        // error: wallet.amount - upd.amount < 0
        assertThrows(ValidationException.class, () -> walletService.updateAmount(
                new UpdateWallet(idStr, WalletOperations.WITHDRAW, BigDecimal.TEN)
        ));
        // ok: wallet.amount - upd.amount = 9
        Wallet savedWallet = new Wallet(UUID.randomUUID(), BigDecimal.ONE);
        when(walletRepository.save(any(Wallet.class))).thenReturn(savedWallet);

        when(wallet.getAmount()).thenReturn(BigDecimal.TEN);
        WalletDto dto = walletService.updateAmount(
                new UpdateWallet(idStr, WalletOperations.WITHDRAW, BigDecimal.ONE)
        );

        assertNotNull(dto);
        assertNotNull(dto.getId());
        assertEquals(savedWallet.getWalletId().toString(), dto.getId());
        assertEquals(savedWallet.getAmount(), dto.getAmount());
    }

    @Test
    void remove_test() {
        // Invalid args
        assertThrows(ValidationException.class, () -> {
            String strUuid = null;
            walletService.remove(strUuid);
        });
        assertThrows(ValidationException.class, () -> {
            UUID uuid = null;
            walletService.remove(uuid);
        });
        // DB empty
        when(walletRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThrows(NotFoundByIdException.class, () -> walletService.remove(UUID.randomUUID()));

        // Positive
        UUID uuid = UUID.randomUUID();
        Wallet wallet = new Wallet(uuid, BigDecimal.ZERO);
        when(walletRepository.findById(eq(uuid))).thenReturn(Optional.of(wallet));

        walletService.remove(uuid.toString());
        verify(walletRepository, times(1)).delete(any(Wallet.class));

        walletService.remove(uuid);
        verify(walletRepository, times(2)).delete(any(Wallet.class));
    }

}