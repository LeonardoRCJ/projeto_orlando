package tech.devleo.projeto_orlando.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import tech.devleo.projeto_orlando.domain.MetodoPagamento;

public record PagamentoRequest(
    @Schema(description = "Método de pagamento utilizado", example = "PIX")
    @NotNull(message = "metodo is required") 
    MetodoPagamento metodo,

    @Schema(description = "ID da dívida que está sendo paga", example = "1")
    @NotNull(message = "dividaId is required") 
    Integer dividaId
) {
}