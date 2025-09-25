package med.voll.api.domain.consulta.validacoes.agendamento;

import med.voll.api.domain.ValidacaoException;
import med.voll.api.domain.consulta.ConsultaRepository;
import med.voll.api.domain.consulta.DadosAgendamentoConsulta;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidadorPacienteSemOutraConsultaNoDia implements ValidadorAgendamentoDeConsulta {

    @Autowired
    private ConsultaRepository repository;

    @Override
    public void validar(DadosAgendamentoConsulta dados) {
        
    	var inicio = dados.data().minusHours(1);
        var fim = dados.data().plusHours(1).minusSeconds(1);
        
        //impede que o paciente agende consultas com menos de 1h de intervalo entre elas
        var pacienteJaTemConsultaNoIntervalo = repository
                .existsByPacienteIdAndDataBetweenAndMotivoCancelamentoIsNull(dados.idPaciente(),inicio, fim);

        if (pacienteJaTemConsultaNoIntervalo) {
            throw new ValidacaoException("Você já possui uma consulta no mesmo horario ou em um horario próximo, agende uma nova com uma 1 hora de intervalo");
        }

    }
}
