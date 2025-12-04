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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import tech.devleo.projeto_orlando.dto.DividaRequest;
import tech.devleo.projeto_orlando.dto.DividaResponse;
import tech.devleo.projeto_orlando.dto.ErrorResponse;
import tech.devleo.projeto_orlando.service.DividaService;

@RestController
@RequestMapping("/api/dividas")
@Tag(name = "Dívidas", description = "Endpoints para gerenciamento de dívidas")
@SecurityRequirement(name = "Bearer Authentication")
public class DividaController {

    private final DividaService service;

    public DividaController(DividaService service) {
        this.service = service;
    }

    @Operation(summary = "Listar dívidas", description = "Retorna todas as dívidas da empresa do usuário autenticado")
    @ApiResponse(responseCode = "200", description = "Lista de dívidas",
            content = @Content(schema = @Schema(implementation = DividaResponse.class)))
    @GetMapping
    public ResponseEntity<List<DividaResponse>> all() {
        return ResponseEntity.ok(service.findAll());
    }

    @Operation(summary = "Buscar dívida por ID", description = "Retorna uma dívida específica da empresa do usuário")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dívida encontrada",
                content = @Content(schema = @Schema(implementation = DividaResponse.class))),
        @ApiResponse(responseCode = "404", description = "Dívida não encontrada",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Dívida não pertence à sua empresa",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<DividaResponse> get(@PathVariable Integer id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(
        summary = "Buscar dívidas", 
        description = "Busca dívidas com filtros opcionais: valor mínimo, valor máximo e/ou conta específica"
    )
    @ApiResponse(responseCode = "200", description = "Lista de dívidas filtradas",
            content = @Content(schema = @Schema(implementation = DividaResponse.class)))
    @GetMapping("/search")
    public ResponseEntity<List<DividaResponse>> search(
            @io.swagger.v3.oas.annotations.Parameter(description = "Valor mínimo da dívida", example = "100.0")
            @RequestParam(required = false) Double min,
            @io.swagger.v3.oas.annotations.Parameter(description = "Valor máximo da dívida", example = "1000.0")
            @RequestParam(required = false) Double max,
            @io.swagger.v3.oas.annotations.Parameter(description = "ID da conta (UUID)", example = "123e4567-e89b-12d3-a456-426614174000")
            @RequestParam(required = false) UUID contaId) {
        return ResponseEntity.ok(service.search(min, max, contaId));
    }

    @Operation(summary = "Soma de valores por conta", description = "Retorna a soma dos valores das dívidas de uma conta específica")
    @ApiResponse(responseCode = "200", description = "Soma dos valores",
            content = @Content(schema = @Schema(implementation = Double.class)))
    @GetMapping("/stats/sum-by-conta/{contaId}")
    public ResponseEntity<Double> sumByConta(@PathVariable UUID contaId) {
        return ResponseEntity.ok(service.sumValorByConta(contaId));
    }

    @Operation(summary = "Contar dívidas", description = "Retorna o total de dívidas da empresa do usuário")
    @ApiResponse(responseCode = "200", description = "Total de dívidas",
            content = @Content(schema = @Schema(implementation = Long.class)))
    @GetMapping("/stats/count")
    public ResponseEntity<Long> countByFiadora() {
        return ResponseEntity.ok(service.countByFiadora());
    }

    @Operation(summary = "Criar dívida", description = "Cria uma nova dívida para uma conta da empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Dívida criada com sucesso",
                content = @Content(schema = @Schema(implementation = DividaResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Conta não pertence à sua empresa",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Conta não encontrada",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<DividaResponse> create(@Valid @RequestBody DividaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @Operation(summary = "Atualizar dívida", description = "Atualiza os dados de uma dívida da empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dívida atualizada com sucesso",
                content = @Content(schema = @Schema(implementation = DividaResponse.class))),
        @ApiResponse(responseCode = "404", description = "Dívida não encontrada",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Dívida não pertence à sua empresa",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<DividaResponse> update(@PathVariable Integer id, @Valid @RequestBody DividaRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @Operation(summary = "Deletar dívida", description = "Remove uma dívida da empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Dívida deletada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Dívida não encontrada",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Dívida não pertence à sua empresa",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
