package tech.devleo.projeto_orlando.service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import tech.devleo.projeto_orlando.domain.Contrato;
import tech.devleo.projeto_orlando.domain.Devedor;
import tech.devleo.projeto_orlando.domain.Empresa;
import tech.devleo.projeto_orlando.domain.StatusContrato;
import tech.devleo.projeto_orlando.dto.ContratoRequest;
import tech.devleo.projeto_orlando.dto.ContratoResponse;
import tech.devleo.projeto_orlando.repository.ContratoRepository;
import tech.devleo.projeto_orlando.repository.DevedorRepository;

@Service
public class ContratoService {

    private final ContratoRepository repository;
    private final DevedorRepository devedorRepository;
    private final EmpresaService empresaService;

    public ContratoService(ContratoRepository repository, DevedorRepository devedorRepository, EmpresaService empresaService) {
        this.repository = repository;
        this.devedorRepository = devedorRepository;
        this.empresaService = empresaService;
    }

    public List<ContratoResponse> findAll() {
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        return repository.findByEmpresa(empresa).stream()
                .map(c -> new ContratoResponse(
                        c.getId(), 
                        c.getTexto_contrato(), 
                        c.getEmpresa() != null ? c.getEmpresa().getId() : null, 
                        c.getDevedor() != null ? c.getDevedor().getId() : null, 
                        c.getVencimentoContrato(),
                        c.getStatus()))
                .collect(Collectors.toList());
    }

    public ContratoResponse findById(UUID id) {
        Contrato c = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        
        // Verificar se o contrato pertence à empresa do usuário atual
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        if (!c.getEmpresa().getId().equals(empresa.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Contrato não pertence à sua empresa");
        }
        
        return new ContratoResponse(
                c.getId(), 
                c.getTexto_contrato(), 
                c.getEmpresa() != null ? c.getEmpresa().getId() : null, 
                c.getDevedor() != null ? c.getDevedor().getId() : null, 
                c.getVencimentoContrato(),
                c.getStatus());
    }

    @Transactional
    public ContratoResponse create(ContratoRequest req) {
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        
        UUID devedorUUID = UUID.fromString(req.devedorId());
        Devedor devedor = devedorRepository.findById(devedorUUID)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Devedor não encontrado"));
        
        // Verificar se o devedor pertence à empresa do usuário
        if (!devedor.getEmpresa().getId().equals(empresa.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Devedor não pertence à sua empresa");
        }
        
        Contrato contrato = new Contrato();
        contrato.setTexto_contrato(req.textoContrato());
        contrato.setEmpresa(empresa);
        contrato.setDevedor(devedor);
        
        // Definir data de vencimento
        ZoneId zoneId = ZoneId.of("America/Sao_Paulo");
        if (req.dataVencimento() != null) {
            // Se a data foi informada, usar ela (convertendo para ZonedDateTime, fim do dia)
            contrato.setVencimentoContrato(req.dataVencimento().atTime(LocalTime.MAX).atZone(zoneId));
        } else {
            // Se não foi informada, usar a regra padrão (data de hoje + 1 ano)
            contrato.setVencimentoContrato(ZonedDateTime.now(zoneId).plusYears(1));
        }
        
        // Definir status inicial como ATIVO
        contrato.setStatus(StatusContrato.ATIVO);
        
        contrato = repository.save(contrato);
        return new ContratoResponse(
                contrato.getId(), 
                contrato.getTexto_contrato(), 
                contrato.getEmpresa().getId(), 
                contrato.getDevedor().getId(), 
                contrato.getVencimentoContrato(),
                contrato.getStatus());
    }

    @Transactional
    public ContratoResponse update(UUID id, ContratoRequest req) {
        Contrato c = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        
        // Verificar se o contrato pertence à empresa do usuário atual
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        if (!c.getEmpresa().getId().equals(empresa.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Contrato não pertence à sua empresa");
        }
        
        UUID devedorUUID = UUID.fromString(req.devedorId());
        Devedor devedor = devedorRepository.findById(devedorUUID)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Devedor não encontrado"));
        
        // Verificar se o devedor pertence à empresa do usuário
        if (!devedor.getEmpresa().getId().equals(empresa.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Devedor não pertence à sua empresa");
        }
        
        c.setTexto_contrato(req.textoContrato());
        c.setDevedor(devedor);
        repository.save(c);
        
        return new ContratoResponse(
                c.getId(), 
                c.getTexto_contrato(), 
                c.getEmpresa().getId(), 
                c.getDevedor().getId(), 
                c.getVencimentoContrato(),
                c.getStatus());
    }

    public void delete(UUID id) {
        Contrato c = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        
        // Verificar se o contrato pertence à empresa do usuário atual
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        if (!c.getEmpresa().getId().equals(empresa.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Contrato não pertence à sua empresa");
        }
        
        repository.deleteById(id);
    }
}
