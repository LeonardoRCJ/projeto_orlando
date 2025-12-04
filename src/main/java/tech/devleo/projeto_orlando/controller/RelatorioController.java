package tech.devleo.projeto_orlando.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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
import tech.devleo.projeto_orlando.dto.AuditoriaResponse;
import tech.devleo.projeto_orlando.dto.ErrorResponse;
import tech.devleo.projeto_orlando.dto.RelatorioRequest;
import tech.devleo.projeto_orlando.dto.RelatorioResponse;
import tech.devleo.projeto_orlando.service.RelatorioService;

@RestController
@RequestMapping("/api/relatorios")
@Tag(name = "Relatórios", description = "Endpoints para gerenciamento de relatórios")
@SecurityRequirement(name = "Bearer Authentication")
public class RelatorioController {

    private final RelatorioService service;

    public RelatorioController(RelatorioService service) {
        this.service = service;
    }

    @Operation(summary = "Listar relatórios", description = "Retorna todos os relatórios das contas da empresa do usuário autenticado")
    @ApiResponse(responseCode = "200", description = "Lista de relatórios",
            content = @Content(schema = @Schema(implementation = RelatorioResponse.class)))
    @GetMapping
    public ResponseEntity<List<RelatorioResponse>> all() {
        return ResponseEntity.ok(service.findAll());
    }

    @Operation(summary = "Buscar relatório por ID", description = "Retorna um relatório específico da empresa do usuário")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Relatório encontrado",
                content = @Content(schema = @Schema(implementation = RelatorioResponse.class))),
        @ApiResponse(responseCode = "404", description = "Relatório não encontrado",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Relatório não pertence à sua empresa",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<RelatorioResponse> get(@PathVariable Integer id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(
        summary = "Criar relatório", 
        description = "Cria um novo relatório. Suporta vários tipos:\n" +
                     "- MANUAL: Relatório manual com valor informado\n" +
                     "- CONTA_ESPECIFICA: Relatório automático de uma conta (calcula saldo, dívidas, pagamentos)\n" +
                     "- CONSOLIDADO_EMPRESA: Relatório consolidado de todas as contas da empresa\n" +
                     "- PERIODO: Relatório de movimentações em um período (requer dataInicio e dataFim)\n" +
                     "- INADIMPLENCIA: Relatório de contas inadimplentes (opcional: valorMinimoInadimplencia)\n" +
                     "- RECEBIMENTOS: Relatório de recebimentos em um período (requer dataInicio e dataFim)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Relatório criado com sucesso",
                content = @Content(schema = @Schema(implementation = RelatorioResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou parâmetros obrigatórios faltando",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Conta não pertence à sua empresa",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Conta não encontrada",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<RelatorioResponse> create(@Valid @RequestBody RelatorioRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @Operation(summary = "Atualizar relatório", description = "Atualiza os dados de um relatório da empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Relatório atualizado com sucesso",
                content = @Content(schema = @Schema(implementation = RelatorioResponse.class))),
        @ApiResponse(responseCode = "404", description = "Relatório não encontrado",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Relatório não pertence à sua empresa",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<RelatorioResponse> update(@PathVariable Integer id, @Valid @RequestBody RelatorioRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @Operation(summary = "Deletar relatório", description = "Remove um relatório da empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Relatório deletado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Relatório não encontrado",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Relatório não pertence à sua empresa",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Gerar relatório de auditoria", description = "Gera relatório de auditoria com valor total de dívidas e total de pagamentos em um período")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Relatório de auditoria gerado com sucesso",
                content = @Content(schema = @Schema(implementation = AuditoriaResponse.class))),
        @ApiResponse(responseCode = "400", description = "Parâmetros inválidos",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/auditoria")
    public ResponseEntity<AuditoriaResponse> gerarAuditoria(
            @Parameter(description = "Data de início do período (formato: YYYY-MM-DD)", required = true, example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(description = "Data de fim do período (formato: YYYY-MM-DD)", required = true, example = "2024-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(service.gerarAuditoria(inicio, fim));
    }
}
