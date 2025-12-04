package tech.devleo.projeto_orlando.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import tech.devleo.projeto_orlando.domain.Empresa;
import tech.devleo.projeto_orlando.domain.User;
import tech.devleo.projeto_orlando.repository.EmpresaRepository;
import tech.devleo.projeto_orlando.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected EmpresaRepository empresaRepository;

    protected User testUser;
    protected Empresa testEmpresa;

    @BeforeEach
    void setUp() {
        // Limpar contexto de segurança
        SecurityContextHolder.clearContext();

        // Criar usuário de teste
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"); // password
        testUser.setEnabled(true);
        testUser = userRepository.save(testUser);

        // Criar empresa de teste
        testEmpresa = new Empresa();
        testEmpresa.setName("Empresa Teste");
        testEmpresa.setCnpj("12345678000100");
        testEmpresa.setTelefone("11999999999");
        testEmpresa.setDono(testUser);
        testEmpresa = empresaRepository.save(testEmpresa);

        // Associar empresa ao usuário
        testUser.setEmpresa(testEmpresa);
        testUser = userRepository.save(testUser);

        // Configurar autenticação
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            testUser.getEmail(),
            null,
            java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}

