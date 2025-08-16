package med.voll.api.domain.usuario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import med.voll.api.domain.medico.MedicoRepository;
import med.voll.api.domain.paciente.PacienteRepository;
import med.voll.api.domain.usuario.Usuario.Role;
import med.voll.api.infra.exception.ContaNaoEncontradaException;

@Service
@RequiredArgsConstructor
public class AutenticacaoService implements UserDetailsService {
	
	private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByLogin(username)
        		.orElseThrow(() -> new ContaNaoEncontradaException());
        
     // Verifica se é Paciente
        if (usuario.getRole() == Role.ROLE_PACIENTE) {
            var paciente = pacienteRepository.findByUsuarioId(usuario.getId())
                    .orElseThrow(() -> new UsernameNotFoundException("Conta não encontrada"));
            if (!paciente.getAtivo()) {
                throw new ContaNaoEncontradaException();
            }
        }

        // Verifica se é Médico
        if (usuario.getRole() == Role.ROLE_MEDICO) {
            var medico = medicoRepository.findByUsuarioId(usuario.getId())
                    .orElseThrow(() -> new UsernameNotFoundException("Conta não encontrada"));
            if (!medico.getAtivo()) {
                throw new ContaNaoEncontradaException();
            }
        }

        return usuario; //USUARIO IMPLEMENTA USERDETAILS
    }
}
