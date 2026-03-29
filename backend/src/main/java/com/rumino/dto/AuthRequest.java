package com.rumino.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequest {

    @NotBlank
    private String identifier;

    @NotBlank
    @Size(min = 4, max = 40)
    private String password;
}

