package com.creatorboost.auth_service.service;

import com.creatorboost.auth_service.entiy.UserEntity;
import com.creatorboost.auth_service.io.ProfileRequest;
import com.creatorboost.auth_service.io.ProfileResponse;
import com.creatorboost.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements   ProfileService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public ProfileResponse createProfile(ProfileRequest request){
        UserEntity newProfile= convertToUserEntity(request);
        if (!userRepository.existsByEmail(newProfile.getEmail())) {
            newProfile = userRepository.save(newProfile);
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

        //generate 6 digi otp
        String resetOtp = String.format("%06d", (int) (Math.random() * 1000000));

        //update the user entity with the reset OTP
        existingUser.setResetOtp(resetOtp);
        existingUser.setResetOtpExpiry(System.currentTimeMillis() + 15 * 60 * 1000); // 15 minutes expiry

        // save the updated user entity
        userRepository.save(existingUser);

        try{
            emailService.sendPasswordResetEmail(existingUser.getEmail(), resetOtp);
        }catch(Exception e){
            throw new RuntimeException("unable to send reset OTP", e);
        }
    }

    @Override
    public void resetPassword(String email, String resetOtp, String newPassword) {
        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email: " + email));

        if (existingUser.getResetOtp()==null || !existingUser.getResetOtp().equals(resetOtp)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reset OTP");
        }

        if (existingUser.getResetOtpExpiry() < System.currentTimeMillis()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reset OTP has expired");
        }
        existingUser.setPassword(passwordEncoder.encode(newPassword));
        existingUser.setResetOtp(null); // Clear the reset OTP after successful reset
        existingUser.setResetOtpExpiry(0); // Clear the expiry time

        userRepository.save(existingUser);

    }

    private UserEntity convertToUserEntity(ProfileRequest request) {
       return UserEntity.builder()
               .email(request.getEmail())
               .userId(UUID.randomUUID().toString())
               .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .isAccountVerified(false)
               .resetOtpExpiry(0)
               .verifyOtp(null)
               .verifyOtpExpiry(0)
                .resetOtp(null)
                .build();


    }
    private ProfileResponse convertToProfileResponse(UserEntity newProfile) {
        return ProfileResponse.builder()
                .name(newProfile.getName())
                .email(newProfile.getEmail())
                .userId(newProfile.getUserId())
                .isAccountVerified(newProfile.isAccountVerified())
                .build();
    }
}
