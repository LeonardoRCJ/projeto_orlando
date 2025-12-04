package tech.devleo.projeto_orlando.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import tech.devleo.projeto_orlando.domain.Empresa;
import tech.devleo.projeto_orlando.domain.User;
import tech.devleo.projeto_orlando.dto.EmpresaRequest;
import tech.devleo.projeto_orlando.dto.EmpresaResponse;
import tech.devleo.projeto_orlando.repository.EmpresaRepository;
import tech.devleo.projeto_orlando.repository.UserRepository;

@Service
public class EmpresaService {

    private final EmpresaRepository repository;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    public EmpresaService(EmpresaRepository repository, CurrentUserService currentUserService, UserRepository userRepository) {
        this.repository = repository;
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
    }

    public EmpresaResponse getMyEmpresa() {
        User currentUser = currentUserService.getCurrentUser();
        Empresa empresa = repository.findByDono(currentUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada. Crie uma empresa primeiro."));
        return toResponse(empresa);
    }

    @Transactional
    public EmpresaResponse create(EmpresaRequest req) {
        User currentUser = currentUserService.getCurrentUser();
        
        // Verificar se o usuário já tem uma empresa
        if (repository.existsByDono(currentUser)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário já possui uma empresa");
        }

        Empresa empresa = new Empresa();
        empresa.setName(req.name());
        empresa.setCnpj(req.cnpj());
        empresa.setTelefone(req.telefone());
        empresa.setDono(currentUser);
        
        empresa = repository.save(empresa);
        currentUser.setEmpresa(empresa);
        userRepository.save(currentUser);
        
        return toResponse(empresa);
    }

    @Transactional
    public EmpresaResponse update(EmpresaRequest req) {
        User currentUser = currentUserService.getCurrentUser();
        Empresa empresa = repository.findByDono(currentUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));
        
        empresa.setName(req.name());
        empresa.setCnpj(req.cnpj());
        empresa.setTelefone(req.telefone());
        empresa = repository.save(empresa);
        
        return toResponse(empresa);
    }

    @Transactional
    public void delete() {
        User currentUser = currentUserService.getCurrentUser();
        Empresa empresa = repository.findByDono(currentUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));
        
        UUID empresaId = empresa.getId();
        
        // Remover referência da empresa no usuário antes de deletar
        currentUser.setEmpresa(null);
        userRepository.save(currentUser);
        
        // Fazer flush para garantir que a atualização do usuário foi aplicada
        userRepository.flush();
        
        // Deletar a empresa usando deleteById para evitar problemas com o contexto de persistência
        // Os devedores, contratos e dívidas serão deletados em cascade conforme configurado
        repository.deleteById(empresaId);
    }

    public Empresa getEmpresaByCurrentUser() {
        User currentUser = currentUserService.getCurrentUser();
        return repository.findByDono(currentUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));
    }

    private EmpresaResponse toResponse(Empresa empresa) {
        return new EmpresaResponse(empresa.getName(), empresa.getCnpj(), empresa.getTelefone(), empresa.getDividasFiadas().size());
    }
}
