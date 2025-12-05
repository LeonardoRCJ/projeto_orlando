package tech.devleo.projeto_orlando.domain;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tech.devleo.projeto_orlando.domain.Pk.PagamentoId;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
public class Pagamento {

    // Substitui o ID antigo pela chave composta
    @EmbeddedId
    private PagamentoId id;
    
    @Column(nullable = false)
    private Double valor;
    
    @Enumerated(EnumType.STRING) // Boa prática: definir o tipo do Enum no banco
    private MetodoPagamento metodo;
    
    private ZonedDateTime dataPagamento;
    
    @OneToOne
    @MapsId("dividaId") // Mapeia o "dividaId" do PagamentoId para esta relação
    @JoinColumn(name = "divida_id")
    private Divida divida;
    
    @ManyToOne
    @MapsId("contaId") // Mapeia o "contaId" do PagamentoId para esta relação
    @JoinColumn(name = "conta_id")
    private Conta conta;

    @PrePersist
    protected void onCreate() {
        if (dataPagamento == null) {
            dataPagamento = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"));
        }
        // Garante que o ID seja instanciado antes de salvar
        if (this.id == null && this.divida != null && this.conta != null) {
            this.id = new PagamentoId(this.divida.getId(), this.conta.getId());
        }
    }
}