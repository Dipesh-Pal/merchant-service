package com.pal.dipesh.razorpay.merchant.controller;

import com.pal.dipesh.razorpay.common.pojo.FindOrCreateCustomerRequest;
import com.pal.dipesh.razorpay.merchant.service.CustomerService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/customers")
public class InternalCustomerController {

    private final CustomerService customerService;

    @PostMapping("/find-or-create")
    ResponseEntity<UUID> findOrCreate(@RequestBody FindOrCreateCustomerRequest request){
        UUID customerId = customerService.findOrCreate(request.merchantId(), request.email(), request.name(), request.phone());
        return ResponseEntity.ok(customerId);
    }
}
