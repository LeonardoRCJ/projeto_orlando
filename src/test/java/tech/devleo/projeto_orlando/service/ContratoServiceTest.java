package tech.devleo.projeto_orlando.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tech.devleo.projeto_orlando.domain.Contrato;
import tech.devleo.projeto_orlando.domain.Devedor;
import tech.devleo.projeto_orlando.domain.Empresa;
import tech.devleo.projeto_orlando.domain.StatusContrato;
import tech.devleo.projeto_orlando.dto.ContratoRequest;
import tech.devleo.projeto_orlando.repository.ContratoRepository;
import tech.devleo.projeto_orlando.repository.DevedorRepository;

@ExtendWith(MockitoExtension.class)
class ContratoServiceTest {

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private DevedorRepository devedorRepository;

    @Mock
    private EmpresaService empresaService;

    @InjectMocks
    private ContratoService contratoService;

    private Empresa empresa;
    private Devedor devedor;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(UUID.randomUUID());
        empresa.setName("Empresa Teste");

        devedor = new Devedor();
        devedor.setId(UUID.randomUUID());
        devedor.setName("Devedor Teste");
        devedor.setEmpresa(empresa);
    }

    @Test
    void testCreate_ComDataVencimentoInformada_DeveUsarDataInformada() {
        // Arrange
        LocalDate dataVencimento = LocalDate.now().plusMonths(6);
        ContratoRequest request = new ContratoRequest(
            "Contrato de teste",
            devedor.getId().toString(),
            dataVencimento
        );

        when(devedorRepository.findById(devedor.getId())).thenReturn(java.util.Optional.of(devedor));
        when(empresaService.getEmpresaByCurrentUser()).thenReturn(empresa);
        when(contratoRepository.save(any(Contrato.class))).thenAnswer(invocation -> {
            Contrato c = invocation.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        // Act
        var response = contratoService.create(request);

        // Assert
        assertNotNull(response);
        verify(contratoRepository).save(argThat(c -> 
            c.getStatus() == StatusContrato.ATIVO &&
            c.getVencimentoContrato().toLocalDate().equals(dataVencimento)
        ));
    }

    @Test
    void testCreate_SemDataVencimento_DeveUsarDataPadraoMaisUmAno() {
        // Arrange
        ContratoRequest request = new ContratoRequest(
            "Contrato de teste",
            devedor.getId().toString(),
            null
        );

        ZonedDateTime agora = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"));
        ZonedDateTime esperado = agora.plusYears(1);

        when(devedorRepository.findById(devedor.getId())).thenReturn(java.util.Optional.of(devedor));
        when(empresaService.getEmpresaByCurrentUser()).thenReturn(empresa);
        when(contratoRepository.save(any(Contrato.class))).thenAnswer(invocation -> {
            Contrato c = invocation.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        // Act
        var response = contratoService.create(request);

        // Assert
        assertNotNull(response);
        verify(contratoRepository).save(argThat(c -> 
            c.getStatus() == StatusContrato.ATIVO &&
            c.getVencimentoContrato() != null &&
            Math.abs(c.getVencimentoContrato().toEpochSecond() - esperado.toEpochSecond()) < 60 // Tolerância de 1 minuto
        ));
    }

    @Test
    void testCreate_StatusInicial_DeveSerATIVO() {
        // Arrange
        ContratoRequest request = new ContratoRequest(
            "Contrato de teste",
            devedor.getId().toString(),
            null
        );

        when(devedorRepository.findById(devedor.getId())).thenReturn(java.util.Optional.of(devedor));
        when(empresaService.getEmpresaByCurrentUser()).thenReturn(empresa);
        when(contratoRepository.save(any(Contrato.class))).thenAnswer(invocation -> {
            Contrato c = invocation.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        // Act
        var response = contratoService.create(request);

        // Assert
        assertNotNull(response);
        assertEquals(StatusContrato.ATIVO, response.status());
    }
}

