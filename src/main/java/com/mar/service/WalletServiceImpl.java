package com.mar.service;

import com.mar.api.dto.ExceptionResponse;
import com.mar.api.dto.UpdateWallet;
import com.mar.api.dto.WalletDto;
import com.mar.api.dto.WalletOperations;
import com.mar.db.entity.Wallet;
import com.mar.db.mapper.WalletMapper;
import com.mar.db.repository.WalletRepository;
import com.mar.exception.NotFoundByIdException;
import com.mar.exception.ValidationException;
import com.mar.utils.VerificationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.mar.api.dto.ExceptionResponse.Errors.GET_WALLET__ID_IS_NULL;
import static com.mar.api.dto.ExceptionResponse.Errors.REMOVE_WALLET__ID_IS_NULL;
import static com.mar.api.dto.ExceptionResponse.Errors.WITHDRAW_LOW_AMOUNT;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;

    @Override
    public WalletDto get(UUID id) {
        if (id == null) {
            throw new ValidationException(GET_WALLET__ID_IS_NULL);
        }

        return walletMapper.toDto(
                walletRepository
                        .findById(id)
                        .orElseThrow(() -> new NotFoundByIdException(id))
        );
    }

    @Override
    public WalletDto get(String id) {
        List<ExceptionResponse.Errors> errors = VerificationUtils.verifiId(id);
        if (errors.isEmpty()) {
            return get(UUID.fromString(id));
        }
        throw new ValidationException(errors);
    }

    @Override
    public List<WalletDto> getAll() {
        return walletRepository.findAll().parallelStream().map(walletMapper::toDto).toList();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, timeout = 5)
    public WalletDto create(WalletDto dto) {
        VerificationUtils.verifiWithException(dto, true);

        return walletMapper.toDto(
                walletRepository.save(walletMapper.toEntity(dto))
        );
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, timeout = 5)
    public WalletDto updateAmount(UpdateWallet upd) {
        VerificationUtils.verifiWithException(upd);

        UUID id = UUID.fromString(upd.getWalletId());
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new NotFoundByIdException(id));

        if (WalletOperations.DEPOSIT.equals(upd.getOperationType())) {
            wallet.setAmount(wallet.getAmount().add(upd.getAmount()));
        } else {
            if (wallet.getAmount().compareTo(upd.getAmount()) < 0) {
                throw new ValidationException(WITHDRAW_LOW_AMOUNT);
            } else {
                wallet.setAmount(wallet.getAmount().subtract(upd.getAmount()));
            }
        }

        return walletMapper.toDto(walletRepository.save(wallet));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, timeout = 5)
    public void remove(UUID id) {
        if (id == null) {
            throw new ValidationException(REMOVE_WALLET__ID_IS_NULL);
        }
        Wallet wallet = walletRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundByIdException(id));

        walletRepository.delete(wallet);
    }

    @Override
    public void remove(String id) {
        List<ExceptionResponse.Errors> errors = VerificationUtils.verifiId(id);
        if (errors.isEmpty()) {
            remove(UUID.fromString(id));
            return;
        }
        throw new ValidationException(errors);
    }
}
