package tech.devleo.projeto_orlando.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.devleo.projeto_orlando.domain.Empresa;
import tech.devleo.projeto_orlando.domain.Relatorio;

public interface RelatorioRepository extends JpaRepository<Relatorio, Integer> {
    @Query("select r from Relatorio r left join r.conta c left join c.devedor d where (r.conta is null) or (d.empresa = :empresa)")
    List<Relatorio> findByEmpresa(@Param("empresa") Empresa empresa);
}
