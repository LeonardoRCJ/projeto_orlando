package tech.devleo.projeto_orlando.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import tech.devleo.projeto_orlando.domain.Divida;
import tech.devleo.projeto_orlando.domain.Empresa;
import tech.devleo.projeto_orlando.domain.Pagamento;
import tech.devleo.projeto_orlando.domain.Pk.PagamentoId;
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
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Método auxiliar para converter entidade em resposta
    private PagamentoResponse toResponse(Pagamento p) {
        return new PagamentoResponse(
            p.getId().getDividaId(), 
            p.getId().getContaId(), 
            p.getMetodo()
        );
    }

    // Assinatura alterada para receber a chave composta
    public PagamentoResponse findById(Integer dividaId, UUID contaId) {
        PagamentoId id = new PagamentoId(dividaId, contaId);
        Pagamento p = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pagamento não encontrado"));

        validarPertencimentoEmpresa(p);

        return toResponse(p);
    }

    public long countByMetodo(tech.devleo.projeto_orlando.domain.MetodoPagamento metodo) {
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        return repository.countByMetodoAndEmpresa(metodo, empresa);
    }

    @Transactional
    public PagamentoResponse create(PagamentoRequest req) {
        Empresa empresa = empresaService.getEmpresaByCurrentUser();

        Divida divida = dividaRepository.findById(req.dividaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dívida não encontrada"));

        if (divida.getFiadora() == null || !divida.getFiadora().getId().equals(empresa.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Dívida não pertence à sua empresa");
        }
        
        // Verificar se já existe pagamento para esta dívida (regra 1 para 1 implícita pela chave composta)
        PagamentoId novoId = new PagamentoId(divida.getId(), divida.getConta().getId());
        if (repository.existsById(novoId)) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Já existe um pagamento para esta dívida.");
        }

        Pagamento p = new Pagamento();
        // Definir a chave composta manualmente
        p.setId(novoId);
        p.setMetodo(req.metodo());
        p.setDivida(divida);
        p.setConta(divida.getConta());
        p.setValor(divida.getValor());

        p = repository.save(p);
        return toResponse(p);
    }

    @Transactional
    public PagamentoResponse update(Integer dividaId, UUID contaId, PagamentoRequest req) {
        PagamentoId id = new PagamentoId(dividaId, contaId);
        Pagamento p = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pagamento não encontrado"));

        validarPertencimentoEmpresa(p);

        // Nota: Não permitimos alterar a 'Divida' ou 'Conta' aqui, pois isso mudaria o ID.
        // Se o usuário tentar passar um dividaId diferente no body, podemos ignorar ou lançar erro.
        // Aqui atualizaremos apenas dados mutáveis, como o método.
        
        p.setMetodo(req.metodo());
        
        // Se a regra de negócio permitir recalcular o valor baseada na dívida atual:
        p.setValor(p.getDivida().getValor()); 

        p = repository.save(p);
        return toResponse(p);
    }

    public void delete(Integer dividaId, UUID contaId) {
        PagamentoId id = new PagamentoId(dividaId, contaId);
        Pagamento p = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pagamento não encontrado"));

        validarPertencimentoEmpresa(p);

        repository.deleteById(id);
    }
    
    private void validarPertencimentoEmpresa(Pagamento p) {
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        if (p.getConta() == null || p.getConta().getDevedor() == null ||
                !p.getConta().getDevedor().getEmpresa().getId().equals(empresa.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pagamento não pertence à sua empresa");
        }
    }
}