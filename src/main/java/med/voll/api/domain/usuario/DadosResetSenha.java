package med.voll.api.domain.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DadosResetSenha(
    @NotBlank(message = "O login não pode ser vazio") 
    String login,
    
    @NotBlank(message = "A senha atual não pode ser vazia") 
    String senhaAtual,
    
    @NotBlank(message = "A nova senha não pode ser nula ou vazia") 
    @Size(min = 6, message = "A nova senha deve ter no mínimo 6 caracteres")
    String novaSenha
) {}

