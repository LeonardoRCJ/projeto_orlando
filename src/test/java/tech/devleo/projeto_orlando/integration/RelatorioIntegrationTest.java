package tech.devleo.projeto_orlando.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import tech.devleo.projeto_orlando.domain.Conta;
import tech.devleo.projeto_orlando.domain.Devedor;
import tech.devleo.projeto_orlando.domain.Divida;
import tech.devleo.projeto_orlando.domain.Pagamento;
import tech.devleo.projeto_orlando.domain.TipoRelatorio;
import tech.devleo.projeto_orlando.domain.Pk.PagamentoId;
import tech.devleo.projeto_orlando.dto.AuditoriaResponse;
import tech.devleo.projeto_orlando.dto.RelatorioRequest;
import tech.devleo.projeto_orlando.dto.RelatorioResponse;
import tech.devleo.projeto_orlando.repository.ContaRepository;
import tech.devleo.projeto_orlando.repository.DevedorRepository;
import tech.devleo.projeto_orlando.repository.DividaRepository;
import tech.devleo.projeto_orlando.repository.PagamentoRepository;
import tech.devleo.projeto_orlando.repository.RelatorioRepository;
import tech.devleo.projeto_orlando.service.RelatorioService;

@Transactional
class RelatorioIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RelatorioService relatorioService;

    @Autowired
    private RelatorioRepository relatorioRepository;

    @Autowired
    private DividaRepository dividaRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private DevedorRepository devedorRepository;

    @Autowired
    private ContaRepository contaRepository;

    private Devedor devedor;
    private Conta conta;

    @BeforeEach
    @Override
    void setUp() {
        super.setUp();
        
        devedor = new Devedor();
        devedor.setName("Devedor Teste");
        devedor.setCpf("12345678900");
        devedor.setEmail("devedor@test.com");
        devedor.setEmpresa(testEmpresa);
        
        conta = new Conta();
        conta.setDevedor(devedor);
        devedor.setConta(conta);
        devedor = devedorRepository.save(devedor);
        conta = devedor.getConta();
    }

    @Test
    void testCreate_RelatorioManual_DeveCriarRelatorio() {
        RelatorioRequest request = new RelatorioRequest(
            TipoRelatorio.MANUAL,
            1000.0,
            conta.getId().toString(),
            null,
            null,
            null,
            "Relatório manual de teste"
        );
        
        RelatorioResponse response = relatorioService.create(request);
        
        assertNotNull(response);
        assertEquals(TipoRelatorio.MANUAL, response.tipo());
        assertEquals(1000.0, response.valorMovimentado());
    }

    @Test
    void testCreate_RelatorioContaEspecifica_DeveCalcularAutomaticamente() {
        // Criar dívidas
        Divida divida1 = new Divida();
        divida1.setValor(200.0);
        divida1.setConta(conta);
        divida1.setFiadora(testEmpresa);
        divida1 = dividaRepository.save(divida1);
        conta.getDividas().add(divida1);

        Divida divida2 = new Divida();
        divida2.setValor(100.0);
        divida2.setConta(conta);
        divida2.setFiadora(testEmpresa);
        divida2 = dividaRepository.save(divida2);
        conta.getDividas().add(divida2);
        
        contaRepository.save(conta);

        // Criar pagamento com ID explícito
        Pagamento pagamento = new Pagamento();
        pagamento.setId(new PagamentoId(divida1.getId(), conta.getId())); // Fix: Set ID explicitly
        pagamento.setConta(conta);
        pagamento.setDivida(divida1);
        pagamento.setValor(divida1.getValor());
        
        pagamentoRepository.save(pagamento);
        conta.getPagamentos().add(pagamento);
        
        contaRepository.save(conta);
        
        // Flush e recarregar
        dividaRepository.flush();
        pagamentoRepository.flush();
        contaRepository.flush();

        conta = contaRepository.findById(conta.getId()).orElseThrow();
        Hibernate.initialize(conta.getDividas());
        Hibernate.initialize(conta.getPagamentos());

        RelatorioRequest request = new RelatorioRequest(
            TipoRelatorio.CONTA_ESPECIFICA,
            null,
            conta.getId().toString(),
            null,
            null,
            null,
            null
        );
        
        RelatorioResponse response = relatorioService.create(request);
        
        assertNotNull(response);
        assertEquals(300.0, response.totalDividas());
        assertEquals(200.0, response.totalPagamentos());
        assertEquals(2, response.quantidadeDividas());
        assertEquals(1, response.quantidadePagamentos());
    }

    @Test
    void testCreate_RelatorioPeriodo_DeveCalcularMovimentacoes() {
        ZonedDateTime agora = ZonedDateTime.now();
        
        Divida divida = new Divida();
        divida.setValor(500.0);
        divida.setConta(conta);
        divida.setFiadora(testEmpresa);
        divida.setDataCriacao(agora);
        divida = dividaRepository.save(divida);

        Pagamento pagamento = new Pagamento();
        pagamento.setId(new PagamentoId(divida.getId(), conta.getId())); // Fix: Set ID explicitly
        pagamento.setConta(conta);
        pagamento.setDivida(divida);
        pagamento.setValor(divida.getValor());
        pagamento.setDataPagamento(agora);
        pagamentoRepository.save(pagamento);

        LocalDate inicio = LocalDate.now().minusDays(1);
        LocalDate fim = LocalDate.now().plusDays(1);

        RelatorioRequest request = new RelatorioRequest(
            TipoRelatorio.PERIODO,
            null,
            null,
            inicio,
            fim,
            null,
            null
        );
        
        RelatorioResponse response = relatorioService.create(request);
        
        assertNotNull(response);
        assertEquals(500.0, response.totalDividas());
        assertEquals(500.0, response.totalPagamentos());
    }

    @Test
    void testCreate_RelatorioRecebimentos_DeveCalcularRecebimentos() {
        ZonedDateTime agora = ZonedDateTime.now();
        
        // Criar dívidas
        Divida divida1 = new Divida();
        divida1.setValor(200.0);
        divida1.setConta(conta);
        divida1.setFiadora(testEmpresa);
        divida1 = dividaRepository.save(divida1);

        Divida divida2 = new Divida();
        divida2.setValor(100.0);
        divida2.setConta(conta);
        divida2.setFiadora(testEmpresa);
        divida2 = dividaRepository.save(divida2);

        // Pagamento 1
        Pagamento pagamento1 = new Pagamento();
        pagamento1.setId(new PagamentoId(divida1.getId(), conta.getId())); // Fix: Set ID explicitly
        pagamento1.setConta(conta);
        pagamento1.setDivida(divida1);
        pagamento1.setValor(divida1.getValor());
        pagamento1.setDataPagamento(agora);
        pagamentoRepository.save(pagamento1);

        // Pagamento 2
        Pagamento pagamento2 = new Pagamento();
        pagamento2.setId(new PagamentoId(divida2.getId(), conta.getId())); // Fix: Set ID explicitly
        pagamento2.setConta(conta);
        pagamento2.setDivida(divida2);
        pagamento2.setValor(divida2.getValor());
        pagamento2.setDataPagamento(agora);
        pagamentoRepository.save(pagamento2);

        LocalDate inicio = LocalDate.now().minusDays(1);
        LocalDate fim = LocalDate.now().plusDays(1);

        RelatorioRequest request = new RelatorioRequest(
            TipoRelatorio.RECEBIMENTOS,
            null,
            null,
            inicio,
            fim,
            null,
            null
        );
        
        RelatorioResponse response = relatorioService.create(request);
        
        assertNotNull(response);
        assertEquals(300.0, response.totalPagamentos());
        assertEquals(2, response.quantidadePagamentos());
    }

    @Test
    void testGerarAuditoria_ComDividasEPagamentos_DeveCalcularCorretamente() {
        ZonedDateTime agora = ZonedDateTime.now();
        
        // Dívidas
        Divida divida1 = new Divida();
        divida1.setValor(100.0);
        divida1.setConta(conta);
        divida1.setFiadora(testEmpresa);
        divida1.setDataCriacao(agora);
        divida1 = dividaRepository.save(divida1);

        // Pagamento
        Pagamento pagamento1 = new Pagamento();
        pagamento1.setId(new PagamentoId(divida1.getId(), conta.getId())); // Fix: Set ID explicitly
        pagamento1.setConta(conta);
        pagamento1.setDivida(divida1);
        pagamento1.setValor(divida1.getValor());
        pagamento1.setDataPagamento(agora);
        pagamentoRepository.save(pagamento1);

        LocalDate inicio = LocalDate.now().minusDays(1);
        LocalDate fim = LocalDate.now().plusDays(1);
        
        AuditoriaResponse auditoria = relatorioService.gerarAuditoria(inicio, fim);
        
        assertNotNull(auditoria);
        assertEquals(100.0, auditoria.valorTotalDividas());
        assertEquals(1L, auditoria.totalPagamentos());
    }
}