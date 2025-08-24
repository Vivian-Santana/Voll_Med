package med.voll.api.infra.security;

public record DadosTokenJWT(String token, Long id, String role) {//*adaptação retorna o id do usuario e a role junto com o token
	
}
