package tech.devleo.projeto_orlando.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import tech.devleo.projeto_orlando.dto.EmpresaRequest;
import tech.devleo.projeto_orlando.dto.EmpresaResponse;
import tech.devleo.projeto_orlando.dto.ErrorResponse;
import tech.devleo.projeto_orlando.service.EmpresaService;

@RestController
@RequestMapping("/api/empresas")
@Tag(name = "Empresas", description = "Endpoints para gerenciamento de empresas")
@SecurityRequirement(name = "Bearer Authentication")
public class EmpresaController {

    private final EmpresaService service;

    public EmpresaController(EmpresaService service) {
        this.service = service;
    }

    @Operation(summary = "Obter minha empresa", description = "Retorna a empresa do usuário autenticado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Empresa encontrada",
                content = @Content(schema = @Schema(implementation = EmpresaResponse.class))),
        @ApiResponse(responseCode = "404", description = "Empresa não encontrada",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<EmpresaResponse> getMyEmpresa() {
        return ResponseEntity.ok(service.getMyEmpresa());
    }

    @Operation(summary = "Criar empresa", description = "Cria uma nova empresa para o usuário autenticado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Empresa criada com sucesso",
                content = @Content(schema = @Schema(implementation = EmpresaResponse.class))),
        @ApiResponse(responseCode = "400", description = "Usuário já possui uma empresa ou dados inválidos",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<EmpresaResponse> create(@Valid @RequestBody EmpresaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @Operation(summary = "Atualizar empresa", description = "Atualiza os dados da empresa do usuário autenticado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Empresa atualizada com sucesso",
                content = @Content(schema = @Schema(implementation = EmpresaResponse.class))),
        @ApiResponse(responseCode = "404", description = "Empresa não encontrada",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping
    public ResponseEntity<EmpresaResponse> update(@Valid @RequestBody EmpresaRequest req) {
        return ResponseEntity.ok(service.update(req));
    }

    @Operation(
        summary = "Deletar empresa", 
        description = "Remove a empresa do usuário autenticado. " +
                     "Ao deletar uma empresa, todos os devedores, contratos, dívidas e pagamentos associados serão deletados automaticamente em cascata."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Empresa deletada com sucesso (incluindo todos os dados relacionados)"),
        @ApiResponse(responseCode = "404", description = "Empresa não encontrada",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping
    public ResponseEntity<Void> delete() {
        service.delete();
        return ResponseEntity.noContent().build();
    }
}
