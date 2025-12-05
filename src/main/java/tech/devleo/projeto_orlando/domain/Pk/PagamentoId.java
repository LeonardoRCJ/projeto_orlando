package tech.devleo.projeto_orlando.domain.Pk;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoId implements Serializable {

    @Column(name = "divida_id", nullable = false)
    private Integer dividaId;
    
    @Column(name = "conta_id", nullable = false)
    private UUID contaId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PagamentoId that = (PagamentoId) o;
        return Objects.equals(dividaId, that.dividaId) && 
               Objects.equals(contaId, that.contaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dividaId, contaId);
    }
}