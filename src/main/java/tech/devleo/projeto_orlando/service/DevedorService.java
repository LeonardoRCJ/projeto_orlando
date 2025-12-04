package tech.devleo.projeto_orlando.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import tech.devleo.projeto_orlando.domain.Conta;
import tech.devleo.projeto_orlando.domain.Devedor;
import tech.devleo.projeto_orlando.domain.Empresa;
import tech.devleo.projeto_orlando.dto.DevedorRequest;
import tech.devleo.projeto_orlando.dto.DevedorResponse;
import tech.devleo.projeto_orlando.repository.ContaRepository;
import tech.devleo.projeto_orlando.repository.ContratoRepository;
import tech.devleo.projeto_orlando.repository.DevedorRepository;

@Service
public class DevedorService {

    private final DevedorRepository repository;
    private final ContaRepository contaRepository;
    private final ContratoRepository contratoRepository;
    private final EmpresaService empresaService;

    public DevedorService(DevedorRepository repository, ContaRepository contaRepository, ContratoRepository contratoRepository, EmpresaService empresaService) {
        this.repository = repository;
        this.contaRepository = contaRepository;
        this.contratoRepository = contratoRepository;
        this.empresaService = empresaService;
    }

    public List<DevedorResponse> findAll() {
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        return repository.findByEmpresa(empresa).stream()
                .map(d -> new DevedorResponse(d.getId(), d.getName(), d.getCpf(), d.getEmail(), d.getConta().getSaldo()))
                .collect(Collectors.toList());
    }

    public DevedorResponse findById(String id) {
        UUID uuid = UUID.fromString(id);
        Devedor d = repository.findById(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        
        // Verificar se o devedor pertence à empresa do usuário atual
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        if (!d.getEmpresa().getId().equals(empresa.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Devedor não pertence à sua empresa");
        }
        
        return new DevedorResponse(d.getId(), d.getName(), d.getCpf(), d.getEmail(), d.getConta().getSaldo());
    }

    @Transactional
    public DevedorResponse create(DevedorRequest req) {
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        
        Devedor d = new Devedor();
        d.setName(req.name());
        d.setCpf(req.cpf());
        d.setEmail(req.email());
        d.setEmpresa(empresa);
        
        // Auto-criar Conta associada ao Devedor antes de salvar
        // A conta nasce sem dívidas, logo, saldo zero (calculado dinamicamente)
        Conta c = new Conta();
        c.setDevedor(d);
        d.setConta(c);
        
        d = repository.save(d);

        return new DevedorResponse(d.getId(), d.getName(), d.getCpf(), d.getEmail(), d.getConta().getSaldo());
    }

    @Transactional
    public DevedorResponse update(String id, DevedorRequest req) {
        UUID uuid = UUID.fromString(id);
        Devedor d = repository.findById(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        
        // Verificar se o devedor pertence à empresa do usuário atual
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        if (!d.getEmpresa().getId().equals(empresa.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Devedor não pertence à sua empresa");
        }
        
        d.setName(req.name());
        d.setCpf(req.cpf());
        d.setEmail(req.email());
        repository.save(d);
        return new DevedorResponse(d.getId(), d.getName(), d.getCpf(), d.getEmail(), d.getConta().getSaldo());
    }

    @Transactional
    public void delete(String id) {
        UUID uuid = UUID.fromString(id);
        Devedor d = repository.findById(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Devedor não encontrado"));
        
        // Verificar se o devedor pertence à empresa do usuário atual
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        if (!d.getEmpresa().getId().equals(empresa.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Devedor não pertence à sua empresa");
        }
        
        // Com cascade configurado, os contratos e suas dívidas serão deletados automaticamente
        // quando o devedor for deletado
        repository.deleteById(uuid);
    }
}
