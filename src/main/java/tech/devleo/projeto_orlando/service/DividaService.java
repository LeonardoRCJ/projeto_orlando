package tech.devleo.projeto_orlando.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import tech.devleo.projeto_orlando.domain.Conta;
import tech.devleo.projeto_orlando.domain.Divida;
import tech.devleo.projeto_orlando.domain.Empresa;
import tech.devleo.projeto_orlando.dto.DividaRequest;
import tech.devleo.projeto_orlando.dto.DividaResponse;
import tech.devleo.projeto_orlando.repository.ContaRepository;
import tech.devleo.projeto_orlando.repository.DividaRepository;

@Service
public class DividaService {

    private final DividaRepository repository;
    private final ContaRepository contaRepository;
    private final EmpresaService empresaService;

    public DividaService(DividaRepository repository, ContaRepository contaRepository, EmpresaService empresaService) {
        this.repository = repository;
        this.contaRepository = contaRepository;
        this.empresaService = empresaService;
    }

    public List<DividaResponse> findAll() {
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        return repository.findByFiadora(empresa).stream()
                .map(d -> new DividaResponse(d.getId(), d.getValor(), d.getConta() != null ? d.getConta().getId() : null, d.getFiadora() != null ? d.getFiadora().getId() : null))
                .collect(Collectors.toList());
    }

    // Aggregation: sum of values by conta da empresa
    public Double sumValorByConta(java.util.UUID contaId) {
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        return repository.sumValorByContaIdAndEmpresa(contaId, empresa);
    }

    // Aggregation: count of dividas by fiadora (empresa atual)
    public Long countByFiadora() {
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        return repository.countByFiadora(empresa);
    }

    // Multi-criteria search using repository query with optional params
    public List<DividaResponse> search(Double minValor, Double maxValor, java.util.UUID contaId) {
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        return repository.findByFiltersAndEmpresa(minValor, maxValor, contaId, empresa).stream()
                .map(d -> new DividaResponse(d.getId(), d.getValor(), d.getConta() != null ? d.getConta().getId() : null, d.getFiadora() != null ? d.getFiadora().getId() : null))
                .collect(Collectors.toList());
    }

    public DividaResponse findById(Integer id) {
        Divida d = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dívida não encontrada"));
        
        // Verificar se a dívida pertence à empresa do usuário
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        if (d.getFiadora() == null || !d.getFiadora().getId().equals(empresa.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Dívida não pertence à sua empresa");
        }
        
        return new DividaResponse(d.getId(), d.getValor(), d.getConta() != null ? d.getConta().getId() : null, d.getFiadora() != null ? d.getFiadora().getId() : null);
    }

    @Transactional
    public DividaResponse create(DividaRequest req) {
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        
        Divida d = new Divida();
        d.setValor(req.valor());
        d.setFiadora(empresa);
        
        if (req.contaId() != null) {
            java.util.UUID contaUUID = java.util.UUID.fromString(req.contaId());
            Conta c = contaRepository.findById(contaUUID)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada"));
            
            // Verificar se a conta pertence à empresa
            if (c.getDevedor() == null || !c.getDevedor().getEmpresa().getId().equals(empresa.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Conta não pertence à sua empresa");
            }
            
            d.setConta(c);
        }
        
        d = repository.save(d);
        return new DividaResponse(d.getId(), d.getValor(), d.getConta() != null ? d.getConta().getId() : null, d.getFiadora().getId());
    }

    @Transactional
    public DividaResponse update(Integer id, DividaRequest req) {
        Divida d = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dívida não encontrada"));
        
        // Verificar se a dívida pertence à empresa do usuário
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        if (d.getFiadora() == null || !d.getFiadora().getId().equals(empresa.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Dívida não pertence à sua empresa");
        }
        
        d.setValor(req.valor());
        
        if (req.contaId() != null) {
            java.util.UUID contaUUID = java.util.UUID.fromString(req.contaId());
            Conta c = contaRepository.findById(contaUUID)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada"));
            
            // Verificar se a conta pertence à empresa
            if (c.getDevedor() == null || !c.getDevedor().getEmpresa().getId().equals(empresa.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Conta não pertence à sua empresa");
            }
            
            d.setConta(c);
        }
        
        d = repository.save(d);
        return new DividaResponse(d.getId(), d.getValor(), d.getConta() != null ? d.getConta().getId() : null, d.getFiadora().getId());
    }

    public void delete(Integer id) {
        Divida d = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dívida não encontrada"));
        
        // Verificar se a dívida pertence à empresa do usuário
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        if (d.getFiadora() == null || !d.getFiadora().getId().equals(empresa.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Dívida não pertence à sua empresa");
        }
        
        repository.deleteById(id);
    }
}
