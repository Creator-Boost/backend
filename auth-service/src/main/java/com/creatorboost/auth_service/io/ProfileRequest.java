package com.creatorboost.auth_service.io;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileRequest {
    @NotBlank(message = "Name should not be empty")
    private String name;
    @Email(message = "Email should be valid")
    @NotNull(message = "Email should not be null")
    private String email;
    @Size(min = 6, message = "Password should be at least 6 characters long")
    private String password;
}
