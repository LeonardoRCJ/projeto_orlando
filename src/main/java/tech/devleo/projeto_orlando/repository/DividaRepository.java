package tech.devleo.projeto_orlando.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.devleo.projeto_orlando.domain.Divida;
import tech.devleo.projeto_orlando.domain.Empresa;

public interface DividaRepository extends JpaRepository<Divida, Integer> {

	@Query("select sum(d.valor) from Divida d where d.conta.id = :contaId and d.fiadora = :empresa")
	Double sumValorByContaIdAndEmpresa(@Param("contaId") UUID contaId, @Param("empresa") Empresa empresa);

	@Query("select count(d) from Divida d where d.fiadora = :empresa")
	Long countByFiadora(@Param("empresa") Empresa empresa);

	List<Divida> findByFiadora(Empresa empresa);

	// flexible filter: any parameter may be null
	@Query("select d from Divida d where d.fiadora = :empresa and (:min is null or d.valor >= :min) and (:max is null or d.valor <= :max) and (:contaId is null or d.conta.id = :contaId)")
	List<Divida> findByFiltersAndEmpresa(@Param("min") Double min, @Param("max") Double max, @Param("contaId") UUID contaId, @Param("empresa") Empresa empresa);

	@Query("select sum(d.valor) from Divida d where d.fiadora = :empresa and d.dataCriacao >= :inicio and d.dataCriacao <= :fim")
	Double sumValorByPeriodo(@Param("empresa") Empresa empresa, @Param("inicio") ZonedDateTime inicio, @Param("fim") ZonedDateTime fim);
}

