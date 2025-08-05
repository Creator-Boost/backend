package src.main.com.example.gig_service.repository;

import src.main.com.example.gig_service.entity.GigFaq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GigFaqRepository extends JpaRepository<GigFaq, UUID> {}
