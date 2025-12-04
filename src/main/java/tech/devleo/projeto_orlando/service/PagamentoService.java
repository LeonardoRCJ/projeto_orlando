package tech.devleo.projeto_orlando.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import tech.devleo.projeto_orlando.domain.Divida;
import tech.devleo.projeto_orlando.domain.Empresa;
import tech.devleo.projeto_orlando.domain.Pagamento;
import tech.devleo.projeto_orlando.dto.PagamentoRequest;
import tech.devleo.projeto_orlando.dto.PagamentoResponse;
import tech.devleo.projeto_orlando.repository.DividaRepository;
import tech.devleo.projeto_orlando.repository.PagamentoRepository;

@Service
public class PagamentoService {

    private final PagamentoRepository repository;
    private final DividaRepository dividaRepository;
    private final EmpresaService empresaService;

    public PagamentoService(PagamentoRepository repository, DividaRepository dividaRepository,
            EmpresaService empresaService) {
        this.repository = repository;
        this.dividaRepository = dividaRepository;
        this.empresaService = empresaService;
    }

    public List<PagamentoResponse> findAll() {
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        return repository.findByEmpresa(empresa).stream()
                .map(p -> new PagamentoResponse(p.getId(), p.getMetodo(),
                        p.getConta() != null ? p.getConta().getId() : null))
                .collect(Collectors.toList());
    }

    public PagamentoResponse findById(Integer id) {
        Pagamento p = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pagamento não encontrado"));

        // Verificar se o pagamento pertence à empresa do usuário
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        if (p.getConta() == null || p.getConta().getDevedor() == null ||
                !p.getConta().getDevedor().getEmpresa().getId().equals(empresa.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pagamento não pertence à sua empresa");
        }

        return new PagamentoResponse(p.getId(), p.getMetodo(), p.getConta() != null ? p.getConta().getId() : null);
    }

    // Aggregation: count by payment method da empresa
    public long countByMetodo(tech.devleo.projeto_orlando.domain.MetodoPagamento metodo) {
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        return repository.countByMetodoAndEmpresa(metodo, empresa);
    }

    @Transactional
    public PagamentoResponse create(PagamentoRequest req) {
        Empresa empresa = empresaService.getEmpresaByCurrentUser();

        // Buscar a Dívida pelo ID recebido
        Divida divida = dividaRepository.findById(req.dividaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dívida não encontrada"));

        // Validar se a Dívida pertence à Empresa do usuário logado
        if (divida.getFiadora() == null || !divida.getFiadora().getId().equals(empresa.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Dívida não pertence à sua empresa");
        }

        // Criar o novo Pagamento
        Pagamento p = new Pagamento();
        p.setMetodo(req.metodo());
        p.setDivida(divida);

        // Associar a Conta (pegando divida.getConta())
        p.setConta(divida.getConta());
        
        // O valor do pagamento é automaticamente herdado da dívida
        p.setValor(divida.getValor());

        p = repository.save(p);
        return new PagamentoResponse(p.getId(), p.getMetodo(), p.getConta() != null ? p.getConta().getId() : null);
    }

    @Transactional
    public PagamentoResponse update(Integer id, PagamentoRequest req) {
        Pagamento p = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pagamento não encontrado"));

        // Verificar se o pagamento pertence à empresa do usuário
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        if (p.getConta() == null || p.getConta().getDevedor() == null ||
                !p.getConta().getDevedor().getEmpresa().getId().equals(empresa.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pagamento não pertence à sua empresa");
        }

        p.setMetodo(req.metodo());

        // Buscar a nova Dívida se fornecida
        Divida divida = dividaRepository.findById(req.dividaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dívida não encontrada"));

        // Validar se a Dívida pertence à Empresa do usuário logado
        if (divida.getFiadora() == null || !divida.getFiadora().getId().equals(empresa.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Dívida não pertence à sua empresa");
        }

        p.setDivida(divida);
        p.setConta(divida.getConta());
        
        // O valor do pagamento é automaticamente herdado da dívida
        p.setValor(divida.getValor());

        p = repository.save(p);
        return new PagamentoResponse(p.getId(), p.getMetodo(), p.getConta() != null ? p.getConta().getId() : null);
    }

    public void delete(Integer id) {
        Pagamento p = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pagamento não encontrado"));

        // Verificar se o pagamento pertence à empresa do usuário
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        if (p.getConta() == null || p.getConta().getDevedor() == null ||
                !p.getConta().getDevedor().getEmpresa().getId().equals(empresa.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pagamento não pertence à sua empresa");
        }

        repository.deleteById(id);
    }
}
