package tech.devleo.projeto_orlando.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tech.devleo.projeto_orlando.domain.Conta;
import tech.devleo.projeto_orlando.domain.Contrato;
import tech.devleo.projeto_orlando.domain.Devedor;
import tech.devleo.projeto_orlando.domain.Empresa;
import tech.devleo.projeto_orlando.dto.DevedorRequest;
import tech.devleo.projeto_orlando.repository.ContratoRepository;
import tech.devleo.projeto_orlando.repository.DevedorRepository;

@ExtendWith(MockitoExtension.class)
class DevedorServiceTest {

    @Mock
    private DevedorRepository devedorRepository;

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private EmpresaService empresaService;

    @InjectMocks
    private DevedorService devedorService;

    private Empresa empresa;
    private Devedor devedor;
    private Conta conta;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(UUID.randomUUID());
        empresa.setName("Empresa Teste");

        devedor = new Devedor();
        devedor.setId(UUID.randomUUID());
        devedor.setName("Devedor Teste");
        devedor.setCpf("12345678900");
        devedor.setEmail("devedor@teste.com");
        devedor.setEmpresa(empresa);

        conta = new Conta();
        conta.setId(UUID.randomUUID());
        conta.setDevedor(devedor);
        devedor.setConta(conta);
    }

    @Test
    void testDelete_DevedorComContratos_DeveDeletarEmCascade() {
        // Arrange
        Contrato contrato = new Contrato();
        contrato.setId(UUID.randomUUID());
        contrato.setDevedor(devedor);
        devedor.getContratos().add(contrato);

        when(devedorRepository.findById(devedor.getId())).thenReturn(java.util.Optional.of(devedor));
        when(empresaService.getEmpresaByCurrentUser()).thenReturn(empresa);
        doNothing().when(devedorRepository).deleteById(devedor.getId());

        // Act
        devedorService.delete(devedor.getId().toString());

        // Assert
        verify(devedorRepository).deleteById(devedor.getId());
        // Com cascade configurado, os contratos serão deletados automaticamente
    }

    @Test
    void testDelete_DevedorSemContratos_DeveDeletarNormalmente() {
        // Arrange
        when(devedorRepository.findById(devedor.getId())).thenReturn(java.util.Optional.of(devedor));
        when(empresaService.getEmpresaByCurrentUser()).thenReturn(empresa);
        doNothing().when(devedorRepository).deleteById(devedor.getId());

        // Act
        devedorService.delete(devedor.getId().toString());

        // Assert
        verify(devedorRepository).deleteById(devedor.getId());
    }

    @Test
    void testCreate_DevedorNovo_ContaDeveTerSaldoZero() {
        // Arrange
        DevedorRequest request = new DevedorRequest(
            "Novo Devedor",
            "98765432100",
            "novo@teste.com"
        );

        when(empresaService.getEmpresaByCurrentUser()).thenReturn(empresa);
        when(devedorRepository.save(any(Devedor.class))).thenAnswer(invocation -> {
            Devedor d = invocation.getArgument(0);
            d.setId(UUID.randomUUID());
            Conta c = new Conta();
            c.setId(UUID.randomUUID());
            c.setDevedor(d);
            d.setConta(c);
            return d;
        });

        // Act
        var response = devedorService.create(request);

        // Assert
        assertNotNull(response);
        assertEquals(BigDecimal.ZERO, response.contaSaldo());
        verify(devedorRepository).save(argThat(d -> 
            d.getConta() != null &&
            d.getConta().getSaldo().equals(BigDecimal.ZERO)
        ));
    }
}

