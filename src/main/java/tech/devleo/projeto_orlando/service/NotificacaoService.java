package tech.devleo.projeto_orlando.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import tech.devleo.projeto_orlando.domain.Empresa;
import tech.devleo.projeto_orlando.domain.Notificacao;
import tech.devleo.projeto_orlando.dto.NotificacaoRequest;
import tech.devleo.projeto_orlando.dto.NotificacaoResponse;
import tech.devleo.projeto_orlando.repository.NotificacaoRepository;

@Service
public class NotificacaoService {

    private final NotificacaoRepository repository;
    private final EmpresaService empresaService;

    public NotificacaoService(NotificacaoRepository repository, EmpresaService empresaService) {
        this.repository = repository;
        this.empresaService = empresaService;
    }

    public List<NotificacaoResponse> findAll() {
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        return repository.findByEmpresa(empresa).stream()
                .map(n -> new NotificacaoResponse(n.getId(), n.getMensagem(), n.getEmail()))
                .collect(Collectors.toList());
    }

    public NotificacaoResponse findById(Integer id) {
        Notificacao n = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificação não encontrada"));
        
        // Verificar se a notificação pertence à empresa do usuário
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        if (n.getEmpresa() == null || !n.getEmpresa().getId().equals(empresa.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Notificação não pertence à sua empresa");
        }
        
        return new NotificacaoResponse(n.getId(), n.getMensagem(), n.getEmail());
    }

    @Transactional
    public NotificacaoResponse create(NotificacaoRequest req) {
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        
        Notificacao n = new Notificacao();
        n.setMensagem(req.mensagem());
        n.setEmail(req.email());
        n.setEmpresa(empresa);
        
        n = repository.save(n);
        return new NotificacaoResponse(n.getId(), n.getMensagem(), n.getEmail());
    }

    @Transactional
    public NotificacaoResponse update(Integer id, NotificacaoRequest req) {
        Notificacao n = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificação não encontrada"));
        
        // Verificar se a notificação pertence à empresa do usuário
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        if (n.getEmpresa() == null || !n.getEmpresa().getId().equals(empresa.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Notificação não pertence à sua empresa");
        }
        
        n.setMensagem(req.mensagem());
        n.setEmail(req.email());
        repository.save(n);
        
        return new NotificacaoResponse(n.getId(), n.getMensagem(), n.getEmail());
    }

    public void delete(Integer id) {
        Notificacao n = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificação não encontrada"));
        
        // Verificar se a notificação pertence à empresa do usuário
        Empresa empresa = empresaService.getEmpresaByCurrentUser();
        if (n.getEmpresa() == null || !n.getEmpresa().getId().equals(empresa.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Notificação não pertence à sua empresa");
        }
        
        repository.deleteById(id);
    }
}
