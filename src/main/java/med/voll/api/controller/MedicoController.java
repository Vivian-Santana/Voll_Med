package med.voll.api.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import med.voll.api.domain.medico.*;
import med.voll.api.domain.usuario.Usuario;
import med.voll.api.domain.usuario.Usuario.Role;
import med.voll.api.domain.usuario.UsuarioRepository;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("medicos")
@SecurityRequirement(name = "bearer-key")
public class MedicoController {
	
	@Autowired
    private MedicoRepository repository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private UsuarioRepository usuarioRepository;

    @PostMapping
    @Transactional
    @PreAuthorize("permitAll()")
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
    	medico.setUsuario(usuario); // ESSA LINHA É ESSENCIAL ***************
        
    	System.out.println("Usuário criado com ID: " + usuario.getId());
        System.out.println("Médico associando usuário: " + (medico.getUsuario() != null ? medico.getUsuario().getId() : "null"));
        
    	repository.save(medico);

        var uri = uriBuilder.path("medicos/{id}").buildAndExpand(medico.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosDetalhamentoMedico(medico));
    }

    @GetMapping
    public ResponseEntity<?> listar(@PageableDefault(size = 10, sort = {"nome"}) Pageable paginacao) {
    	
    	var login = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	
    	if (!(login instanceof Usuario usuario) || !usuario.getRole().equals(Usuario.Role.ROLE_ADMIN)) {
    		var resposta = Map.of("erro", "Você não tem autorização para este acesso.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(resposta);
        }
  	
    	var page = repository.findAllByAtivoTrue(paginacao).map(DadosListagemMedico::new);
        return ResponseEntity.ok(page);
    }

    @PutMapping
    @Transactional
    public ResponseEntity atualizar(@RequestBody @Valid DadosAtualizacaoMedicos dados) {
    	String login = SecurityContextHolder.getContext().getAuthentication().getName();
    	
    	 var medico = repository.findByUsuarioLogin(login)
    		        .orElseThrow(() -> new RuntimeException("Médico não encontrado."));

    		    if (!medico.getId().equals(dados.id())) {
    		        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você só pode atualizar seus próprios dados.");
    		    }

    		    medico.atualizarInformacoes(dados);

        return ResponseEntity.ok(new DadosDetalhamentoMedico(medico));
    }

    @DeleteMapping("/{id}")//SOFT DELETE
    @Transactional
    @PreAuthorize("@authService.podeExcluirMedico(#id)")
    public ResponseEntity excluir(@PathVariable Long id) {	        
    	var medico = repository.findById(id)
    			.orElseThrow(() -> new RuntimeException("Médico não encontrado"));
    	
        medico.excluir();
        return ResponseEntity.ok("Médico desativado com sucesso!");
    }

    @GetMapping("/{id}")
    @PreAuthorize("@authService.podeAcessarMedico(#id)")
    public ResponseEntity detalhar(@PathVariable Long id) {
    	String login = SecurityContextHolder.getContext().getAuthentication().getName();
        
    	var medico = repository.findById(id)
    			.orElseThrow(() -> new RuntimeException("Médico não encontrado"));

        return ResponseEntity.ok(new DadosDetalhamentoMedico(medico));                
    }
    
    @GetMapping("/teste")
    public String testar() {
        var usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return "Olá, " + usuario.getUsername();
    }

}
