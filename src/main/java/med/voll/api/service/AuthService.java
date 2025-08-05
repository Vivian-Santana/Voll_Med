package med.voll.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import med.voll.api.domain.medico.MedicoRepository;
import med.voll.api.domain.usuario.Usuario;

@Service("authService")
public class AuthService {
	
	@Autowired
    private final MedicoRepository medicoRepository;
	
	public AuthService(MedicoRepository medicoRepository) {
	    this.medicoRepository = medicoRepository;
	}


    public boolean podeAcessarMedico(Long id) {
        var usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // SE FOR ADMIN, PODE ACESSAR QUALQUER DADO
        if (usuario.getRole().name().equals("ROLE_ADMIN")) {
            return true;
        }

        // SE FOR MEDICO, PODE ACESSAR APENAS SEUS PRÓPRIOS DADOS
        var medico = medicoRepository.findById(id);
        return medico.isPresent() && medico.get().getUsuario().getId().equals(usuario.getId());
    }
    
    public boolean podeExcluirMedico(Long idDoMedico) {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();

        return medicoRepository.findById(idDoMedico)
                .map(medico -> medico.getUsuario() != null && medico.getUsuario().getLogin().equals(login))
                .orElse(false);
    }
    
}
