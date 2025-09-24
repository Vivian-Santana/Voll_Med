package med.voll.api.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import med.voll.api.domain.consulta.AgendaDeConsultas;
import med.voll.api.domain.consulta.Consulta;
import med.voll.api.domain.consulta.ConsultaRepository;
import med.voll.api.domain.consulta.DadosAgendamentoConsulta;
import med.voll.api.domain.consulta.DadosCancelamentoConsulta;
import med.voll.api.domain.consulta.DadosDetalhamentoConsulta;
import med.voll.api.domain.consulta.MotivoCancelamento;
import med.voll.api.domain.medico.DadosAgendamentoPorNomeMedico;
import med.voll.api.domain.paciente.Paciente;
import med.voll.api.domain.paciente.PacienteRepository;
import med.voll.api.domain.usuario.Usuario;

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
    
    @PostMapping("/agendar-por-nome-medico")
    @Transactional
    public ResponseEntity agendarPorNome(@RequestBody @Valid DadosAgendamentoPorNomeMedico dados) {
        var dto = agenda.agendarPorNome(dados);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping
    @Transactional
    public ResponseEntity cancelar(@RequestBody @Valid DadosCancelamentoConsulta dados) {
        agenda.cancelar(dados);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/{id}")
    @Transactional
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity cancelar(
            @PathVariable Long id,
            @RequestParam MotivoCancelamento motivo) {
        agenda.cancelar(new DadosCancelamentoConsulta(id, motivo));
        return ResponseEntity.noContent().build();
    }

    
    @GetMapping("/{id}/consultas")
    @PreAuthorize("hasRole('ADMIN') or @pacienteRepository.findById(#id).get().usuario.id == principal.id")
    public ResponseEntity<List<DadosDetalhamentoConsulta>> listarConsultas(@PathVariable Long id) {
    
        if (!pacienteRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
    	
    	var consultas = consultaRepository.findByPacienteId(id)
    									  .stream().map(DadosDetalhamentoConsulta::new)
    									  .toList();
        
    	return ResponseEntity.ok(consultas);
    }

    // ENDPOINT PARA O FRONT PEGAR CONSULTAS SEM PRECISAR PASSAR O ID DO PACIENTE
    @GetMapping("/minhas/consultas")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<List<DadosDetalhamentoConsulta>> minhasConsultas(Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        Paciente paciente = pacienteRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        
        LocalDateTime inicioHoje = LocalDate.now().atStartOfDay();
        LocalDateTime fimInfinito = LocalDateTime.of(3000, 1, 1, 0, 0);

        List<DadosDetalhamentoConsulta> consultas = consultaRepository
        		.buscarAtivasEFuturas(paciente.getId(), inicioHoje, fimInfinito)
                .stream()
                .map(DadosDetalhamentoConsulta::new)
                .toList();
        
        return ResponseEntity.ok(consultas);
    }

}
