package com.mar.db.mapper;

import com.mar.api.dto.WalletDto;
import com.mar.db.entity.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WalletMapper {

    @Mapping(target = "id", source = "walletId")
    WalletDto toDto(Wallet wallet);

    @Mapping(target = "walletId", source = "id")
    Wallet toEntity(WalletDto dto);

}
