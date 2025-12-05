package tech.devleo.projeto_orlando.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record EmpresaRequest(
    @Schema(description = "Nome fantasia da empresa", example = "Cobranças Silva LTDA")
    @NotBlank String name,
    
    @Schema(description = "CNPJ (apenas números ou formatado)", example = "12.345.678/0001-90")
    @NotBlank String cnpj,
    
    @Schema(description = "Telefone de contato", example = "11999887766")
    @NotBlank String telefone
) {}