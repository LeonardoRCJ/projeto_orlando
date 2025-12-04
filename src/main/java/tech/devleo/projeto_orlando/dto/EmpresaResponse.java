package tech.devleo.projeto_orlando.dto;

import java.util.UUID;

public record EmpresaResponse(String name, String cnpj, String telefone, Integer totalDividas) {
}
