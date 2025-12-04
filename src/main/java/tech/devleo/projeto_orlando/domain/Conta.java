package tech.devleo.projeto_orlando.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
public class Conta {

    @Id
    private UUID id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Devedor devedor;

    @OneToMany(mappedBy = "conta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Divida> dividas = new ArrayList<>();

    @OneToMany(mappedBy = "conta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pagamento> pagamentos = new ArrayList<>();

    /**
     * Calcula o saldo dinamicamente: (Soma dos valores das Dívidas da conta) - (Soma dos valores dos Pagamentos)
     * O valor do pagamento é automaticamente herdado da dívida associada
     */
    public BigDecimal getSaldo() {
        BigDecimal totalDividas = dividas.stream()
                .map(d -> BigDecimal.valueOf(d.getValor() != null ? d.getValor() : 0.0))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPagamentos = pagamentos.stream()
                .map(p -> BigDecimal.valueOf(p.getValor() != null ? p.getValor() : 0.0))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalDividas.subtract(totalPagamentos);
    }
}
