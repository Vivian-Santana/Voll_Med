package med.voll.api.domain.paciente;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PacienteService {
	private final PacienteRepository pacienteRepository;

    public DadosPacienteUsuario buscarPorIdUsuario(Long idUsuario) {
        var paciente = pacienteRepository.findByUsuarioId(idUsuario)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Paciente não encontrado para o usuário " + idUsuario));
        return new DadosPacienteUsuario(paciente);
    }

}
