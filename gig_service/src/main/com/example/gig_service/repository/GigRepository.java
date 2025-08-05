package src.main.com.example.gig_service.repository;

import src.main.com.example.gig_service.entity.Gig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GigRepository extends JpaRepository<Gig, UUID> {
    List<Gig> findByPlatform(String platform);
    List<Gig> findByCategory(String category);
    List<Gig> findBySellerId(UUID sellerId);
}
