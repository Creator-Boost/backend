package com.creatorboost.auth_service.service;

import com.creatorboost.auth_service.entiy.ClientProfile;
import com.creatorboost.auth_service.entiy.ProviderProfile;
import com.creatorboost.auth_service.entiy.UserEntity;
import com.creatorboost.auth_service.io.ClientProfileRequset;
import com.creatorboost.auth_service.io.ProfileRequest;
import com.creatorboost.auth_service.io.ProfileResponse;
import com.creatorboost.auth_service.io.ProviderProfileRequest;
import com.creatorboost.auth_service.repository.ClientProfileRepository;
import com.creatorboost.auth_service.repository.ProviderProfileRepository;
import com.creatorboost.auth_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements   ProfileService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaProducerService kafkaProducerService;
    private final CloudinaryClient cloudinaryClient;
    private final ProviderProfileRepository providerProfileRepository;
    private final ClientProfileRepository clientProfileRepository;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ProfileServiceImpl.class);

    @Value("${client.url}")
    private String clientUrl;

    @Override
    public ProfileResponse createProfile(ProfileRequest request){
        UserEntity newProfile= convertToUserEntity(request);
        if (!userRepository.existsByEmail(newProfile.getEmail())) {
            newProfile = userRepository.save(newProfile);

            // Send welcome email via Kafka
            try {
                kafkaProducerService.sendWelcomeEmail(newProfile.getEmail(), newProfile.getName());
                logger.info("✅ Welcome email notification sent for user: {}", newProfile.getEmail());
            } catch (Exception e) {
                logger.error("❌ Failed to send welcome email notification for user: {}", newProfile.getEmail(), e);
                // Don't throw exception here as user creation was successful
            }
            return convertToProfileResponse(newProfile);
        }

        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
    }

    @Override
    public ProfileResponse getProfile(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email: " + email));
        return convertToProfileResponse(existingUser);
    }

    @Override
    public void sendResetOtp(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email: " + email));

        byte[] bytes = new byte[20]; // 20 bytes = 40 hex characters
        new SecureRandom().nextBytes(bytes);
        String resetOtp = HexFormat.of().formatHex(bytes);


        //update the user entity with the reset OTP
        existingUser.setResetOtp(resetOtp);
        existingUser.setResetOtpExpiry(Instant.now().plusSeconds(15 * 60)); // 15 minutes

        // save the updated user entity
        userRepository.save(existingUser);

        try{
            //emailService.sendPasswordResetEmail(existingUser.getEmail(), resetOtp);
            kafkaProducerService.sendPasswordResetOtp(existingUser.getEmail(), clientUrl + "/reset-password/" + resetOtp);
            logger.info("✅ Password reset OTP notification sent for user: {}", existingUser.getEmail());
        }catch(Exception e){
            logger.error("❌ Failed to send password reset OTP notification for user: {}", existingUser.getEmail(), e);
            throw new RuntimeException("unable to send reset OTP", e);
        }
    }

    @Override
    public void resetPassword(String resetOtp, String newPassword) {
        UserEntity existingUser = userRepository.findByResetOtp(resetOtp)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired reset token"));

        if (existingUser.getResetOtp()==null || !existingUser.getResetOtp().equals(resetOtp)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reset OTP");
        }

        if (existingUser.getResetOtpExpiry().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reset OTP has expired");
        }
        existingUser.setPassword(passwordEncoder.encode(newPassword));
        existingUser.setResetOtp(null); // Clear the reset OTP after successful reset
        existingUser.setResetOtpExpiry(null); // Clear the expiry time

        userRepository.save(existingUser);

    }

    @Override
    public void sendOtp(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        if( existingUser.isAccountVerified()) {
            return;
        }
        String otp = String.format("%06d", (int) (Math.random() * 1000000));


        existingUser.setVerifyOtp(otp);
        existingUser.setVerifyOtpExpiry(Instant.now().plusSeconds(10 * 60 * 60)); // 10 hours

        userRepository.save(existingUser);

        try {
            //emailService.sendOtpEmail(existingUser.getEmail(), otp);
            kafkaProducerService.sendVerificationOtp(existingUser.getEmail(), otp);
            logger.info("✅ Verification OTP notification sent for user: {}", existingUser.getEmail());
        } catch (Exception e) {
            logger.error("❌ Failed to send verification OTP notification for user: {}", existingUser.getEmail(), e);
            throw new RuntimeException("Unable to send OTP", e);
        }
    }

    @Override
    public void verifyOtp(String email, String otp) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        if (existingUser.getVerifyOtp() == null || !existingUser.getVerifyOtp().equals(otp)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP");
        }
        if(existingUser.getVerifyOtpExpiry().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP has expired");
        }

        existingUser.setAccountVerified(true);
        existingUser.setVerifyOtp(null); // Clear the OTP after successful verification
        existingUser.setVerifyOtpExpiry(null); // Clear the expiry time
        userRepository.save(existingUser);
    }

    @Override
    public String getLoggedUserId(String email) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return existingUser.getUserId();
    }



    private UserEntity convertToUserEntity(ProfileRequest request) {
       return UserEntity.builder()
               .email(request.getEmail())
               .userId(UUID.randomUUID().toString())
               .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .isAccountVerified(false)
               .resetOtpExpiry(null)
               .verifyOtp(null)
               .verifyOtpExpiry(null)
                .resetOtp(null)
                .build();


    }
    private ProfileResponse convertToProfileResponse(UserEntity newProfile) {
        return ProfileResponse.builder()
                .name(newProfile.getName())
                .email(newProfile.getEmail())
                .userId(newProfile.getUserId())
                .role(newProfile.getRole())
                .isAccountVerified(newProfile.isAccountVerified())
                .imageUrl(newProfile.getImageUrl())
                .build();
    }


    @Override
    public String uploadFile(MultipartFile file) {
        String fileNameExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        String key = UUID.randomUUID().toString() + "." + fileNameExtension;

        try {
            return cloudinaryClient.uploadFile(file, key);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @Override
    public boolean deleteFile(String filename) {
        try {
            // Assuming CloudinaryClient has a deleteFile method
            return cloudinaryClient.deleteFile(filename);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file: " + filename, e);
        }
    }

    @Override
    public ProfileResponse updateProfileImage(String email, MultipartFile image) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email: " + email));

        if (image == null || image.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is required");
        }

        // Delete old image if exists
        if (existingUser.getImageUrl() != null) {
            try {
                String publicId = extractPublicIdFromUrl(existingUser.getImageUrl());
                deleteFile(publicId);
            } catch (Exception e) {
                logger.error("Failed to delete old image", e);
                // Continue with upload anyway
            }
        }

        // Upload new image
        try {
            String imageUrl = uploadFile(image);

            existingUser.setImageUrl(imageUrl);
            userRepository.save(existingUser);
            return convertToProfileResponse(existingUser);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload profile image", e);
        }
    }

    @Override
    @Transactional
    public void updateProviderProfile(ProviderProfileRequest profileData, String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        ProviderProfile profile = providerProfileRepository.findById(String.valueOf(user.getId()))
                .orElseGet(() -> {

                    ProviderProfile newProfile = new ProviderProfile();
                    newProfile.setUser(user); // Link to UserEntity // or set user directly if using @OneToOne
                    return newProfile;
                });
        // Update profile fields
        profile.setTitle(profileData.getTitle());
        profile.setLocation(profileData.getLocation());
        profile.setLanguages(profileData.getLanguages());
        profile.setSkills(profileData.getSkills());
        profile.setDescription(profileData.getDescription());
        profile.setCertifications(profileData.getCertifications());

        providerProfileRepository.save(profile);


    }

    @Override
    @Transactional
    public void updateClientProfile(ClientProfileRequset profileData, String email) {
        logger.info("Updating client profile for email: {}", email);
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        logger.debug("Found user with ID: {}", user.getId());
        ClientProfile profile = clientProfileRepository.findById(String.valueOf((user.getId())))
                .orElseGet(() -> {
                    logger.info("ClientProfile not found for user ID: {}. Creating new one.", user.getId());
                    ClientProfile newProfile = new ClientProfile();
                    newProfile.setUser(user); // Link to UserEntity // or set user directly if using @OneToOne
                    return newProfile;
                });
        logger.debug("Updating profile fields. Location: {}, Preferences: {}",
                profileData.getLocation(), profileData.getPreferences());
        profile.setLocation(profileData.getLocation());
        profile.setPreferences(profileData.getPreferences());

        clientProfileRepository.save(profile);

    }



    private String extractPublicIdFromUrl(String url) {
        try {
            // Example URL: https://res.cloudinary.com/your-cloud-name/image/upload/v1234567890/abc123.jpg
            String[] parts = url.split("/");
            String filenameWithExt = parts[parts.length - 1]; // abc123.jpg
            return filenameWithExt.substring(0, filenameWithExt.lastIndexOf('.')); // abc123
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract public ID from image URL", e);
        }
    }


}
