package tech.devleo.projeto_orlando.controller;

import java.util.List;

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
import tech.devleo.projeto_orlando.dto.DevedorRequest;
import tech.devleo.projeto_orlando.dto.DevedorResponse;
import tech.devleo.projeto_orlando.dto.ErrorResponse;
import tech.devleo.projeto_orlando.service.DevedorService;

@RestController
@RequestMapping("/api/devedores")
@Tag(name = "Devedores", description = "Endpoints para gerenciamento de devedores")
@SecurityRequirement(name = "Bearer Authentication")
public class DevedorController {

    private final DevedorService service;

    public DevedorController(DevedorService service) {
        this.service = service;
    }

    @Operation(summary = "Listar devedores", description = "Retorna todos os devedores da empresa do usuário autenticado")
    @ApiResponse(responseCode = "200", description = "Lista de devedores",
            content = @Content(schema = @Schema(implementation = DevedorResponse.class)))
    @GetMapping
    public ResponseEntity<List<DevedorResponse>> all() {
        return ResponseEntity.ok(service.findAll());
    }

    @Operation(summary = "Buscar devedor por ID", description = "Retorna um devedor específico da empresa do usuário")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Devedor encontrado",
                content = @Content(schema = @Schema(implementation = DevedorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Devedor não encontrado",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Devedor não pertence à sua empresa",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<DevedorResponse> get(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(summary = "Criar devedor", description = "Cria um novo devedor para a empresa do usuário autenticado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Devedor criado com sucesso",
                content = @Content(schema = @Schema(implementation = DevedorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<DevedorResponse> create(@Valid @RequestBody DevedorRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @Operation(summary = "Atualizar devedor", description = "Atualiza os dados de um devedor da empresa do usuário")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Devedor atualizado com sucesso",
                content = @Content(schema = @Schema(implementation = DevedorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Devedor não encontrado",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Devedor não pertence à sua empresa",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<DevedorResponse> update(@PathVariable String id, @Valid @RequestBody DevedorRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @Operation(
        summary = "Deletar devedor", 
        description = "Remove um devedor da empresa do usuário. " +
                     "Ao deletar um devedor, todos os contratos associados e suas dívidas serão deletados automaticamente em cascata. " +
                     "A conta do devedor também será deletada automaticamente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Devedor deletado com sucesso (incluindo contratos, dívidas e conta associados)"),
        @ApiResponse(responseCode = "404", description = "Devedor não encontrado",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Devedor não pertence à sua empresa",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
