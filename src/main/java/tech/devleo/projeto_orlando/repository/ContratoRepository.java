package tech.devleo.projeto_orlando.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import tech.devleo.projeto_orlando.domain.Contrato;
import tech.devleo.projeto_orlando.domain.Devedor;
import tech.devleo.projeto_orlando.domain.Empresa;

public interface ContratoRepository extends JpaRepository<Contrato, UUID> {
    List<Contrato> findByEmpresa(Empresa empresa);
    List<Contrato> findByDevedor(Devedor devedor);
}
