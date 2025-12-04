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
        assertEquals(conta.getId(), response.contaId());
        assertNotNull(response.dataGeracao());
        assertEquals("Relatório manual de teste", response.descricao());
    }

    @Test
    void testCreate_RelatorioContaEspecifica_DeveCalcularAutomaticamente() {
        // Criar dívidas e pagamentos
        Divida divida1 = new Divida();
        divida1.setValor(200.0);
        divida1.setConta(conta);
        divida1.setFiadora(testEmpresa);
        
        // Adicionar dívida à lista da conta para garantir o relacionamento bidirecional
        conta.getDividas().add(divida1);
        divida1 = dividaRepository.save(divida1);

        Divida divida2 = new Divida();
        divida2.setValor(100.0);
        divida2.setConta(conta);
        divida2.setFiadora(testEmpresa);
        
        // Adicionar dívida à lista da conta para garantir o relacionamento bidirecional
        conta.getDividas().add(divida2);
        divida2 = dividaRepository.save(divida2);

        // Criar pagamento para divida1 (valor do pagamento = valor da dívida)
        Pagamento pagamento = new Pagamento();
        pagamento.setConta(conta);
        pagamento.setDivida(divida1);
        pagamento.setValor(divida1.getValor()); // Valor herdado da dívida
        
        // Adicionar pagamento à lista da conta para garantir o relacionamento bidirecional
        conta.getPagamentos().add(pagamento);
        pagamentoRepository.save(pagamento);
        
        // Salvar a conta para garantir que os relacionamentos estão sincronizados
        contaRepository.save(conta);
        
        // Fazer flush para garantir que tudo está persistido
        dividaRepository.flush();
        pagamentoRepository.flush();
        contaRepository.flush();

        // Recarregar conta e inicializar coleções
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
        assertEquals(TipoRelatorio.CONTA_ESPECIFICA, response.tipo());
        assertEquals(conta.getId(), response.contaId());
        assertEquals(300.0, response.totalDividas()); // 200 + 100
        assertEquals(200.0, response.totalPagamentos()); // Valor da divida1
        assertEquals(2, response.quantidadeDividas());
        assertEquals(1, response.quantidadePagamentos());
        assertEquals(100.0, response.valorMovimentado()); // Saldo: 300 - 200
        assertNotNull(response.descricao());
    }

    @Test
    void testCreate_RelatorioConsolidado_DeveCalcularTodasAsContas() {
        // Criar outra conta
        Devedor outroDevedor = new Devedor();
        outroDevedor.setName("Outro Devedor");
        outroDevedor.setCpf("98765432100");
        outroDevedor.setEmail("outro@test.com");
        outroDevedor.setEmpresa(testEmpresa);
        
        Conta outraConta = new Conta();
        outraConta.setDevedor(outroDevedor);
        outroDevedor.setConta(outraConta);
        outroDevedor = devedorRepository.save(outroDevedor);
        outraConta = outroDevedor.getConta();

        // Criar dívidas
        Divida divida1 = new Divida();
        divida1.setValor(100.0);
        divida1.setConta(conta);
        divida1.setFiadora(testEmpresa);
        
        // Adicionar dívida à lista da conta para garantir o relacionamento bidirecional
        conta.getDividas().add(divida1);
        divida1 = dividaRepository.save(divida1);
        contaRepository.save(conta);

        Divida divida2 = new Divida();
        divida2.setValor(200.0);
        divida2.setConta(outraConta);
        divida2.setFiadora(testEmpresa);
        
        // Adicionar dívida à lista da conta para garantir o relacionamento bidirecional
        outraConta.getDividas().add(divida2);
        divida2 = dividaRepository.save(divida2);
        contaRepository.save(outraConta);
        
        // Fazer flush para garantir que tudo está persistido
        dividaRepository.flush();
        contaRepository.flush();
        
        // Recarregar as contas do banco para garantir que as dívidas estão carregadas
        conta = contaRepository.findById(conta.getId()).orElseThrow();
        outraConta = contaRepository.findById(outraConta.getId()).orElseThrow();
        
        // Forçar inicialização das coleções lazy
        org.hibernate.Hibernate.initialize(conta.getDividas());
        org.hibernate.Hibernate.initialize(outraConta.getDividas());

        RelatorioRequest request = new RelatorioRequest(
            TipoRelatorio.CONSOLIDADO_EMPRESA,
            null,
            null,
            null,
            null,
            null,
            null
        );
        
        RelatorioResponse response = relatorioService.create(request);
        
        assertNotNull(response);
        assertEquals(TipoRelatorio.CONSOLIDADO_EMPRESA, response.tipo());
        assertNull(response.contaId());
        assertEquals(300.0, response.totalDividas());
        assertEquals(2, response.quantidadeContas());
        assertEquals(2, response.quantidadeDividas());
        assertEquals(300.0, response.valorMovimentado()); // Saldo total
        assertNotNull(response.descricao());
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
        pagamento.setConta(conta);
        pagamento.setDivida(divida);
        pagamento.setValor(divida.getValor()); // Valor herdado da dívida
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
        assertEquals(TipoRelatorio.PERIODO, response.tipo());
        assertNotNull(response.dataInicio());
        assertNotNull(response.dataFim());
        assertEquals(500.0, response.totalDividas());
        assertEquals(500.0, response.totalPagamentos()); // Valor herdado da dívida
        assertEquals(0.0, response.valorMovimentado()); // 500 - 500
    }

    @Test
    void testCreate_RelatorioRecebimentos_DeveCalcularRecebimentos() {
        ZonedDateTime agora = ZonedDateTime.now();
        
        // Criar duas dívidas diferentes (cada dívida só pode ter um pagamento devido ao @OneToOne)
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

        Pagamento pagamento1 = new Pagamento();
        pagamento1.setConta(conta);
        pagamento1.setDivida(divida1);
        pagamento1.setValor(divida1.getValor()); // Valor herdado da dívida
        pagamento1.setDataPagamento(agora);
        pagamentoRepository.save(pagamento1);

        Pagamento pagamento2 = new Pagamento();
        pagamento2.setConta(conta);
        pagamento2.setDivida(divida2);
        pagamento2.setValor(divida2.getValor()); // Valor herdado da dívida
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
        assertEquals(TipoRelatorio.RECEBIMENTOS, response.tipo());
        assertEquals(300.0, response.totalPagamentos()); // 200 (divida1) + 100 (divida2)
        assertEquals(300.0, response.valorMovimentado());
        assertEquals(2, response.quantidadePagamentos());
        assertNotNull(response.descricao());
    }

    @Test
    void testFindAll_DeveRetornarRelatoriosDaEmpresa() {
        RelatorioRequest request = new RelatorioRequest(
            TipoRelatorio.MANUAL,
            1000.0,
            conta.getId().toString(),
            null,
            null,
            null,
            null
        );
        relatorioService.create(request);
        
        java.util.List<RelatorioResponse> relatorios = relatorioService.findAll();
        
        assertEquals(1, relatorios.size());
        assertEquals(1000.0, relatorios.get(0).valorMovimentado());
    }

    @Test
    void testFindById_RelatorioExistente_DeveRetornarRelatorio() {
        RelatorioRequest request = new RelatorioRequest(
            TipoRelatorio.MANUAL,
            2000.0,
            conta.getId().toString(),
            null,
            null,
            null,
            null
        );
        RelatorioResponse created = relatorioService.create(request);
        
        RelatorioResponse response = relatorioService.findById(created.id());
        
        assertNotNull(response);
        assertEquals(created.id(), response.id());
        assertEquals(2000.0, response.valorMovimentado());
        assertEquals(TipoRelatorio.MANUAL, response.tipo());
    }

    @Test
    void testUpdate_RelatorioManual_DeveAtualizar() {
        RelatorioRequest createRequest = new RelatorioRequest(
            TipoRelatorio.MANUAL,
            1000.0,
            conta.getId().toString(),
            null,
            null,
            null,
            "Descrição original"
        );
        RelatorioResponse created = relatorioService.create(createRequest);
        
        RelatorioRequest updateRequest = new RelatorioRequest(
            TipoRelatorio.MANUAL,
            1500.0,
            conta.getId().toString(),
            null,
            null,
            null,
            "Descrição atualizada"
        );
        RelatorioResponse response = relatorioService.update(created.id(), updateRequest);
        
        assertEquals(1500.0, response.valorMovimentado());
        assertEquals("Descrição atualizada", response.descricao());
    }

    @Test
    void testDelete_RelatorioExistente_DeveDeletar() {
        RelatorioRequest request = new RelatorioRequest(
            TipoRelatorio.MANUAL,
            1000.0,
            conta.getId().toString(),
            null,
            null,
            null,
            null
        );
        RelatorioResponse created = relatorioService.create(request);
        
        relatorioService.delete(created.id());
        
        assertFalse(relatorioRepository.existsById(created.id()));
    }

    @Test
    void testGerarAuditoria_ComDividasEPagamentos_DeveCalcularCorretamente() {
        // Criar dívidas no período
        ZonedDateTime agora = ZonedDateTime.now();
        Divida divida1 = new Divida();
        divida1.setValor(100.0);
        divida1.setConta(conta);
        divida1.setFiadora(testEmpresa);
        divida1.setDataCriacao(agora);
        divida1 = dividaRepository.save(divida1);

        Divida divida2 = new Divida();
        divida2.setValor(200.0);
        divida2.setConta(conta);
        divida2.setFiadora(testEmpresa);
        divida2.setDataCriacao(agora);
        divida2 = dividaRepository.save(divida2);

        // Criar pagamentos no período
        Pagamento pagamento1 = new Pagamento();
        pagamento1.setConta(conta);
        pagamento1.setDivida(divida1);
        pagamento1.setValor(divida1.getValor()); // Valor herdado da dívida
        pagamento1.setDataPagamento(agora);
        pagamentoRepository.save(pagamento1);

        Pagamento pagamento2 = new Pagamento();
        pagamento2.setConta(conta);
        pagamento2.setDivida(divida2);
        pagamento2.setValor(divida2.getValor()); // Valor herdado da dívida
        pagamento2.setDataPagamento(agora);
        pagamentoRepository.save(pagamento2);

        // Gerar auditoria
        LocalDate inicio = LocalDate.now().minusDays(1);
        LocalDate fim = LocalDate.now().plusDays(1);
        
        AuditoriaResponse auditoria = relatorioService.gerarAuditoria(inicio, fim);
        
        assertNotNull(auditoria);
        assertEquals(300.0, auditoria.valorTotalDividas());
        assertEquals(2L, auditoria.totalPagamentos());
    }
}

