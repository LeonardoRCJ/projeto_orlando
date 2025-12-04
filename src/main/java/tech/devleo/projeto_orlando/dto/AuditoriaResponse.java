package tech.devleo.projeto_orlando.dto;

public record AuditoriaResponse(
        Double valorTotalDividas,
        Long totalPagamentos,
        String periodoInicio,
        String periodoFim
) {
}

