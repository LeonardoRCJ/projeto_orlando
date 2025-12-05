package tech.devleo.projeto_orlando.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record DevedorRequest(
                @Schema(description = "Nome completo do devedor", example = "Carlos Cliente Inadimplente") @NotBlank String name,
                @Schema(description = "CPF válido", example = "111.222.333-44") @NotBlank String cpf,
                @Schema(description = "Email para contato/cobrança", example = "carlos.cliente@email.com") @NotBlank @Email String email) {
}