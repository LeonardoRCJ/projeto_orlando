package tech.devleo.projeto_orlando.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.devleo.projeto_orlando.domain.Empresa;
import tech.devleo.projeto_orlando.domain.MetodoPagamento;
import tech.devleo.projeto_orlando.domain.Pagamento;
import tech.devleo.projeto_orlando.domain.Pk.PagamentoId;

public interface PagamentoRepository extends JpaRepository<Pagamento, PagamentoId> {

	@Query("select p from Pagamento p where p.conta.devedor.empresa = :empresa")
	List<Pagamento> findByEmpresa(@Param("empresa") Empresa empresa);

	@Query("select count(p) from Pagamento p where p.conta.devedor.empresa = :empresa and p.metodo = :metodo")
	long countByMetodoAndEmpresa(@Param("metodo") MetodoPagamento metodo, @Param("empresa") Empresa empresa);

	@Query("select count(p) from Pagamento p where p.divida.fiadora = :empresa and p.dataPagamento >= :inicio and p.dataPagamento <= :fim")
	Long countByPeriodo(@Param("empresa") Empresa empresa, @Param("inicio") ZonedDateTime inicio, @Param("fim") ZonedDateTime fim);

	@Query("select sum(p.valor) from Pagamento p where p.conta.devedor.empresa = :empresa and p.dataPagamento >= :inicio and p.dataPagamento <= :fim")
	Double sumValorByPeriodo(@Param("empresa") Empresa empresa, @Param("inicio") ZonedDateTime inicio, @Param("fim") ZonedDateTime fim);

	@Query("select sum(p.valor) from Pagamento p where p.conta.id = :contaId and p.conta.devedor.empresa = :empresa")
	Double sumValorByContaIdAndEmpresa(@Param("contaId") UUID contaId, @Param("empresa") Empresa empresa);

}
