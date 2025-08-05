package src.main.com.example.gig_service.repository;

import src.main.com.example.gig_service.entity.GigPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GigPackageRepository extends JpaRepository<GigPackage, UUID> {}
