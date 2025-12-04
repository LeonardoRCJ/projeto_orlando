package tech.devleo.projeto_orlando.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DividaRequest(
        @NotNull(message = "valor is required") @Positive(message = "valor must be positive") Double valor,
        @NotBlank(message = "contaId is required") String contaId
) {
}