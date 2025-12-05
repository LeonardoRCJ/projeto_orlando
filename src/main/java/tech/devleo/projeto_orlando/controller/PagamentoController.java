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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import tech.devleo.projeto_orlando.domain.MetodoPagamento;
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

    @GetMapping
    public ResponseEntity<List<PagamentoResponse>> all() {
        return ResponseEntity.ok(service.findAll());
    }

    // URL agora inclui os dois IDs: /api/pagamentos/{dividaId}/{contaId}
    @Operation(summary = "Buscar pagamento por ID composto")
    @GetMapping("/{dividaId}/{contaId}")
    public ResponseEntity<PagamentoResponse> get(
            @PathVariable Integer dividaId, 
            @PathVariable UUID contaId) {
        return ResponseEntity.ok(service.findById(dividaId, contaId));
    }

    @GetMapping("/stats/count-by-metodo")
    public ResponseEntity<Long> countByMetodo(@RequestParam MetodoPagamento metodo) {
        return ResponseEntity.ok(service.countByMetodo(metodo));
    }

    @PostMapping
    public ResponseEntity<PagamentoResponse> create(@Valid @RequestBody PagamentoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @Operation(summary = "Atualizar pagamento")
    @PutMapping("/{dividaId}/{contaId}")
    public ResponseEntity<PagamentoResponse> update(
            @PathVariable Integer dividaId,
            @PathVariable UUID contaId,
            @Valid @RequestBody PagamentoRequest req) {
        return ResponseEntity.ok(service.update(dividaId, contaId, req));
    }

    @Operation(summary = "Deletar pagamento")
    @DeleteMapping("/{dividaId}/{contaId}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer dividaId, 
            @PathVariable UUID contaId) {
        service.delete(dividaId, contaId);
        return ResponseEntity.noContent().build();
    }
}