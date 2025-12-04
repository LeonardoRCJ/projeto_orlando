package tech.devleo.projeto_orlando.dto;

import jakarta.validation.constraints.NotNull;
import tech.devleo.projeto_orlando.domain.MetodoPagamento;

public record PagamentoRequest(
        @NotNull(message = "metodo is required") MetodoPagamento metodo,
        @NotNull(message = "dividaId is required") Integer dividaId
) {
}
