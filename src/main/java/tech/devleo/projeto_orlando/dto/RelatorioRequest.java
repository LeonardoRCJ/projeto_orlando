package tech.devleo.projeto_orlando.dto;

import java.time.LocalDate;

import tech.devleo.projeto_orlando.domain.TipoRelatorio;

public record RelatorioRequest(
        /**
         * Tipo de relatório a ser gerado
         */
        TipoRelatorio tipo,
        
        /**
         * Valor movimentado (obrigatório apenas para tipo MANUAL)
         */
        Double valorMovimentado,
        
        /**
         * ID da conta (obrigatório para CONTA_ESPECIFICA, opcional para outros tipos)
         */
        String contaId,
        
        /**
         * Data de início do período (obrigatório para PERIODO e RECEBIMENTOS)
         */
        LocalDate dataInicio,
        
        /**
         * Data de fim do período (obrigatório para PERIODO e RECEBIMENTOS)
         */
        LocalDate dataFim,
        
        /**
         * Valor mínimo de saldo para relatório de inadimplência (opcional para INADIMPLENCIA)
         */
        Double valorMinimoInadimplencia,
        
        /**
         * Descrição do relatório (opcional)
         */
        String descricao
) {
}