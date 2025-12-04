package tech.devleo.projeto_orlando.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.devleo.projeto_orlando.domain.Conta;
import tech.devleo.projeto_orlando.domain.Empresa;

public interface ContaRepository extends JpaRepository<Conta, UUID> {

	@Query("select c from Conta c join c.devedor d where d.empresa = :empresa")
	List<Conta> findByDevedorEmpresa(@Param("empresa") Empresa empresa);

	List<Conta> findByDevedorNameContainingIgnoreCase(String name);

	@Query("select c from Conta c where c.devedor.empresa = :empresa and (:devedorName is null or lower(c.devedor.name) like lower(concat('%', :devedorName, '%')))")
	List<Conta> findByFiltersAndEmpresa(@Param("devedorName") String devedorName, @Param("empresa") Empresa empresa);
}
