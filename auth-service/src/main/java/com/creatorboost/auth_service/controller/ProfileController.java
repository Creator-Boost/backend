package com.creatorboost.auth_service.controller;

import com.creatorboost.auth_service.entiy.ClientProfile;
import com.creatorboost.auth_service.entiy.ProviderProfile;
import com.creatorboost.auth_service.io.*;
import com.creatorboost.auth_service.service.ProfileService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse register(@Valid @RequestBody ProfileRequest request){
        ProfileResponse response = profileService.createProfile(request);
        //emailService.sendWelcomeEmail(response.getEmail(), response.getName());
        return response;
    }
    @GetMapping("/profile")
    public ProfileResponse getProfile(@CurrentSecurityContext(expression = "authentication.name") String email) {
        return profileService.getProfile(email);
    }
    @PostMapping("/reset-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request){
       try{
           profileService.resetPassword(request.getResetOtp(), request.getNewPassword());
       }catch (Exception e){
              throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                      "Unable to reset password. Please try again later.");
       }

    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("jwt", null)
                .httpOnly(true)
                .path("/")
                .maxAge(0) // Expire immediately
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "Logged out successfully"));
    }

    @PutMapping("/profile/image")
    public ResponseEntity<ProfileResponse> updateProfileImage(
            @RequestParam("image") MultipartFile image,
            @CurrentSecurityContext(expression = "authentication.name") String email) {

        try {
            ProfileResponse updatedProfile = profileService.updateProfileImage(email, image);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update profile image", e);
        }
    }

    @PutMapping("/provider/profile")
    public ResponseEntity<Void> updateProviderProfile(
            @RequestBody ProviderProfileRequest profileData,
            @CurrentSecurityContext(expression = "authentication.name") String email) {

        profileService.updateProviderProfile(profileData,email);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/client/profile")
    public ResponseEntity<Void> updateClientProfile(
            @RequestBody ClientProfileRequset profileData,
            @CurrentSecurityContext(expression = "authentication.name") String email) {

        profileService.updateClientProfile(profileData,email);
        return ResponseEntity.ok().build();
    }

}
