package tech.devleo.projeto_orlando.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import tech.devleo.projeto_orlando.domain.Devedor;
import tech.devleo.projeto_orlando.domain.Empresa;

public interface DevedorRepository extends JpaRepository<Devedor, UUID> {
    List<Devedor> findByEmpresa(Empresa empresa);
}
