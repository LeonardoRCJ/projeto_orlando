package tech.devleo.projeto_orlando.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ContratoRequest(
        @Schema(description = "Texto ou cláusulas do contrato", example = "Contrato de confissão de dívida referente ao serviço X...")
        @NotBlank String textoContrato,
        
        @Schema(description = "ID do Devedor (UUID)", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
        @NotBlank String devedorId,
        
        @Schema(description = "Data de vencimento (Opcional). Se vazio, será hoje + 1 ano.", example = "2025-12-31")
        LocalDate dataVencimento
    ) {}