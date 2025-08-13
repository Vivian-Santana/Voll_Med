package med.voll.api.domain.usuario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import med.voll.api.domain.medico.MedicoRepository;
import med.voll.api.domain.paciente.PacienteRepository;
import med.voll.api.domain.usuario.Usuario.Role;

@Service
public class AutenticacaoService implements UserDetailsService {
	
	private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;

    @Autowired
    public AutenticacaoService(UsuarioRepository usuarioRepository,
                               PacienteRepository pacienteRepository,
                               MedicoRepository medicoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.pacienteRepository = pacienteRepository;
        this.medicoRepository = medicoRepository;
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByLogin(username)
        		.orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        
     // Verifica se é Paciente
        if (usuario.getRole() == Role.ROLE_PACIENTE) {
            var paciente = pacienteRepository.findByUsuarioId(usuario.getId())
                    .orElseThrow(() -> new UsernameNotFoundException("Conta não encontrada"));
            if (!paciente.getAtivo()) {
                throw new UsernameNotFoundException("Conta não encontrada");
            }
        }

        // Verifica se é Médico
        if (usuario.getRole() == Role.ROLE_MEDICO) {
            var medico = medicoRepository.findByUsuarioId(usuario.getId())
                    .orElseThrow(() -> new UsernameNotFoundException("Conta não encontrada"));
            if (!medico.getAtivo()) {
                throw new UsernameNotFoundException("Conta não encontrada");
            }
        }

        return usuario; //USUARIO IMPLEMENTA USERDETAILS
    }
}
