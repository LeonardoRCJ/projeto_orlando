package tech.devleo.projeto_orlando.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import tech.devleo.projeto_orlando.domain.TipoRelatorio;

public record RelatorioRequest(
    @Schema(description = "Tipo do relatório. Define quais outros campos são obrigatórios.", example = "PERIODO")
    TipoRelatorio tipo,
    
    @Schema(description = "Apenas para tipo MANUAL", example = "100.00")
    Double valorMovimentado,
    
    @Schema(description = "Obrigatório para CONTA_ESPECIFICA", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    String contaId,
    
    @Schema(description = "Obrigatório para PERIODO e RECEBIMENTOS", example = "2024-01-01")
    LocalDate dataInicio,
    
    @Schema(description = "Obrigatório para PERIODO e RECEBIMENTOS", example = "2024-01-31")
    LocalDate dataFim,
    
    @Schema(description = "Opcional para INADIMPLENCIA", example = "500.00")
    Double valorMinimoInadimplencia,
    
    @Schema(description = "Descrição personalizada", example = "Relatório de fechamento de Janeiro")
    String descricao
) {}