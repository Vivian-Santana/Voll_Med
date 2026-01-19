package med.voll.api.infra.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import med.voll.api.domain.usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

	private final TokenService tokenService;
	private final UsuarioRepository repository;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		var requestURI = request.getRequestURI();

		// Bypass no login (não tenta validar token aqui)
		if (requestURI.equals("/login")) {
			filterChain.doFilter(request, response);
			return;
		}

		try {

			var tokenJWT = recuperarToken(request);

			if (tokenJWT != null) {
				var subject = tokenService.getSubject(tokenJWT);
				var usuario = repository.findByLogin(subject)
						.orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + subject));

				var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());

				SecurityContextHolder.getContext().setAuthentication(authentication);
			}

			filterChain.doFilter(request, response);

		} catch (Exception ex) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json");
			response.getWriter().write("""
					    {
					      "message": "Token expirado ou inválido"
					    }
					""");
		}
	}

	private String recuperarToken(HttpServletRequest request) {
		var authorizationHeader = request.getHeader("Authorization");
		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			return authorizationHeader.replace("Bearer ", "");
		}
		return null;
	}
}
