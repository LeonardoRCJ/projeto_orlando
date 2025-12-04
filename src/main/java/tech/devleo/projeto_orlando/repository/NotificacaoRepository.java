package tech.devleo.projeto_orlando.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import tech.devleo.projeto_orlando.domain.Empresa;
import tech.devleo.projeto_orlando.domain.Notificacao;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Integer> {
    List<Notificacao> findByEmpresa(Empresa empresa);
}
