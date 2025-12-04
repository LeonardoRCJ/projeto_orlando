package tech.devleo.projeto_orlando.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

import tech.devleo.projeto_orlando.domain.StatusContrato;

public record ContratoResponse(
        UUID id, 
        String textoContrato, 
        UUID empresaId, 
        UUID devedorId, 
        ZonedDateTime vencimentoContrato,
        StatusContrato status
) {
}
