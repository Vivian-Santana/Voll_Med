package med.voll.api.infra.startup;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import med.voll.api.domain.usuario.Usuario;
import med.voll.api.domain.usuario.Usuario.Role;
import med.voll.api.domain.usuario.UsuarioRepository;

@Configuration
@RequiredArgsConstructor
public class AdminInitializer {

	private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

	@Value("${ADMIN_LOGIN}")
    private String adminLogin;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @PostConstruct
    public void criarAdminSeNaoExistir() {
    	var adminExistente = usuarioRepository.findByLogin(adminLogin);
    	
        if (adminExistente.isEmpty()) {
            var admin = new Usuario();
            admin.setLogin(adminLogin);
            admin.setSenha(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ROLE_ADMIN);

            usuarioRepository.save(admin);
            System.out.println("Usuário ADMIN criado com sucesso!");
        } else {
            System.out.println("Usuário ADMIN já existe, não será recriado.");
        }
    }
}
