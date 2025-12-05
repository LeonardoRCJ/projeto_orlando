package tech.devleo.projeto_orlando.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import tech.devleo.projeto_orlando.domain.Conta;
import tech.devleo.projeto_orlando.domain.Empresa;
import tech.devleo.projeto_orlando.domain.Relatorio;
import tech.devleo.projeto_orlando.domain.TipoRelatorio;
import tech.devleo.projeto_orlando.dto.AuditoriaResponse;
import tech.devleo.projeto_orlando.dto.RelatorioRequest;
import tech.devleo.projeto_orlando.dto.RelatorioResponse;
import tech.devleo.projeto_orlando.repository.ContaRepository;
import tech.devleo.projeto_orlando.repository.DividaRepository;
import tech.devleo.projeto_orlando.repository.PagamentoRepository;
import tech.devleo.projeto_orlando.repository.RelatorioRepository;

@Service
public class RelatorioService {

    private final RelatorioRepository repository;
    private final ContaRepository contaRepository;
    private final DividaRepository dividaRepository;
    private final PagamentoRepository pagamentoRepository;
    private final EmpresaService empresaService;

    public RelatorioService(RelatorioRepository repository, ContaRepository contaRepository, DividaRepository dividaRepository, PagamentoRepository pagamentoRepository, EmpresaService empresaService) {
        this.repository = repository;
        this.contaRepository = contaRepository;
        this.dividaRepository = dividaRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.empresaService = empresaService;
    }

    public List<RelatorioResponse> findAll() {
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        return repository.findByEmpresa(empresa).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public RelatorioResponse findById(Integer id) {
        Relatorio r = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Relatório não encontrado"));
        
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        if (r.getConta() != null && r.getConta().getDevedor() != null) {
            if (!r.getConta().getDevedor().getEmpresa().getId().equals(empresa.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Relatório não pertence à sua empresa");
            }
        }
        
        return toResponse(r);
    }
    
    private RelatorioResponse toResponse(Relatorio r) {
        return new RelatorioResponse(
            r.getId(),
            r.getTipo(),
            r.getValorMovimentado(),
            r.getTotalDividas(),
            r.getTotalPagamentos(),
            r.getQuantidadeDividas(),
            r.getQuantidadePagamentos(),
            r.getQuantidadeContas(),
            r.getDescricao(),
            r.getConta() != null ? r.getConta().getId() : null,
            r.getDataGeracao(),
            r.getDataInicio(),
            r.getDataFim()
        );
    }

    @Transactional
    public RelatorioResponse create(RelatorioRequest req) {
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        TipoRelatorio tipo = req.tipo() != null ? req.tipo() : TipoRelatorio.MANUAL;
        
        Relatorio r = new Relatorio();
        r.setTipo(tipo);
        r.setDescricao(req.descricao());
        
        ZoneId zoneId = ZoneId.of("America/Sao_Paulo");
        
        switch (tipo) {
            case MANUAL:
                if (req.valorMovimentado() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "valorMovimentado é obrigatório para relatório MANUAL");
                }
                r.setValorMovimentado(req.valorMovimentado());
                if (req.contaId() != null) {
                    setConta(r, req.contaId(), empresa);
                }
                break;
                
            case CONTA_ESPECIFICA:
                if (req.contaId() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "contaId é obrigatório para relatório CONTA_ESPECIFICA");
                }
                setConta(r, req.contaId(), empresa);
                gerarRelatorioContaEspecifica(r, empresa);
                break;
                
            case CONSOLIDADO_EMPRESA:
                gerarRelatorioConsolidado(r, empresa);
                break;
                
            case PERIODO:
                if (req.dataInicio() == null || req.dataFim() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dataInicio e dataFim são obrigatórios para relatório PERIODO");
                }
                r.setDataInicio(req.dataInicio().atStartOfDay(zoneId));
                r.setDataFim(req.dataFim().atTime(LocalTime.MAX).atZone(zoneId));
                gerarRelatorioPeriodo(r, empresa);
                break;
                
            case RECEBIMENTOS:
                if (req.dataInicio() == null || req.dataFim() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dataInicio e dataFim são obrigatórios para relatório RECEBIMENTOS");
                }
                r.setDataInicio(req.dataInicio().atStartOfDay(zoneId));
                r.setDataFim(req.dataFim().atTime(LocalTime.MAX).atZone(zoneId));
                gerarRelatorioRecebimentos(r, empresa);
                break;
        }
        
        r = repository.save(r);
        return toResponse(r);
    }
    
