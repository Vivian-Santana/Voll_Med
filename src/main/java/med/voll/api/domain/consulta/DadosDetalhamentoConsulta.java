package med.voll.api.domain.consulta;

import java.time.LocalDateTime;


public record DadosDetalhamentoConsulta(Long id, String nomeMedico,String especialidade, LocalDateTime data, String motivoCancelamento) {

    public DadosDetalhamentoConsulta(Consulta consulta) {
        this(
        		 consulta.getId(), 
				 consulta.getMedico().getNome(),
				 consulta.getMedico().getEspecialidade().toString(),
				 consulta.getData(),
				 consulta.getMotivoCancelamento() != null ? 
				 consulta.getMotivoCancelamento().name() : null
        	);
    }
}
