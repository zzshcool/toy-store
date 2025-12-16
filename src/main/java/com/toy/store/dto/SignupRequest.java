package com.toy.store.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^09\\d{8}$", message = "手機格式不正確")
    private String phone;

    @Size(max = 30)
    private String nickname;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;
}
