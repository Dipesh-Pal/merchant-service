package com.pal.dipesh.razorpay.merchant.mapper;

import com.pal.dipesh.razorpay.merchant.dto.request.MerchantSignupRequest;
import com.pal.dipesh.razorpay.merchant.dto.response.MerchantResponse;
import com.pal.dipesh.razorpay.merchant.entity.Merchant;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MerchantMapper {
    MerchantMapper INSTANCE = Mappers.getMapper(MerchantMapper.class);

    Merchant toEntity(MerchantSignupRequest request);

    @Mapping(target = "merchantStatus", source = "status")
    MerchantResponse toMerchantResponse(Merchant merchant);
}