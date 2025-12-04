package tech.devleo.projeto_orlando.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;

public record ContratoRequest(
        @NotBlank(message = "textoContrato is required") String textoContrato,
        @NotBlank(message = "devedorId is required") String devedorId,
        LocalDate dataVencimento
) {
}
