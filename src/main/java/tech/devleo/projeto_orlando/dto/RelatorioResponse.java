package tech.devleo.projeto_orlando.dto;

import java.time.ZonedDateTime;

import tech.devleo.projeto_orlando.domain.TipoRelatorio;

public record RelatorioResponse(
        Integer id,
        TipoRelatorio tipo,
        Double valorMovimentado,
        Double totalDividas,
        Double totalPagamentos,
        Integer quantidadeDividas,
        Integer quantidadePagamentos,
        Integer quantidadeContas,
        String descricao,
        java.util.UUID contaId,
        ZonedDateTime dataGeracao,
        ZonedDateTime dataInicio,
        ZonedDateTime dataFim
) {
}
