package tech.devleo.projeto_orlando.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import tech.devleo.projeto_orlando.domain.Empresa;
import tech.devleo.projeto_orlando.domain.User;

public interface EmpresaRepository extends JpaRepository<Empresa, UUID> {
    Optional<Empresa> findByDono(User dono);
    boolean existsByDono(User dono);
}
