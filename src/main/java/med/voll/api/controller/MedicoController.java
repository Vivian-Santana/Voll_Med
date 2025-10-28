package med.voll.api.controller;

import java.util.List;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import med.voll.api.domain.medico.DadosAtualizacaoMedicos;
import med.voll.api.domain.medico.DadosCadastroMedico;
import med.voll.api.domain.medico.DadosDetalhamentoMedico;
import med.voll.api.domain.medico.DadosListagemMedico;
import med.voll.api.domain.medico.Medico;
import med.voll.api.domain.medico.MedicoRepository;
import med.voll.api.domain.usuario.Usuario;
import med.voll.api.domain.usuario.Usuario.Role;
import med.voll.api.domain.usuario.UsuarioRepository;

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
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN', 'PACIENTE')")
    public ResponseEntity<?> listar(@PageableDefault(size = 10, sort = {"nome"}) Pageable paginacao) {
    	var page = medicorepository.findAllByAtivoTrue(paginacao)
    			.map(DadosListagemMedico::new);
        return ResponseEntity.ok(page);
    }
    
    // listar sem paginação
    @GetMapping("/todos")
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN', 'PACIENTE')")
    public List<DadosListagemMedico> listarTodos() {
		return medicorepository.findAllByAtivoTrue()
                         .stream()
                         .map(DadosListagemMedico::new)
                         .toList();
    }

    @PutMapping
    @Transactional
    @PreAuthorize("hasAnyRole('MEDICO', 'ADMIN')")
    public ResponseEntity<DadosDetalhamentoMedico> atualizar(@RequestBody @Valid DadosAtualizacaoMedicos dados,
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
    @PreAuthorize("hasRole('ADMIN') or @medicoRepository.findById(#id).get().usuario.id == principal.id")
    public ResponseEntity<?> excluir(@PathVariable Long id, Authentication authentication) {	    	
    	var medico = medicorepository.findById(id)
    			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Médico não encontrado"));
    	
        medico.excluir();
        return ResponseEntity.ok("Médico desativado com sucesso!");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @medicoRepository.findById(#id).get().usuario.id == principal.id")
    public ResponseEntity<?> detalhar(@PathVariable Long id, Authentication authentication) {
    	var medico = medicorepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Médico não encontrado"));

        return ResponseEntity.ok(new DadosDetalhamentoMedico(medico));
    }

}
