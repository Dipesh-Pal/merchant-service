package com.pal.dipesh.razorpay.merchant.mapper;

import com.pal.dipesh.razorpay.merchant.dto.response.WebhookConfigResponse;
import com.pal.dipesh.razorpay.merchant.entity.MerchantWebhookConfig;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WebhookConfigMapper {
    WebhookConfigMapper INSTANCE = Mappers.getMapper(WebhookConfigMapper.class);

    @Mapping(target = "webhookSecret", source = "rawSecret")
    WebhookConfigResponse toResponse(MerchantWebhookConfig entity, String rawSecret);
}
