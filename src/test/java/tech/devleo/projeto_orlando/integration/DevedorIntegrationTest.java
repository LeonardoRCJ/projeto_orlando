package tech.devleo.projeto_orlando.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import tech.devleo.projeto_orlando.domain.Conta;
import tech.devleo.projeto_orlando.domain.Contrato;
import tech.devleo.projeto_orlando.domain.Devedor;
import tech.devleo.projeto_orlando.domain.Divida;
import tech.devleo.projeto_orlando.domain.User;
import tech.devleo.projeto_orlando.dto.DevedorRequest;
import tech.devleo.projeto_orlando.dto.DevedorResponse;
import tech.devleo.projeto_orlando.repository.ContaRepository;
import tech.devleo.projeto_orlando.repository.ContratoRepository;
import tech.devleo.projeto_orlando.repository.DevedorRepository;
import tech.devleo.projeto_orlando.repository.DividaRepository;
import tech.devleo.projeto_orlando.service.DevedorService;

@Transactional
class DevedorIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private DevedorService devedorService;

    @Autowired
    private DevedorRepository devedorRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private DividaRepository dividaRepository;

    private Devedor devedor;

    @BeforeEach
    @Override
    void setUp() {
        super.setUp();
        
        devedor = new Devedor();
        devedor.setName("Devedor Teste");
        devedor.setCpf("12345678900");
        devedor.setEmail("devedor@test.com");
        devedor.setEmpresa(testEmpresa);
        
        // Criar conta associada
        Conta conta = new Conta();
        conta.setDevedor(devedor);
        devedor.setConta(conta);
        
        devedor = devedorRepository.save(devedor);
    }

    @Test
    void testCreate_DevedorNovo_DeveCriarComConta() {
        DevedorRequest request = new DevedorRequest("Novo Devedor", "11122233344", "novo@test.com");
        
        DevedorResponse response = devedorService.create(request);
        
        assertNotNull(response);
        assertEquals("Novo Devedor", response.name());
        assertEquals("11122233344", response.cpf());
        
        // Verificar se a conta foi criada
        Devedor saved = devedorRepository.findById(response.id()).orElseThrow();
        assertNotNull(saved.getConta());
    }

    @Test
    void testFindAll_DeveRetornarDevedoresDaEmpresa() {
        // Criar outro devedor de outra empresa
        User outroUser = new User();
        outroUser.setUsername("outrouser");
        outroUser.setEmail("outro@example.com");
        outroUser.setPassword("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy");
        outroUser.setEnabled(true);
        outroUser = userRepository.save(outroUser);

        tech.devleo.projeto_orlando.domain.Empresa outraEmpresa = new tech.devleo.projeto_orlando.domain.Empresa();
        outraEmpresa.setName("Outra Empresa");
        outraEmpresa.setDono(outroUser);
        outraEmpresa = empresaRepository.save(outraEmpresa);

        Devedor outroDevedor = new Devedor();
        outroDevedor.setName("Outro Devedor");
        outroDevedor.setCpf("98765432100");
        outroDevedor.setEmail("outro@test.com");
        outroDevedor.setEmpresa(outraEmpresa);
        
        Conta outraConta = new Conta();
        outraConta.setDevedor(outroDevedor);
        outroDevedor.setConta(outraConta);
        devedorRepository.save(outroDevedor);

        // Buscar devedores
        java.util.List<DevedorResponse> devedores = devedorService.findAll();
        
        // Deve retornar apenas o devedor da empresa do usuário autenticado
        assertEquals(1, devedores.size());
        assertEquals(devedor.getId(), devedores.get(0).id());
    }

    @Test
    void testFindById_DevedorExistente_DeveRetornarDevedor() {
        DevedorResponse response = devedorService.findById(devedor.getId().toString());
        
        assertNotNull(response);
        assertEquals(devedor.getId(), response.id());
        assertEquals(devedor.getName(), response.name());
    }

    @Test
    void testUpdate_DevedorExistente_DeveAtualizar() {
        DevedorRequest request = new DevedorRequest("Devedor Atualizado", "12345678900", "atualizado@test.com");
        
        DevedorResponse response = devedorService.update(devedor.getId().toString(), request);
        
        assertEquals("Devedor Atualizado", response.name());
        assertEquals("atualizado@test.com", response.email());
    }

    @Test
    void testDelete_DevedorSemContratos_DeveDeletar() {
        UUID devedorId = devedor.getId();
        
        devedorService.delete(devedorId.toString());
        
        assertFalse(devedorRepository.existsById(devedorId));
        assertFalse(contaRepository.existsById(devedorId));
    }

    @Test
    void testDelete_DevedorComContratos_DeveDeletarEmCascade() {
        // Criar contrato
        Contrato contrato = new Contrato();
        contrato.setTexto_contrato("Contrato Teste");
        contrato.setEmpresa(testEmpresa);
        contrato.setDevedor(devedor);
        contrato = contratoRepository.save(contrato);
        
        // Adicionar contrato à lista do devedor para garantir o cascade
        devedor.getContratos().add(contrato);
        devedor = devedorRepository.save(devedor);
        
        UUID devedorId = devedor.getId();
        UUID contratoId = contrato.getId();
        
        // Deletar devedor
        devedorService.delete(devedorId.toString());
        
        // Verificar que devedor, conta e contrato foram deletados
        assertFalse(devedorRepository.existsById(devedorId));
        assertFalse(contaRepository.existsById(devedorId));
        assertFalse(contratoRepository.existsById(contratoId));
    }
}

