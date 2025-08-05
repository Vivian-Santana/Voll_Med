package med.voll.api.infra.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import med.voll.api.domain.ValidacaoException;

import java.nio.file.AccessDeniedException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

@RestControllerAdvice
public class TratadorDeErros {

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity tratarErro404() {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Recurso não encontrado");
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity tratarErro400(MethodArgumentNotValidException ex) {
		var erros = ex.getFieldErrors();
		return ResponseEntity.badRequest().body(erros.stream().map(DadosErroValidacao::new).toList());
	}

	@ExceptionHandler(ValidacaoException.class)
	public ResponseEntity tratarErroRegraDeNegocio(ValidacaoException ex) {
		return ResponseEntity.badRequest().body(ex.getMessage());
	}

	private record DadosErroValidacao(String campo, String mensagem) {
		public DadosErroValidacao(FieldError erro) {
			this(erro.getField(), erro.getDefaultMessage());
		}
	}

	@ExceptionHandler(InvalidFormatException.class)
	public ResponseEntity<?> tratarEnumInvalido(InvalidFormatException ex) {
		var targetType = ex.getTargetType();

		// VERIFICA SE É UM ENUM
		if (targetType.isEnum()) {
			var valoresValidos = String.join(", ", ((Class<?>) targetType).getEnumConstants() != null
					? ((Class<?>) targetType).getEnumConstants().length > 0
							? java.util.Arrays.stream(((Class<?>) targetType).getEnumConstants()).map(Object::toString)
									.collect(Collectors.toList())
							: java.util.Collections.emptyList()
					: java.util.Collections.emptyList());

			var mensagem = "Valor inválido para o campo '" + ex.getPath().get(0).getFieldName()
					+ "'. Os valores aceitos são: " + valoresValidos;

			return ResponseEntity.badRequest().body(new ErroDTO(mensagem));
		}

		return ResponseEntity.badRequest().body(new ErroDTO("Formato de valor inválido."));
	}

	// FALLBACK PARA QUALQUER EXCEÇÃO NÃO TRATADA
	/*
	@ExceptionHandler(Exception.class)
	public ResponseEntity tratarErro500(Exception ex) {
		ex.printStackTrace(); // OPCIONAL: ÚTIL PARA DEPURAÇÃO
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErroDTO("Erro interno no servidor. Tente novamente mais tarde."));
	}
	*/
	
	/*
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<?> tratarErroAcessoNegado(HttpServletRequest request, AccessDeniedException ex) {
	    String path = request.getRequestURI();

	    if (path.startsWith("/medicos")) {
	        return ResponseEntity
	            .status(HttpStatus.FORBIDDEN)
	            .body("Você não tem permissão para excluir esse médico. Só é possível excluir sua própria conta.");
	    }

	    return ResponseEntity
	        .status(HttpStatus.FORBIDDEN)
	        .body("Acesso negado.");
	}
	*/

	// CLASSE PARA PADRONIZAR RESPOSTA
	public record ErroDTO(String erro) {
	}

}
