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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Pagamentos", description = "Endpoints para gerenciamento de pagamentos")
@SecurityRequirement(name = "Bearer Authentication")
public class PagamentoController {

    private final PagamentoService service;

    public PagamentoController(PagamentoService service) {
        this.service = service;
    }

    @Operation(summary = "Listar pagamentos", description = "Retorna todos os pagamentos das contas da empresa do usuário autenticado")
    @ApiResponse(responseCode = "200", description = "Lista de pagamentos",
            content = @Content(schema = @Schema(implementation = PagamentoResponse.class)))
    @GetMapping
    public ResponseEntity<List<PagamentoResponse>> all() {
        return ResponseEntity.ok(service.findAll());
    }

    @Operation(summary = "Buscar pagamento por ID", description = "Retorna um pagamento específico da empresa do usuário")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pagamento encontrado",
                content = @Content(schema = @Schema(implementation = PagamentoResponse.class))),
        @ApiResponse(responseCode = "404", description = "Pagamento não encontrado",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Pagamento não pertence à sua empresa",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<PagamentoResponse> get(@PathVariable Integer id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(summary = "Contar pagamentos por método", description = "Retorna a quantidade de pagamentos por método de pagamento da empresa")
    @ApiResponse(responseCode = "200", description = "Quantidade de pagamentos",
            content = @Content(schema = @Schema(implementation = Long.class)))
    @GetMapping("/stats/count-by-metodo")
    public ResponseEntity<Long> countByMetodo(@RequestParam MetodoPagamento metodo) {
        return ResponseEntity.ok(service.countByMetodo(metodo));
    }

    @Operation(
        summary = "Criar pagamento", 
        description = "Cria um novo pagamento para uma dívida da empresa. " +
                     "O valor do pagamento é automaticamente herdado da dívida associada, " +
                     "portanto não é necessário informar o valor no request."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Pagamento criado com sucesso",
                content = @Content(schema = @Schema(implementation = PagamentoResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Dívida não pertence à sua empresa",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Dívida não encontrada",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<PagamentoResponse> create(@Valid @RequestBody PagamentoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @Operation(
        summary = "Atualizar pagamento", 
        description = "Atualiza os dados de um pagamento da empresa. " +
                     "O valor do pagamento é automaticamente atualizado com o valor da dívida associada."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pagamento atualizado com sucesso",
                content = @Content(schema = @Schema(implementation = PagamentoResponse.class))),
        @ApiResponse(responseCode = "404", description = "Pagamento ou dívida não encontrado",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Pagamento ou dívida não pertence à sua empresa",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<PagamentoResponse> update(@PathVariable Integer id, @Valid @RequestBody PagamentoRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @Operation(summary = "Deletar pagamento", description = "Remove um pagamento da empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Pagamento deletado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Pagamento não encontrado",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Pagamento não pertence à sua empresa",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
