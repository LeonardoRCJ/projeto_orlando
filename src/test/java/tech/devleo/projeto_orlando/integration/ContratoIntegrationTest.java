package tech.devleo.projeto_orlando.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import tech.devleo.projeto_orlando.domain.Contrato;
import tech.devleo.projeto_orlando.domain.Devedor;
import tech.devleo.projeto_orlando.domain.StatusContrato;
import tech.devleo.projeto_orlando.domain.User;
import tech.devleo.projeto_orlando.dto.ContratoRequest;
import tech.devleo.projeto_orlando.dto.ContratoResponse;
import tech.devleo.projeto_orlando.repository.ContratoRepository;
import tech.devleo.projeto_orlando.repository.DevedorRepository;
import tech.devleo.projeto_orlando.service.ContratoService;

@Transactional
class ContratoIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ContratoService contratoService;

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private DevedorRepository devedorRepository;

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
        
        tech.devleo.projeto_orlando.domain.Conta conta = new tech.devleo.projeto_orlando.domain.Conta();
        conta.setDevedor(devedor);
        devedor.setConta(conta);
        
        devedor = devedorRepository.save(devedor);
    }

    @Test
    void testCreate_ContratoComDataVencimento_DeveUsarDataInformada() {
        LocalDate dataVencimento = LocalDate.now().plusMonths(6);
        ContratoRequest request = new ContratoRequest("Contrato Teste", devedor.getId().toString(), dataVencimento);
        
        ContratoResponse response = contratoService.create(request);
        
        assertNotNull(response);
        assertEquals("Contrato Teste", response.textoContrato());
        assertEquals(devedor.getId(), response.devedorId());
        assertEquals(testEmpresa.getId(), response.empresaId());
        assertEquals(StatusContrato.ATIVO, response.status());
        assertNotNull(response.vencimentoContrato());
    }

    @Test
    void testCreate_ContratoSemDataVencimento_DeveUsarDataPadrao() {
        ContratoRequest request = new ContratoRequest("Contrato Teste", devedor.getId().toString(), null);
        
        ContratoResponse response = contratoService.create(request);
        
        assertNotNull(response);
        assertNotNull(response.vencimentoContrato());
        // A data padrão deve ser aproximadamente 1 ano a partir de agora
        assertTrue(response.vencimentoContrato().isAfter(java.time.ZonedDateTime.now()));
    }

    @Test
    void testFindAll_DeveRetornarContratosDaEmpresa() {
        // Criar contrato
        ContratoRequest request = new ContratoRequest("Contrato 1", devedor.getId().toString(), null);
        contratoService.create(request);
        
        // Criar outro contrato de outra empresa
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
        
        tech.devleo.projeto_orlando.domain.Conta outraConta = new tech.devleo.projeto_orlando.domain.Conta();
        outraConta.setDevedor(outroDevedor);
        outroDevedor.setConta(outraConta);
        outroDevedor = devedorRepository.save(outroDevedor);

        Contrato outroContrato = new Contrato();
        outroContrato.setTexto_contrato("Contrato Outra Empresa");
        outroContrato.setEmpresa(outraEmpresa);
        outroContrato.setDevedor(outroDevedor);
        contratoRepository.save(outroContrato);

        // Buscar contratos
        java.util.List<ContratoResponse> contratos = contratoService.findAll();
        
        // Deve retornar apenas o contrato da empresa do usuário autenticado
        assertEquals(1, contratos.size());
        assertEquals("Contrato 1", contratos.get(0).textoContrato());
    }

    @Test
    void testFindById_ContratoExistente_DeveRetornarContrato() {
        ContratoRequest request = new ContratoRequest("Contrato Teste", devedor.getId().toString(), null);
        ContratoResponse created = contratoService.create(request);
        
        ContratoResponse response = contratoService.findById(created.id());
        
        assertNotNull(response);
        assertEquals(created.id(), response.id());
        assertEquals("Contrato Teste", response.textoContrato());
    }

    @Test
    void testUpdate_ContratoExistente_DeveAtualizar() {
        ContratoRequest createRequest = new ContratoRequest("Contrato Original", devedor.getId().toString(), null);
        ContratoResponse created = contratoService.create(createRequest);
        
        ContratoRequest updateRequest = new ContratoRequest("Contrato Atualizado", devedor.getId().toString(), null);
        ContratoResponse response = contratoService.update(created.id(), updateRequest);
        
        assertEquals("Contrato Atualizado", response.textoContrato());
    }

    @Test
    void testDelete_ContratoExistente_DeveDeletar() {
        ContratoRequest request = new ContratoRequest("Contrato Teste", devedor.getId().toString(), null);
        ContratoResponse created = contratoService.create(request);
        
        contratoService.delete(created.id());
        
        assertFalse(contratoRepository.existsById(created.id()));
    }
}

