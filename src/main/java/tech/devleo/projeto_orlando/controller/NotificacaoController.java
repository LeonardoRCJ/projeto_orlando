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
import tech.devleo.projeto_orlando.dto.ErrorResponse;
import tech.devleo.projeto_orlando.dto.NotificacaoRequest;
import tech.devleo.projeto_orlando.dto.NotificacaoResponse;
import tech.devleo.projeto_orlando.service.NotificacaoService;

@RestController
@RequestMapping("/api/notificacoes")
@Tag(name = "Notificações", description = "Endpoints para gerenciamento de notificações")
@SecurityRequirement(name = "Bearer Authentication")
public class NotificacaoController {

    private final NotificacaoService service;

    public NotificacaoController(NotificacaoService service) {
        this.service = service;
    }

    @Operation(summary = "Listar notificações", description = "Retorna todas as notificações da empresa do usuário autenticado")
    @ApiResponse(responseCode = "200", description = "Lista de notificações",
            content = @Content(schema = @Schema(implementation = NotificacaoResponse.class)))
    @GetMapping
    public ResponseEntity<List<NotificacaoResponse>> all() {
        return ResponseEntity.ok(service.findAll());
    }

    @Operation(summary = "Buscar notificação por ID", description = "Retorna uma notificação específica da empresa do usuário")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notificação encontrada",
                content = @Content(schema = @Schema(implementation = NotificacaoResponse.class))),
        @ApiResponse(responseCode = "404", description = "Notificação não encontrada",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Notificação não pertence à sua empresa",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<NotificacaoResponse> get(@PathVariable Integer id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(summary = "Criar notificação", description = "Cria uma nova notificação para a empresa do usuário")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Notificação criada com sucesso",
                content = @Content(schema = @Schema(implementation = NotificacaoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<NotificacaoResponse> create(@Valid @RequestBody NotificacaoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @Operation(summary = "Atualizar notificação", description = "Atualiza os dados de uma notificação da empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notificação atualizada com sucesso",
                content = @Content(schema = @Schema(implementation = NotificacaoResponse.class))),
        @ApiResponse(responseCode = "404", description = "Notificação não encontrada",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Notificação não pertence à sua empresa",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<NotificacaoResponse> update(@PathVariable Integer id, @Valid @RequestBody NotificacaoRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @Operation(summary = "Deletar notificação", description = "Remove uma notificação da empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Notificação deletada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Notificação não encontrada",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Notificação não pertence à sua empresa",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
