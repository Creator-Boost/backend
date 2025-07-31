package com.example.gig_service.repository;

import com.example.gig_service.entity.GigImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GigImageRepository extends JpaRepository<GigImage, UUID> {}
