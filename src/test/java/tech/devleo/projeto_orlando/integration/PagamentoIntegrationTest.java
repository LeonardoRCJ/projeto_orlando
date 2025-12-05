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
import tech.devleo.projeto_orlando.domain.Pk.PagamentoId;
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
        
        // Setup da conta
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
        assertEquals(divida.getId(), response.dividaId());
        
        // Verificar no banco usando a chave composta
        PagamentoId id = new PagamentoId(response.dividaId(), response.contaId());
        Pagamento pagamento = pagamentoRepository.findById(id).orElseThrow();
        
        assertNotNull(pagamento.getDivida());
        assertEquals(divida.getValor(), pagamento.getValor());
    }

    @Test
    void testFindAll_DeveRetornarPagamentosDaEmpresa() {
        PagamentoRequest request = new PagamentoRequest(MetodoPagamento.PIX, divida.getId());
        pagamentoService.create(request);
        
        java.util.List<PagamentoResponse> pagamentos = pagamentoService.findAll();
        
        assertEquals(1, pagamentos.size());
        assertEquals(MetodoPagamento.PIX, pagamentos.get(0).metodo());
    }

    @Test
    void testFindById_PagamentoExistente_DeveRetornarPagamento() {
        PagamentoRequest request = new PagamentoRequest(MetodoPagamento.CREDITO, divida.getId());
        PagamentoResponse created = pagamentoService.create(request);
        
        // Busca usando os dois IDs
        PagamentoResponse response = pagamentoService.findById(created.dividaId(), created.contaId());
        
        assertNotNull(response);
        assertEquals(created.dividaId(), response.dividaId());
        assertEquals(created.contaId(), response.contaId());
        assertEquals(MetodoPagamento.CREDITO, response.metodo());
    }

    @Test
    void testUpdate_PagamentoExistente_DeveAtualizarMetodo() {
        // Criar pagamento inicial como PIX
        PagamentoRequest createRequest = new PagamentoRequest(MetodoPagamento.PIX, divida.getId());
        PagamentoResponse created = pagamentoService.create(createRequest);
        
        // Tentar atualizar para BOLETO (mantendo a mesma dívida, pois o ID é imutável)
        PagamentoRequest updateRequest = new PagamentoRequest(MetodoPagamento.BOLETO, divida.getId());
        
        PagamentoResponse response = pagamentoService.update(created.dividaId(), created.contaId(), updateRequest);
        
        assertEquals(MetodoPagamento.BOLETO, response.metodo());
        
        PagamentoId id = new PagamentoId(created.dividaId(), created.contaId());
        Pagamento pagamento = pagamentoRepository.findById(id).orElseThrow();
        assertEquals(MetodoPagamento.BOLETO, pagamento.getMetodo());
    }

    @Test
    void testDelete_PagamentoExistente_DeveDeletar() {
        PagamentoRequest request = new PagamentoRequest(MetodoPagamento.PIX, divida.getId());
        PagamentoResponse created = pagamentoService.create(request);
        
        // Deletar passando os dois IDs
        pagamentoService.delete(created.dividaId(), created.contaId());
        
        PagamentoId id = new PagamentoId(created.dividaId(), created.contaId());
        assertFalse(pagamentoRepository.existsById(id));
    }
}