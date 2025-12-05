package tech.devleo.projeto_orlando.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @Schema(description = "Nome de usuário único", example = "admin_empresa")
        @NotBlank @Size(min = 3, max = 50) String username,
        
        @Schema(description = "Senha (mínimo 6 caracteres)", example = "SenhaForte123!")
        @NotBlank @Size(min = 6) String password,
        
        @Schema(description = "Email válido", example = "admin@empresa.com")
        @NotBlank @Email String email
    ) {}