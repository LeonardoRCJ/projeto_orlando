package tech.devleo.projeto_orlando.integration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import tech.devleo.projeto_orlando.domain.Conta;
import tech.devleo.projeto_orlando.domain.Devedor;
import tech.devleo.projeto_orlando.domain.Divida;
import tech.devleo.projeto_orlando.domain.MetodoPagamento;
import tech.devleo.projeto_orlando.domain.Pagamento;
import tech.devleo.projeto_orlando.dto.PagamentoRequest;
import tech.devleo.projeto_orlando.dto.PagamentoResponse;
import tech.devleo.projeto_orlando.repository.DividaRepository;
import tech.devleo.projeto_orlando.repository.PagamentoRepository;
import tech.devleo.projeto_orlando.service.PagamentoService;

@Transactional
class PagamentoIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PagamentoService pagamentoService;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private DividaRepository dividaRepository;

    @Autowired
    private tech.devleo.projeto_orlando.repository.DevedorRepository devedorRepository;

    private Devedor devedor;
    private Conta conta;
    private Divida divida;

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

        divida = new Divida();
        divida.setValor(100.0);
        divida.setConta(conta);
        divida.setFiadora(testEmpresa);
        divida = dividaRepository.save(divida);
    }

    @Test
    void testCreate_Pagamento_DeveCriarComValorDaDivida() {
        PagamentoRequest request = new PagamentoRequest(MetodoPagamento.PIX, divida.getId());
        
        PagamentoResponse response = pagamentoService.create(request);
        
        assertNotNull(response);
        assertEquals(MetodoPagamento.PIX, response.metodo());
        assertEquals(conta.getId(), response.contaId());
        
        Pagamento pagamento = pagamentoRepository.findById(response.id()).orElseThrow();
        assertNotNull(pagamento.getDivida());
        assertEquals(divida.getValor(), pagamento.getDivida().getValor());
    }

    @Test
    void testFindAll_DeveRetornarPagamentosDaEmpresa() {
        // Criar pagamento
        PagamentoRequest request = new PagamentoRequest(MetodoPagamento.PIX, divida.getId());
        pagamentoService.create(request);
        
        // Buscar pagamentos
        java.util.List<PagamentoResponse> pagamentos = pagamentoService.findAll();
        
        assertEquals(1, pagamentos.size());
        assertEquals(MetodoPagamento.PIX, pagamentos.get(0).metodo());
    }

    @Test
    void testFindById_PagamentoExistente_DeveRetornarPagamento() {
        PagamentoRequest request = new PagamentoRequest(MetodoPagamento.CREDITO, divida.getId());
        PagamentoResponse created = pagamentoService.create(request);
        
        PagamentoResponse response = pagamentoService.findById(created.id());
        
        assertNotNull(response);
        assertEquals(created.id(), response.id());
        assertEquals(MetodoPagamento.CREDITO, response.metodo());
    }
    @Test
    void testUpdate_PagamentoExistente_DeveAtualizar() {
        PagamentoRequest createRequest = new PagamentoRequest(MetodoPagamento.PIX, divida.getId());
        PagamentoResponse created = pagamentoService.create(createRequest);
        
        Divida novaDivida = new Divida();
        novaDivida.setValor(150.0);
        novaDivida.setConta(conta);
        novaDivida.setFiadora(testEmpresa);
        novaDivida = dividaRepository.save(novaDivida);
        
        PagamentoRequest updateRequest = new PagamentoRequest(MetodoPagamento.BOLETO, novaDivida.getId());
        PagamentoResponse response = pagamentoService.update(created.id(), updateRequest);
        
        assertEquals(MetodoPagamento.BOLETO, response.metodo());
        
        Pagamento pagamento = pagamentoRepository.findById(response.id()).orElseThrow();
        assertEquals(novaDivida.getId(), pagamento.getDivida().getId());
        assertEquals(novaDivida.getValor(), pagamento.getDivida().getValor());
    }

    @Test
    void testDelete_PagamentoExistente_DeveDeletar() {
        PagamentoRequest request = new PagamentoRequest(MetodoPagamento.PIX, divida.getId());
        PagamentoResponse created = pagamentoService.create(request);
        
        pagamentoService.delete(created.id());
        
        assertFalse(pagamentoRepository.existsById(created.id()));
    }
}

