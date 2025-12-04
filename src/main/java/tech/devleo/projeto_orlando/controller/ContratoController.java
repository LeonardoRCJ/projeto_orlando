package tech.devleo.projeto_orlando.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import tech.devleo.projeto_orlando.dto.ContratoRequest;
import tech.devleo.projeto_orlando.dto.ContratoResponse;
import tech.devleo.projeto_orlando.dto.ErrorResponse;
import tech.devleo.projeto_orlando.service.ContratoService;

@RestController
@RequestMapping("/api/contratos")
@Tag(name = "Contratos", description = "Endpoints para gerenciamento de contratos")
@SecurityRequirement(name = "Bearer Authentication")
public class ContratoController {

    private final ContratoService service;

    public ContratoController(ContratoService service) {
        this.service = service;
    }

    @Operation(
        summary = "Listar contratos", 
        description = "Retorna todos os contratos da empresa do usuário autenticado. " +
                     "Cada contrato inclui seu status (RASCUNHO, ATIVO, CONCLUIDO, CANCELADO) e data de vencimento."
    )
    @ApiResponse(responseCode = "200", description = "Lista de contratos",
            content = @Content(schema = @Schema(implementation = ContratoResponse.class)))
    @GetMapping
    public ResponseEntity<List<ContratoResponse>> all() {
        return ResponseEntity.ok(service.findAll());
    }

    @Operation(
        summary = "Buscar contrato por ID", 
        description = "Retorna um contrato específico da empresa do usuário, incluindo status e data de vencimento"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contrato encontrado",
                content = @Content(schema = @Schema(implementation = ContratoResponse.class))),
        @ApiResponse(responseCode = "404", description = "Contrato não encontrado",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Contrato não pertence à sua empresa",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ContratoResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(
        summary = "Criar contrato", 
        description = "Cria um novo contrato entre a empresa e um devedor. " +
                     "O campo 'dataVencimento' é opcional: se não informado, será definido automaticamente como 1 ano a partir da data atual. " +
                     "O status inicial será definido como 'ATIVO' automaticamente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Contrato criado com sucesso",
                content = @Content(schema = @Schema(implementation = ContratoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Devedor não pertence à sua empresa",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Devedor não encontrado",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ContratoResponse> create(@Valid @RequestBody ContratoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @Operation(
        summary = "Atualizar contrato", 
        description = "Atualiza os dados de um contrato da empresa. " +
                     "O campo 'dataVencimento' é opcional e pode ser atualizado. " +
                     "Se informado, será convertido para o fim do dia no timezone 'America/Sao_Paulo'."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contrato atualizado com sucesso",
                content = @Content(schema = @Schema(implementation = ContratoResponse.class))),
        @ApiResponse(responseCode = "404", description = "Contrato não encontrado",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Contrato não pertence à sua empresa",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ContratoResponse> update(@PathVariable UUID id, @Valid @RequestBody ContratoRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @Operation(summary = "Deletar contrato", description = "Remove um contrato da empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Contrato deletado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Contrato não encontrado",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Contrato não pertence à sua empresa",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
