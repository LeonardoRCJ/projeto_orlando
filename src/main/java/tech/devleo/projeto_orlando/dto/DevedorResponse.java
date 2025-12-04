package tech.devleo.projeto_orlando.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DevedorResponse(UUID id, String name, String cpf, String email, BigDecimal contaSaldo) {
}
