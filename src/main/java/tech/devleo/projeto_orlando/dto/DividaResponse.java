package tech.devleo.projeto_orlando.dto;

import java.util.UUID;

public record DividaResponse(Integer id, Double valor, UUID contaId, UUID fiadoraId) {
}
