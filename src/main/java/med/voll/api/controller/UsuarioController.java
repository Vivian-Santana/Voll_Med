package med.voll.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import med.voll.api.domain.usuario.DadosResetSenha;
import med.voll.api.domain.usuario.Usuario;
import med.voll.api.domain.usuario.UsuarioRepository;
import med.voll.api.domain.usuario.UsuarioService;
import med.voll.api.infra.exception.TratadorDeErros.MensagemResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("usuarios")
@SecurityRequirement(name = "bearer-key")//anotação q faltava para funcionar no swagger
public class UsuarioController {

	private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioService usuarioService;

    @PatchMapping("/reset-senha")
    @Transactional
    public ResponseEntity<?> resetarSenha(@RequestBody @Valid DadosResetSenha dados,
    	                                      @AuthenticationPrincipal Usuario usuarioLogado) {
    	
    	if (!passwordEncoder.matches(dados.senhaAtual(), usuarioLogado.getSenha())) {
            return ResponseEntity.badRequest()
                                 .body(new MensagemResponse("Senha atual incorreta."));
        }
    	
    	usuarioService.resetarSenha(usuarioLogado.getId(), dados.novaSenha());
    	return ResponseEntity.ok(new MensagemResponse("Senha alterada com sucesso!"));
    }

}
