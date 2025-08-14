package med.voll.api.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import med.voll.api.domain.medico.*;
import med.voll.api.domain.paciente.DadosDetalhamentoPaciente;
import med.voll.api.domain.usuario.Usuario;
import med.voll.api.domain.usuario.Usuario.Role;
import med.voll.api.domain.usuario.UsuarioRepository;

import java.util.Map;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("medicos")
@SecurityRequirement(name = "bearer-key")
public class MedicoController {
	
    private final MedicoRepository medicorepository;
	private final PasswordEncoder passwordEncoder;
	private final UsuarioRepository usuarioRepository;

    @PostMapping
    @Transactional
    public ResponseEntity cadastrar(@RequestBody @Valid DadosCadastroMedico dados, UriComponentsBuilder uriBuilder) {
        
    	var usuario = new Usuario(
                null,
                dados.email(),
                passwordEncoder.encode(dados.senha()),
                Role.ROLE_MEDICO
            );
            usuarioRepository.save(usuario);
            
        // ASSOCIA O USUÁRIO AO MÉDICO
    	var medico = new Medico(dados);
    	medico.setUsuario(usuario);
        
    	//System.out.println("Usuário criado com ID: " + usuario.getId());
        //System.out.println("Médico associando usuário: " + (medico.getUsuario() != null ? medico.getUsuario().getId() : "null"));
        
    	medicorepository.save(medico);

        var uri = uriBuilder.path("medicos/{id}").buildAndExpand(medico.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosDetalhamentoMedico(medico));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listar(@PageableDefault(size = 10, sort = {"nome"}) Pageable paginacao) {
    	var page = medicorepository.findAllByAtivoTrue(paginacao)
    			.map(DadosListagemMedico::new);
        return ResponseEntity.ok(page);
    }

    @PutMapping
    @Transactional
    @PreAuthorize("hasAnyRole('MEDICO', 'ADMIN')")
    public ResponseEntity atualizar(@RequestBody @Valid DadosAtualizacaoMedicos dados,
    								@AuthenticationPrincipal Usuario usuarioLogado) {
    	
        if (usuarioLogado.getRole() == Usuario.Role.ROLE_MEDICO) {
            var medico = medicorepository.findByUsuarioLogin(usuarioLogado.getLogin())
                    .orElseThrow(() -> new RuntimeException("Médico não encontrado"));

            if (!medico.getId().equals(dados.id())) {
                throw new AccessDeniedException("Acesso negado");
            }

            medico.atualizarInformacoes(dados);
            return ResponseEntity.ok(new DadosDetalhamentoMedico(medico));
        }

        var medico = medicorepository.findById(dados.id())
                .orElseThrow(() -> new RuntimeException("Médico não encontrado"));

    	medico.atualizarInformacoes(dados);

        return ResponseEntity.ok(new DadosDetalhamentoMedico(medico));
    }

    @DeleteMapping("/{id}")//SOFT DELETE
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or " + "(hasRole('MEDICO') and #id == principal.id)")
    public ResponseEntity<?> excluir(@PathVariable Long id, Authentication authentication) {	    	
    	var medico = medicorepository.findById(id)
    			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Médico não encontrado"));
    	
        medico.excluir();
        return ResponseEntity.ok("Médico desativado com sucesso!");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @medicorepository.findById(#id).get().usuario.id == principal.id")
    public ResponseEntity<?> detalhar(@PathVariable Long id, Authentication authentication) {
    	var medico = medicorepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Médico não encontrado"));

        return ResponseEntity.ok(new DadosDetalhamentoMedico(medico));
    }

}
