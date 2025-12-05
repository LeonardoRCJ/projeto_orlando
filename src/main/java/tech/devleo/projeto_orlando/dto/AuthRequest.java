package tech.devleo.projeto_orlando.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
                @Schema(description = "Email do usuário", example = "admin@empresa.com") @NotBlank @Email String email,
                @Schema(description = "Senha do usuário", example = "SenhaForte123!") @NotBlank String password) {
}
