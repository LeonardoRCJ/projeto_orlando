package tech.devleo.projeto_orlando.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record NotificacaoRequest(
	@NotBlank(message = "mensagem is required") String mensagem,
	@Email(message = "invalid email") @NotBlank(message = "email is required") String email
) {
}
