package tech.devleo.projeto_orlando.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record DevedorRequest(
        @NotBlank(message = "name is required") String name,
        @NotBlank(message = "cpf is required") String cpf,
        @Email(message = "invalid email") @NotBlank(message = "email is required") String email
) {
}
