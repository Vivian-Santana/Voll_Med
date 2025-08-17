package med.voll.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import med.voll.api.domain.usuario.DadosAutenticacao;
import med.voll.api.domain.usuario.Usuario;
import med.voll.api.infra.security.DadosTokenJWT;
import med.voll.api.infra.security.TokenService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/login")
public class AutenticacaoController {
    
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @PostMapping
    public ResponseEntity<?> efetuarLogin(@RequestBody @Valid DadosAutenticacao dados) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(dados.login(), dados.senha());

        var authentication = authenticationManager.authenticate(authenticationToken);

        var tokenJWT = tokenService.gerarToken((Usuario) authentication.getPrincipal());

        return ResponseEntity.ok(new DadosTokenJWT(tokenJWT));
    }

    /*endpoint de teste Retorna o ID do usuario logado (tabela usuarios)
    @GetMapping("/teste-principal")
    public ResponseEntity<?> testarPrincipal(@AuthenticationPrincipal Object principal) {
        System.out.println("Tipo do principal: " + principal.getClass().getName());

        if (principal instanceof Usuario usuario) {
            return ResponseEntity.ok("Principal é um Usuario! ID: " + usuario.getId());
        } else {
            return ResponseEntity.ok("Principal NÃO é um Usuario. É: " + principal.getClass().getSimpleName());
        }
    }
    */

}