    private void setConta(Relatorio r, String contaId, Empresa empresa) {
        java.util.UUID contaUUID = java.util.UUID.fromString(contaId);
        Conta c = contaRepository.findById(contaUUID)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada"));
        
        if (c.getDevedor() == null || !c.getDevedor().getEmpresa().getId().equals(empresa.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Conta não pertence à sua empresa");
        }
        
        r.setConta(c);
    }
    
    private void gerarRelatorioContaEspecifica(Relatorio r, Empresa empresa) {
        Conta conta = r.getConta();
        
        Hibernate.initialize(conta.getDividas());
        Hibernate.initialize(conta.getPagamentos());
        
        Double totalDividas = conta.getDividas().stream()
                .mapToDouble(d -> d.getValor() != null ? d.getValor() : 0.0)
                .sum();
        
        Double totalPagamentos = conta.getPagamentos().stream()
                .mapToDouble(p -> p.getValor() != null ? p.getValor() : 0.0)
                .sum();
        
        BigDecimal saldo = conta.getSaldo();
        
        r.setTotalDividas(totalDividas);
        r.setTotalPagamentos(totalPagamentos);
        r.setQuantidadeDividas(conta.getDividas().size());
        r.setQuantidadePagamentos(conta.getPagamentos().size());
        r.setValorMovimentado(saldo.doubleValue());
        
        if (r.getDescricao() == null) {
            r.setDescricao(String.format("Relatório da conta de %s - Saldo: R$ %.2f", 
                conta.getDevedor().getName(), saldo.doubleValue()));
        }
    }
    
    private void gerarRelatorioConsolidado(Relatorio r, Empresa empresa) {
        List<Conta> contas = contaRepository.findByDevedorEmpresa(empresa);
        
        contas.forEach(c -> {
            Hibernate.initialize(c.getDividas());
            Hibernate.initialize(c.getPagamentos());
        });
        
        Double totalDividas = 0.0;
        Double totalPagamentos = 0.0;
        int qtdDividas = 0;
        int qtdPagamentos = 0;
        Double saldoTotal = 0.0;
        
        for (Conta conta : contas) {
            totalDividas += conta.getDividas().stream()
                    .mapToDouble(d -> d.getValor() != null ? d.getValor() : 0.0)
                    .sum();
            
            totalPagamentos += conta.getPagamentos().stream()
                    .mapToDouble(p -> p.getValor() != null ? p.getValor() : 0.0)
                    .sum();
            
            qtdDividas += conta.getDividas().size();
            qtdPagamentos += conta.getPagamentos().size();
            saldoTotal += conta.getSaldo().doubleValue();
        }
        
        r.setTotalDividas(totalDividas);
        r.setTotalPagamentos(totalPagamentos);
        r.setQuantidadeDividas(qtdDividas);
        r.setQuantidadePagamentos(qtdPagamentos);
        r.setQuantidadeContas(contas.size());
        r.setValorMovimentado(saldoTotal);
        
        if (r.getDescricao() == null) {
            r.setDescricao(String.format("Relatório consolidado - %d contas - Saldo total: R$ %.2f", 
                contas.size(), saldoTotal));
        }
    }
    
    private void gerarRelatorioPeriodo(Relatorio r, Empresa empresa) {
        Double totalDividas = dividaRepository.sumValorByPeriodo(empresa, r.getDataInicio(), r.getDataFim());
        Double totalPagamentos = pagamentoRepository.sumValorByPeriodo(empresa, r.getDataInicio(), r.getDataFim());
        Long qtdPagamentos = pagamentoRepository.countByPeriodo(empresa, r.getDataInicio(), r.getDataFim());
        
        List<tech.devleo.projeto_orlando.domain.Divida> dividas = dividaRepository.findByFiadora(empresa).stream()
                .filter(d -> d.getDataCriacao() != null && 
                        !d.getDataCriacao().isBefore(r.getDataInicio()) && 
                        !d.getDataCriacao().isAfter(r.getDataFim()))
                .toList();
        
        r.setTotalDividas(totalDividas != null ? totalDividas : 0.0);
        r.setTotalPagamentos(totalPagamentos != null ? totalPagamentos : 0.0);
        r.setQuantidadeDividas(dividas.size());
        r.setQuantidadePagamentos(qtdPagamentos != null ? qtdPagamentos.intValue() : 0);
        r.setValorMovimentado((totalDividas != null ? totalDividas : 0.0) - (totalPagamentos != null ? totalPagamentos : 0.0));
        
        if (r.getDescricao() == null) {
            r.setDescricao(String.format("Relatório de período %s a %s", 
                r.getDataInicio().toLocalDate(), r.getDataFim().toLocalDate()));
        }
    }

    private void gerarRelatorioRecebimentos(Relatorio r, Empresa empresa) {
        Double totalRecebido = pagamentoRepository.sumValorByPeriodo(empresa, r.getDataInicio(), r.getDataFim());
        Long qtdPagamentos = pagamentoRepository.countByPeriodo(empresa, r.getDataInicio(), r.getDataFim());
        
        r.setTotalPagamentos(totalRecebido != null ? totalRecebido : 0.0);
        r.setQuantidadePagamentos(qtdPagamentos != null ? qtdPagamentos.intValue() : 0);
        r.setValorMovimentado(totalRecebido != null ? totalRecebido : 0.0);
        
        if (r.getDescricao() == null) {
            r.setDescricao(String.format("Relatório de recebimentos - Período: %s a %s - Total recebido: R$ %.2f", 
                r.getDataInicio().toLocalDate(), r.getDataFim().toLocalDate(), 
                totalRecebido != null ? totalRecebido : 0.0));
        }
    }

    @Transactional
    public RelatorioResponse update(Integer id, RelatorioRequest req) {
        Relatorio r = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Relatório não encontrado"));
        
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        if (r.getConta() != null && r.getConta().getDevedor() != null) {
            if (!r.getConta().getDevedor().getEmpresa().getId().equals(empresa.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Relatório não pertence à sua empresa");
            }
        }
        
        if (req.descricao() != null) {
            r.setDescricao(req.descricao());
        }
        
        if (r.getTipo() == TipoRelatorio.MANUAL && req.valorMovimentado() != null) {
            r.setValorMovimentado(req.valorMovimentado());
        }
        
        r = repository.save(r);
        return toResponse(r);
    }

    public void delete(Integer id) {
        Relatorio r = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Relatório não encontrado"));
        
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        
        if (r.getConta() != null) {
            if (r.getConta().getDevedor() == null || 
                !r.getConta().getDevedor().getEmpresa().getId().equals(empresa.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Relatório não pertence à sua empresa");
            }
        }
        
        repository.deleteById(id);
    }

    public AuditoriaResponse gerarAuditoria(LocalDate inicio, LocalDate fim) {
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        
        ZoneId zoneId = ZoneId.of("America/Sao_Paulo");
        ZonedDateTime inicioZoned = inicio.atStartOfDay(zoneId);
        ZonedDateTime fimZoned = fim.atTime(LocalTime.MAX).atZone(zoneId);
        
        Double valorTotalDividas = dividaRepository.sumValorByPeriodo(empresa, inicioZoned, fimZoned);
        Long totalPagamentos = pagamentoRepository.countByPeriodo(empresa, inicioZoned, fimZoned);
        
        return new AuditoriaResponse(
                valorTotalDividas != null ? valorTotalDividas : 0.0,
                totalPagamentos != null ? totalPagamentos : 0L,
                inicio.toString(),
                fim.toString()
        );
    }
}