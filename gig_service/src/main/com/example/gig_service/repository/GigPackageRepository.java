package src.main.com.example.gig_service.repository;

import src.main.com.example.gig_service.entity.GigPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GigPackageRepository extends JpaRepository<GigPackage, UUID> {

    // Find all packages for a specific gig
    List<GigPackage> findByGigId(UUID gigId);

    // Find a specific package within a gig
    @Query("SELECT gp FROM GigPackage gp WHERE gp.gig.id = :gigId AND gp.id = :packageId")
    Optional<GigPackage> findByGigIdAndPackageId(@Param("gigId") UUID gigId, @Param("packageId") UUID packageId);

    // Check if a package exists within a specific gig
    boolean existsByGigIdAndId(UUID gigId, UUID packageId);
}
