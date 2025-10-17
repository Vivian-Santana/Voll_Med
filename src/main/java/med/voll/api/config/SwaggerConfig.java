package med.voll.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.PostConstruct;

@Configuration
public class SwaggerConfig {
	
	@Value("${app.url:https://vollmed-production.up.railway.app}")
    private String serverUrl;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;
    
    @Autowired
    private OpenAPI openAPI; // usa o bean padrão do SpringDoc

    @PostConstruct
    public void customOpenAPI() {

        String environment = switch (activeProfile.toLowerCase()) {
            case "dev" -> "Ambiente de Desenvolvimento";
            case "prod" -> "Ambiente de Produção";
            default -> "Ambiente Padrão";
        };

        openAPI.addServersItem(new Server()
        		.url(serverUrl)
        		.description(environment));
        
        openAPI.getInfo()
        	.title("API Voll.med - " + environment)
        	.description("Documentação da API Voll.med, contendo as funcionalidades de CRUD de médicos e de pacientes, "
        			+ "além de agendamento e cancelamento de consultas")
        	.version("1.1");
    }
}
