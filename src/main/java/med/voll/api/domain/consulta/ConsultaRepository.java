package med.voll.api.domain.consulta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    boolean existsByMedicoIdAndDataAndMotivoCancelamentoIsNull(Long idMedico, LocalDateTime data);

    boolean existsByPacienteIdAndDataBetween(Long idPaciente, LocalDateTime primeiroHorario, LocalDateTime ultimoHorario);
    
    List<Consulta> findByPacienteId(Long idPaciente);
    
    LocalDateTime inicioHoje = LocalDate.now().atStartOfDay();
    LocalDateTime fimInfinito = LocalDateTime.of(3000, 1, 1, 0, 0); // ou qualquer data muito no futuro
    
    @Query("SELECT c FROM Consulta c"
    		+ " WHERE c.paciente.id = :pacienteId "
    		+ "AND c.motivoCancelamento IS NULL "
    		+ "AND c.data BETWEEN :inicioHoje AND :fimInfinito")
    List<Consulta> buscarAtivasEFuturas(@Param("pacienteId") Long pacienteId,
    									@Param("inicioHoje") LocalDateTime inicioHoje, 
    									@Param("fimInfinito") LocalDateTime fimInfinito);
}
