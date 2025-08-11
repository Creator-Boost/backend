package com.creatorboost.auth_service.repository;

import com.creatorboost.auth_service.entiy.ProviderProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderProfileRepository extends JpaRepository<ProviderProfile, String> {
}
