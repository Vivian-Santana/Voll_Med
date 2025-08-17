package med.voll.api.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import med.voll.api.domain.consulta.AgendaDeConsultas;
import med.voll.api.domain.consulta.ConsultaRepository;
import med.voll.api.domain.consulta.DadosAgendamentoConsulta;
import med.voll.api.domain.consulta.DadosCancelamentoConsulta;
import med.voll.api.domain.consulta.DadosConsultaPacienteDTO;
import med.voll.api.domain.paciente.PacienteRepository;
import med.voll.api.domain.usuario.UsuarioRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("consultas")
@SecurityRequirement(name = "bearer-key")
public class ConsultaController {
	
	private final ConsultaRepository consultaRepository;
	private final PacienteRepository pacienteRepository;

    @Autowired
    private AgendaDeConsultas agenda;

    @PostMapping
    @Transactional
    public ResponseEntity agendar(@RequestBody @Valid DadosAgendamentoConsulta dados) {
        var dto = agenda.agendar(dados);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping
    @Transactional
    public ResponseEntity cancelar(@RequestBody @Valid DadosCancelamentoConsulta dados) {
        agenda.cancelar(dados);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}/consultas")
    @PreAuthorize("hasRole('ADMIN') or @pacienteRepository.findById(#id).get().usuario.id == principal.id")
    public ResponseEntity<List<DadosConsultaPacienteDTO>> listarConsultas(@PathVariable Long id) {
    
        if (!pacienteRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
    	
    	var consultas = consultaRepository.findByPacienteId(id)
    									  .stream().map(DadosConsultaPacienteDTO::new)
    									  .toList();
        
    	return ResponseEntity.ok(consultas);
    }

}
