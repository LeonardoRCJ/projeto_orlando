package tech.devleo.projeto_orlando.domain;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
public class Relatorio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(nullable = false)
    private TipoRelatorio tipo = TipoRelatorio.MANUAL;

    private Double valorMovimentado;

    // Campos calculados para relatórios automáticos
    private Double totalDividas;
    private Double totalPagamentos;
    private Integer quantidadeDividas;
    private Integer quantidadePagamentos;
    private Integer quantidadeContas;
    private String descricao;

    @ManyToOne
    @JoinColumn(name = "conta_id")
    private Conta conta;

    private ZonedDateTime dataGeracao = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"));
    
    // Período do relatório (para relatórios de período)
    private ZonedDateTime dataInicio;
    private ZonedDateTime dataFim;

}