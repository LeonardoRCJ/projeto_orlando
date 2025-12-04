package tech.devleo.projeto_orlando.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tech.devleo.projeto_orlando.domain.Conta;
import tech.devleo.projeto_orlando.domain.Devedor;
import tech.devleo.projeto_orlando.domain.Divida;
import tech.devleo.projeto_orlando.domain.Empresa;
import tech.devleo.projeto_orlando.domain.MetodoPagamento;
import tech.devleo.projeto_orlando.domain.Pagamento;
import tech.devleo.projeto_orlando.dto.PagamentoRequest;
import tech.devleo.projeto_orlando.repository.DividaRepository;
import tech.devleo.projeto_orlando.repository.PagamentoRepository;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private DividaRepository dividaRepository;

    @Mock
    private EmpresaService empresaService;

    @InjectMocks
    private PagamentoService pagamentoService;

    private Empresa empresa;
    private Devedor devedor;
    private Conta conta;
    private Divida divida;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(UUID.randomUUID());
        empresa.setName("Empresa Teste");

        devedor = new Devedor();
        devedor.setId(UUID.randomUUID());
        devedor.setName("Devedor Teste");
        devedor.setEmpresa(empresa);

        conta = new Conta();
        conta.setId(UUID.randomUUID());
        conta.setDevedor(devedor);

        divida = new Divida();
        divida.setId(1);
        divida.setValor(100.0);
        divida.setConta(conta);
        divida.setFiadora(empresa);
    }


    @Test
    void testCreate_SemValorInformado_DeveUsarValorDaDivida() {
        // Arrange
        PagamentoRequest request = new PagamentoRequest(
            MetodoPagamento.PIX,
            1
        );

        when(dividaRepository.findById(1)).thenReturn(java.util.Optional.of(divida));
        when(empresaService.getEmpresaByCurrentUser()).thenReturn(empresa);
        when(pagamentoRepository.save(any())).thenAnswer(invocation -> {
            Pagamento p = invocation.getArgument(0);
            p.setId(1);
            return p;
        });

        // Act
        var response = pagamentoService.create(request);

        // Assert
        assertNotNull(response);
        verify(pagamentoRepository).save(argThat(p -> 
            p.getValor().equals(100.0) && // Valor da dívida
            p.getDivida().getId().equals(1) &&
            p.getConta().getId().equals(conta.getId())
        ));
    }

    @Test
    void testCreate_DividaNaoPertenceEmpresa_DeveLancarExcecao() {
        // Arrange
        Empresa outraEmpresa = new Empresa();
        outraEmpresa.setId(UUID.randomUUID());
        divida.setFiadora(outraEmpresa);

        PagamentoRequest request = new PagamentoRequest(
            MetodoPagamento.PIX,
            1
        );

        when(dividaRepository.findById(1)).thenReturn(java.util.Optional.of(divida));
        when(empresaService.getEmpresaByCurrentUser()).thenReturn(empresa);

        // Act & Assert
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> {
            pagamentoService.create(request);
        });
    }
}

