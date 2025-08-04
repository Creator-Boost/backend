package com.creatorboost.auth_service.service;

import com.creatorboost.auth_service.entiy.ClientProfile;
import com.creatorboost.auth_service.entiy.ProviderProfile;
import com.creatorboost.auth_service.io.ClientProfileRequset;
import com.creatorboost.auth_service.io.ProfileRequest;
import com.creatorboost.auth_service.io.ProfileResponse;
import com.creatorboost.auth_service.io.ProviderProfileRequest;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {
    ProfileResponse createProfile(ProfileRequest request);
    ProfileResponse getProfile(String email);
    void sendResetOtp(String email);
    void resetPassword( String resetOtp, String newPassword);
    void sendOtp(String email);
    void verifyOtp(String email, String otp);
    String getLoggedUserId(String email);

    String uploadFile(MultipartFile file);
    boolean deleteFile(String filename);
    ProfileResponse updateProfileImage(String email, MultipartFile image);

    void updateProviderProfile(ProviderProfileRequest profileData,String email);
    void updateClientProfile(ClientProfileRequset profileData,String email);
}
