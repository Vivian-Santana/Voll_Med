package med.voll.api.controller;

import jakarta.validation.Valid;
import med.voll.api.domain.medico.Medico;
import med.voll.api.domain.medico.MedicoRepository;
import med.voll.api.domain.paciente.Paciente;
import med.voll.api.domain.paciente.PacienteRepository;
import med.voll.api.domain.usuario.DadosAutenticacao;
import med.voll.api.domain.usuario.Usuario;
import med.voll.api.infra.security.DadosTokenJWT;
import med.voll.api.infra.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/login")
public class AutenticacaoController {

    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @PostMapping
    public ResponseEntity efetuarLogin(@RequestBody @Valid DadosAutenticacao dados) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(dados.login(), dados.senha());
        var authentication = manager.authenticate(authenticationToken);

        var usuario = (Usuario) authentication.getPrincipal();

     // Se for PACIENTE ou MEDICO, verifica se está ativo
        if (usuario.getRole().equals("PACIENTE") || usuario.getRole().equals("MEDICO")) {
            boolean ativo = usuario.getRole().equals("PACIENTE")
                ? pacienteRepository.findByUsuarioLogin(usuario.getLogin())
                      .map(Paciente::getAtivo)
                      .orElse(false)
                : medicoRepository.findByUsuarioLogin(usuario.getLogin())
                      .map(Medico::getAtivo)
                      .orElse(false);

            if (!ativo) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "login não encontrado");
            }
        }

        var tokenJWT = tokenService.gerarToken(usuario);
        return ResponseEntity.ok(new DadosTokenJWT(tokenJWT));
    }

}
