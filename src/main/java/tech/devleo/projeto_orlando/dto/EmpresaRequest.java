package tech.devleo.projeto_orlando.dto;

import jakarta.validation.constraints.NotBlank;

public record EmpresaRequest(
	@NotBlank(message = "name is required") String name,
	@NotBlank(message = "cnpj is required") String cnpj,
	@NotBlank(message = "telefone is required") String telefone
) {
}
