package med.voll.api.infra.exception;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AccessDeniedHandlerPersonalizado implements AccessDeniedHandler{
	
	@Override
    public void handle(HttpServletRequest request, 
    				   HttpServletResponse response,
                       AccessDeniedException accessDeniedException) 
          throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");

        String json = """
        {
          "status": 403,
          "message": "Você não tem permissão para realizar esta ação."
        }
        """;

        response.getWriter().write(json);
    }

}
