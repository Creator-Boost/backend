package com.creatorboost.auth_service.service;

import com.creatorboost.auth_service.io.ProfileRequest;
import com.creatorboost.auth_service.io.ProfileResponse;

public interface ProfileService {
    ProfileResponse createProfile(ProfileRequest request);
    ProfileResponse getProfile(String email);
    void sendResetOtp(String email);
    void resetPassword(String email, String resetOtp, String newPassword);
    void sendOtp(String email);
    void verifyOtp(String email, String otp);
    String getLoggedUserId(String email);
}
