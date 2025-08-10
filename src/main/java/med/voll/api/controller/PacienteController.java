package med.voll.api.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import med.voll.api.domain.medico.Medico;
import med.voll.api.domain.paciente.*;
import med.voll.api.domain.usuario.Usuario;
import med.voll.api.domain.usuario.UsuarioRepository;
import med.voll.api.domain.usuario.Usuario.Role;
import med.voll.api.domain.paciente.*;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("pacientes")
@SecurityRequirement(name = "bearer-key")
public class PacienteController {

    @Autowired
    private PacienteRepository repository;
    
    @Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private UsuarioRepository usuarioRepository;

    @PostMapping
    @Transactional
    public ResponseEntity cadastrar(@RequestBody @Valid DadosCadastroPaciente dados, UriComponentsBuilder uriBuilder) {
    	
    	var usuario = new Usuario(
                null,
                dados.email(),
                passwordEncoder.encode(dados.senha()),
                Role.ROLE_PACIENTE
            );
    		usuarioRepository.save(usuario);
    	
        var paciente = new Paciente(dados);
        paciente.setUsuario(usuario);
        	
        repository.save(paciente);
            
        var uri = uriBuilder.path("/pacientes/{id}").buildAndExpand(paciente.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosDetalhamentoPaciente(paciente));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<DadosListagemPaciente>>listar(@PageableDefault(page = 0, size = 10, sort = {"nome"})Pageable paginacao) {
    	var usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	
    	if (!(usuarioLogado instanceof Usuario usuario) || !usuario.getRole().equals(Usuario.Role.ROLE_ADMIN)) {
    		throw new AccessDeniedException("Acesso negado");
        }
    	
    	var page = repository.findAllByAtivoTrue(paginacao)
        		.map(DadosListagemPaciente::new);
        return ResponseEntity.ok(page);
    }

    @PutMapping
    @Transactional
    @PreAuthorize("hasAnyRole('PACIENTE', 'ADMIN')")
    public ResponseEntity atualizar(@RequestBody @Valid DadosAtualizacaoPaciente dados) {
    	var usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
    	
    	var paciente = repository.findByUsuarioLogin(usuarioLogado)
		        .orElseThrow(() -> new RuntimeException("Paciente não encontrado."));

		    if (!paciente.getId().equals(dados.id())) {
		    	throw new AccessDeniedException("Acesso negado.");
		    }
    	
		    paciente.atualizarInformacoes(dados);
       
        return ResponseEntity.ok(new DadosDetalhamentoPaciente(paciente));
    }

    @DeleteMapping("/{id}")//SOFT DELETE
    @Transactional
    @PreAuthorize("hasAnyRole('PACIENTE', 'ADMIN')")
    public ResponseEntity remover(@PathVariable Long id, Authentication authentication) {
    	var usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
    	
        var paciente = repository.getReferenceById(id);
        
        if (!paciente.getUsuario().getLogin().equals(usuarioLogado)) {
            throw new AccessDeniedException("Acesso negado.");
        }
        
        paciente.inativar();
        return ResponseEntity.ok("Paciente desativado com sucesso!");
    }
   
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PACIENTE', 'ADMIN')")
    public ResponseEntity<?> detalhar(@PathVariable Long id) {
        var usuarioLogado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        var paciente = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente não encontrado"));

        if (usuarioLogado.getRole().equals(Usuario.Role.ROLE_PACIENTE) &&
            !paciente.getUsuario().getId().equals(usuarioLogado.getId())) {
            throw new AccessDeniedException("Acesso negado.");
        }

        return ResponseEntity.ok(new DadosDetalhamentoPaciente(paciente));
    }
    
}
