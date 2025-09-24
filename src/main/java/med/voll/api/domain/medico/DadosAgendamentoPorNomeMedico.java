package med.voll.api.domain.medico;

import java.time.LocalDateTime;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

public record DadosAgendamentoPorNomeMedico(
		@NotNull
        String nomeMedico,// nome do médico ao em vez de id

        @NotNull
        Long idPaciente,

        @NotNull
        @Future
        LocalDateTime data,

        String especialidade) {

}
