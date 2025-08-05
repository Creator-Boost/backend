package src.main.com.example.gig_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "gig_images")
public class GigImage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gig_id", nullable = false)
    private Gig gig;

    private String url;
    private boolean isPrimary;

    public void setIsPrimary(Object isPrimary) {
    }

    public Object isIsPrimary() {
        return isPrimary;
    }
}
