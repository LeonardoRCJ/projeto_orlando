package tech.devleo.projeto_orlando.dto;

import java.util.UUID;

import tech.devleo.projeto_orlando.domain.MetodoPagamento;

public record PagamentoResponse(
    Integer dividaId, 
    UUID contaId, 
    MetodoPagamento metodo
) {
}