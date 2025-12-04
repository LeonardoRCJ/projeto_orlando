package tech.devleo.projeto_orlando.domain;

public enum TipoRelatorio {
    /**
     * Relatório manual - valor movimentado informado pelo usuário
     */
    MANUAL,
    
    /**
     * Relatório automático de uma conta específica
     * Calcula: saldo atual, total de dívidas, total de pagamentos, quantidade de dívidas/pagamentos
     */
    CONTA_ESPECIFICA,
    
    /**
     * Relatório consolidado de todas as contas da empresa
     * Calcula: saldo total, total de dívidas, total de pagamentos, quantidade de contas
     */
    CONSOLIDADO_EMPRESA,
    
    /**
     * Relatório de período - movimentações em um intervalo de datas
     * Calcula: dívidas criadas, pagamentos realizados no período
     */
    PERIODO,
    
    
    /**
     * Relatório de recebimentos - pagamentos em um período
     * Calcula: total recebido, quantidade de pagamentos, por método de pagamento
     */
    RECEBIMENTOS
}

