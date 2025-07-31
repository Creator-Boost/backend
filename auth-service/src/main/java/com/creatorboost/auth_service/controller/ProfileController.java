package com.creatorboost.auth_service.controller;

import com.creatorboost.auth_service.io.ProfileRequest;
import com.creatorboost.auth_service.io.ProfileResponse;
import com.creatorboost.auth_service.io.ResetPasswordRequest;
import com.creatorboost.auth_service.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
           profileService.resetPassword(request.getEmail(), request.getResetOtp(), request.getNewPassword());
       }catch (Exception e){
              throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                      "Unable to reset password. Please try again later.");
       }

    }


}
