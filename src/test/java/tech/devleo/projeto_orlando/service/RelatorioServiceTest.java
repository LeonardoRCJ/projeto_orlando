package tech.devleo.projeto_orlando.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tech.devleo.projeto_orlando.domain.Empresa;
import tech.devleo.projeto_orlando.dto.AuditoriaResponse;
import tech.devleo.projeto_orlando.repository.ContaRepository;
import tech.devleo.projeto_orlando.repository.DividaRepository;
import tech.devleo.projeto_orlando.repository.PagamentoRepository;
import tech.devleo.projeto_orlando.repository.RelatorioRepository;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock
    private RelatorioRepository relatorioRepository;

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private DividaRepository dividaRepository;

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private EmpresaService empresaService;

    @InjectMocks
    private RelatorioService relatorioService;

    private Empresa empresa;

    @BeforeEach
    void setUp() {
        empresa = new Empresa();
        empresa.setId(UUID.randomUUID());
        empresa.setName("Empresa Teste");
    }

    @Test
    void testGerarAuditoria_ComDadosNoPeriodo_DeveRetornarValoresCorretos() {
        // Arrange
        LocalDate inicio = LocalDate.of(2024, 1, 1);
        LocalDate fim = LocalDate.of(2024, 12, 31);

        ZonedDateTime inicioZoned = inicio.atStartOfDay(ZoneId.of("America/Sao_Paulo"));
        ZonedDateTime fimZoned = fim.atTime(java.time.LocalTime.MAX).atZone(ZoneId.of("America/Sao_Paulo"));

        when(empresaService.getEmpresaByCurrentUser()).thenReturn(empresa);
        when(dividaRepository.sumValorByPeriodo(empresa, inicioZoned, fimZoned)).thenReturn(5000.0);
        when(pagamentoRepository.countByPeriodo(empresa, inicioZoned, fimZoned)).thenReturn(10L);

        // Act
        AuditoriaResponse response = relatorioService.gerarAuditoria(inicio, fim);

        // Assert
        assertNotNull(response);
        assertEquals(5000.0, response.valorTotalDividas());
        assertEquals(10L, response.totalPagamentos());
        assertEquals(inicio.toString(), response.periodoInicio());
        assertEquals(fim.toString(), response.periodoFim());
    }

    @Test
    void testGerarAuditoria_SemDadosNoPeriodo_DeveRetornarZeros() {
        // Arrange
        LocalDate inicio = LocalDate.of(2024, 1, 1);
        LocalDate fim = LocalDate.of(2024, 12, 31);

        ZonedDateTime inicioZoned = inicio.atStartOfDay(ZoneId.of("America/Sao_Paulo"));
        ZonedDateTime fimZoned = fim.atTime(java.time.LocalTime.MAX).atZone(ZoneId.of("America/Sao_Paulo"));

        when(empresaService.getEmpresaByCurrentUser()).thenReturn(empresa);
        when(dividaRepository.sumValorByPeriodo(empresa, inicioZoned, fimZoned)).thenReturn(null);
        when(pagamentoRepository.countByPeriodo(empresa, inicioZoned, fimZoned)).thenReturn(null);

        // Act
        AuditoriaResponse response = relatorioService.gerarAuditoria(inicio, fim);

        // Assert
        assertNotNull(response);
        assertEquals(0.0, response.valorTotalDividas());
        assertEquals(0L, response.totalPagamentos());
    }
}

