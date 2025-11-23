package com.mar.service;

import com.mar.api.dto.UpdateWallet;
import com.mar.api.dto.WalletDto;

import java.util.List;
import java.util.UUID;

public interface WalletService {
    WalletDto get(UUID id);

    WalletDto get(String id);

    List<WalletDto> getAll();

    WalletDto create(WalletDto dto);

    WalletDto updateAmount(UpdateWallet upd);

    void remove(UUID id);

    void remove(String id);
}
