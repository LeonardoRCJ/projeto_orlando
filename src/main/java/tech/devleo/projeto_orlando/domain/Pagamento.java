package tech.devleo.projeto_orlando.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
public class Pagamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false)
    private Double valor;
    
    private MetodoPagamento metodo;
    
    private ZonedDateTime dataPagamento;
    
    @OneToOne
    @JoinColumn(name = "divida_id")
    private Divida divida;
    
    @ManyToOne
    @JoinColumn(name = "conta_id")
    private Conta conta;

    @PrePersist
    protected void onCreate() {
        if (dataPagamento == null) {
            dataPagamento = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"));
        }
    }
}
