package med.voll.api.domain.consulta;

import med.voll.api.domain.ValidacaoException;
import med.voll.api.domain.consulta.validacoes.agendamento.ValidadorAgendamentoDeConsulta;
import med.voll.api.domain.consulta.validacoes.cancelamento.ValidadorCancelamentoDeConsulta;
import med.voll.api.domain.medico.DadosAgendamentoPorNomeMedico;
import med.voll.api.domain.medico.Medico;
import med.voll.api.domain.medico.MedicoRepository;
import med.voll.api.domain.paciente.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class AgendaDeConsultas {

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private List<ValidadorAgendamentoDeConsulta> validadores;

    @Autowired
    private  List<ValidadorCancelamentoDeConsulta> validadoresCancelamento;

    public DadosDetalhamentoConsulta agendar(DadosAgendamentoConsulta dados) {
        if (!pacienteRepository.existsById(dados.idPaciente())) {
            throw new ValidacaoException("Id do paciente informado não existe!");
        }

        if (dados.idMedico() != null && !medicoRepository.existsById(dados.idMedico())) {
            throw new ValidacaoException("Id do médico informado não existe!");
        }

        validadores.forEach(v -> v.validar(dados));

        var paciente = pacienteRepository.getReferenceById(dados.idPaciente());
        var medico = escolherMedico(dados);
        if (medico == null) {
            throw new ValidacaoException("Não existe médico disponível nessa data!");
        }

        var consulta = new Consulta(null, medico, paciente, dados.data(), null);
        consultaRepository.save(consulta);

        return new DadosDetalhamentoConsulta(consulta);
    }
    
    public DadosDetalhamentoConsulta agendarPorNome(DadosAgendamentoPorNomeMedico dados) {

        if (!pacienteRepository.existsById(dados.idPaciente())) {
            throw new ValidacaoException("Id do paciente informado não existe!");
        }

        Medico medico = medicoRepository.findByNome(dados.nomeMedico())
                .orElseThrow(() -> new ValidacaoException("Médico com o nome informado não existe!"));

        if (dados.especialidade() != null && !medico.getEspecialidade().name().equals(dados.especialidade())) {
            throw new ValidacaoException("O médico informado não possui a especialidade indicada!");
        }

        validadores.forEach(v -> v.validar(
                new DadosAgendamentoConsulta(
                        medico.getId(),
                        dados.idPaciente(),
                        dados.data(),
                        medico.getEspecialidade()
                )
        ));

        var paciente = pacienteRepository.getReferenceById(dados.idPaciente());
        var consulta = new Consulta(null, medico, paciente, dados.data(), null);
        consultaRepository.save(consulta);

        return new DadosDetalhamentoConsulta(consulta);
    }


    private Medico escolherMedico(DadosAgendamentoConsulta dados) {
        if (dados.idMedico() != null) {
            return medicoRepository.getReferenceById(dados.idMedico());
        }

        if (dados.especialidade() == null) {
            throw new ValidacaoException("Especialidade é obrigatória quando médico não for escolhido!");
        }

        return medicoRepository.escolherMedicoAleatorioLivreNaData(dados.especialidade(), dados.data());
    }


    public void cancelar(DadosCancelamentoConsulta dados) {
        if (!consultaRepository.existsById(dados.idConsulta())) {
            throw new ValidacaoException("Id da consulta informado não existe!");
        }

        validadoresCancelamento.forEach(v -> v.validar(dados));

        var consulta = consultaRepository.getReferenceById(dados.idConsulta());
        consulta.cancelar(dados.motivo());
    }
    
    public List<DadosDetalhamentoConsulta> listarConsultasDoPaciente(Long pacienteId) {
    	var inicioHoje = LocalDate.now().atStartOfDay();
        var fimInfinito = LocalDateTime.of(3000, 1, 1, 0, 0);

        // busca as consultas ativas/futuras do paciente
        var consultas = consultaRepository.buscarAtivasEFuturas(pacienteId, inicioHoje, fimInfinito)
                                         .stream()
                                         .sorted(Comparator.comparing(Consulta::getData)) // ordena por data
                                         .toList();

        // mapeia para DTO
        return consultas.stream()
                        .map(DadosDetalhamentoConsulta::new)
                        .toList();
    }

}
