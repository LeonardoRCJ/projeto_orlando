package tech.devleo.projeto_orlando.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table
@Getter
@Setter
public class Divida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Double valor;

    private ZonedDateTime dataCriacao;

    @ManyToOne
    @JoinColumn(name = "conta_id")
    private Conta conta;

    @ManyToOne
    @JoinColumn(name = "fiadora_id")
    private Empresa fiadora;

    @ManyToOne
    @JoinColumn(name = "contrato_id")
    private Contrato contrato;

    @PrePersist
    protected void onCreate() {
        if (dataCriacao == null) {
            dataCriacao = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"));
        }
    }
}
