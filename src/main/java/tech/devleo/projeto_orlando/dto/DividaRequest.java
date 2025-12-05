package tech.devleo.projeto_orlando.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DividaRequest(
        @Schema(description = "Valor da dívida", example = "1500.50")
        @NotNull @Positive Double valor,
        
        @Schema(description = "ID da Conta do devedor (UUID)", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
        @NotBlank String contaId
    ) {}