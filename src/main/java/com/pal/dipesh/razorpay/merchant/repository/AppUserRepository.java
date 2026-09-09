package com.pal.dipesh.razorpay.merchant.repository;

import com.pal.dipesh.razorpay.merchant.entity.AppUser;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByEmail(String username);
}