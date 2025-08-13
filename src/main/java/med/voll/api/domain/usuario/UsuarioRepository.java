package med.voll.api.domain.usuario;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    //UserDetails findByLogin(String login);
	Optional<Usuario> findByLogin(String login);
	
	Optional<Usuario> findById(Long id);
}
/*
 * O SPRING SECURITY AUTENTICA USANDO O PRÓPRIO OBJETO USUARIO.
 * O PRINCIPAL SERÁ DO TIPO USUARIO, COM ACESSO DIRETO AO GETID(), GETLOGIN(),
 * E NÃO UM TIPO GENÉRICO COMO USERDATAILS. 
 */