package com.mar.api;

import com.mar.api.dto.ExceptionResponse;
import com.mar.api.dto.UpdateWallet;
import com.mar.api.dto.WalletDto;
import com.mar.exception.ValidationException;
import com.mar.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1")
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/wallets/{id}")
    public WalletDto getWallet(@PathVariable("id") String id) {
        log.debug("Get wallet by id = {}", id);
        return walletService.get(id);
    }

    @GetMapping("/wallets")
    public List<WalletDto> getAll() {
        log.debug("Get all wallet");
        return walletService.getAll();
    }

    @PostMapping("/wallet")
    public WalletDto create(@RequestBody WalletDto dto) {
        log.debug("Create wallet: {}", dto);
        return walletService.create(dto);
    }

    // POST api/v1/wallet >>>> PUT api/v1/wallet
    @PutMapping("/wallet")
    public WalletDto update(@RequestBody UpdateWallet dto) {
        log.debug("Update wallet: {}", dto);
        return walletService.updateAmount(dto);
    }

    @DeleteMapping("/wallets/{id}")
    public void remove(@PathVariable("id") String id) {
        log.debug("Remove wallet by id: {}", id);
        walletService.remove(id);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ExceptionResponse handleException(Exception ex) {
        List<ExceptionResponse.ErrorResponse> errors;

        if (ex instanceof ValidationException) {
            errors = ((ValidationException) ex).getErrors()
                    .parallelStream()
                    .map(err -> new ExceptionResponse.ErrorResponse(err.getErrorCode(), err.getErrorMsg()))
                    .toList();
        } else {
            errors = List.of(new ExceptionResponse.ErrorResponse(0, ex.getMessage()));
        }

        return ExceptionResponse.builder().time(LocalDateTime.now()).errors(errors).build();
    }

}
