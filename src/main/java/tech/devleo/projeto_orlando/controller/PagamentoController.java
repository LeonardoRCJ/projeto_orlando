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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import tech.devleo.projeto_orlando.domain.MetodoPagamento;
import tech.devleo.projeto_orlando.dto.ErrorResponse;
import tech.devleo.projeto_orlando.dto.PagamentoRequest;
import tech.devleo.projeto_orlando.dto.PagamentoResponse;
import tech.devleo.projeto_orlando.service.PagamentoService;

@RestController
@RequestMapping("/api/pagamentos")
@Tag(name = "Pagamentos", description = "Gerenciamento de pagamentos com Chave Composta (Divida + Conta)")
@SecurityRequirement(name = "Bearer Authentication")
public class PagamentoController {

    private final PagamentoService service;

    public PagamentoController(PagamentoService service) {
        this.service = service;
    }

    @Operation(summary = "Listar todos os pagamentos", description = "Retorna todos os pagamentos da empresa logada")
    @GetMapping
    public ResponseEntity<List<PagamentoResponse>> all() {
        return ResponseEntity.ok(service.findAll());
    }

    @Operation(summary = "Buscar pagamento por ID Composto", 
               description = "Busca um pagamento específico usando a combinação de ID da Dívida e ID da Conta")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pagamento encontrado"),
        @ApiResponse(responseCode = "404", description = "Pagamento não encontrado", 
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{dividaId}/{contaId}")
    public ResponseEntity<PagamentoResponse> get(
            @Parameter(description = "ID da Dívida", example = "1") 
            @PathVariable Integer dividaId, 
            
            @Parameter(description = "ID da Conta (UUID)", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11") 
            @PathVariable UUID contaId) {
        return ResponseEntity.ok(service.findById(dividaId, contaId));
    }

    @Operation(summary = "Contar pagamentos por método", description = "Estatísticas de uso de métodos de pagamento")
    @GetMapping("/stats/count-by-metodo")
    public ResponseEntity<Long> countByMetodo(
            @Parameter(description = "Método para filtrar", example = "PIX") 
            @RequestParam MetodoPagamento metodo) {
        return ResponseEntity.ok(service.countByMetodo(metodo));
    }

    @Operation(summary = "Criar novo pagamento", 
               description = "O valor é herdado automaticamente da dívida informada.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Pagamento criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Erro de validação ou pagamento já existente", 
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<PagamentoResponse> create(@Valid @RequestBody PagamentoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @Operation(summary = "Atualizar pagamento", 
               description = "Atualiza o método de pagamento. A dívida e conta não podem ser alteradas pois compõem o ID.")
    @PutMapping("/{dividaId}/{contaId}")
    public ResponseEntity<PagamentoResponse> update(
            @Parameter(description = "ID da Dívida original", example = "1")
            @PathVariable Integer dividaId,
            
            @Parameter(description = "ID da Conta original", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
            @PathVariable UUID contaId,
            
            @Valid @RequestBody PagamentoRequest req) {
        return ResponseEntity.ok(service.update(dividaId, contaId, req));
    }

    @Operation(summary = "Deletar pagamento")
    @DeleteMapping("/{dividaId}/{contaId}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID da Dívida", example = "1")
            @PathVariable Integer dividaId, 
            
            @Parameter(description = "ID da Conta", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
            @PathVariable UUID contaId) {
        service.delete(dividaId, contaId);
        return ResponseEntity.noContent().build();
    }
}