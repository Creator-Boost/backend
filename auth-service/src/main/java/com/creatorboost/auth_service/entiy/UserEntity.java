package com.creatorboost.auth_service.entiy;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@Table(name = "users")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String userId;
    private String name;
    @Column(unique = true)
    private String email;
    private String password;
    private String verifyOtp;
    private boolean isAccountVerified;
    private long verifyOtpExpiry;
    private  String resetOtp;
    private long resetOtpExpiry;

    @CreationTimestamp
    @Column( updatable = false)
    private long createdAt;
    @UpdateTimestamp
    private long updatedAt;


}
