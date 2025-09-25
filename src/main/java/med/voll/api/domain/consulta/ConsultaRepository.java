package med.voll.api.domain.consulta;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    boolean existsByMedicoIdAndDataAndMotivoCancelamentoIsNull(Long idMedico, LocalDateTime data);

    boolean existsByPacienteIdAndDataBetweenAndMotivoCancelamentoIsNull(Long pacienteId, LocalDateTime inicio, LocalDateTime fim);
    
    List<Consulta> findByPacienteId(Long idPaciente, Sort sort);
    
    @Query("SELECT c FROM Consulta c"
    		+ " WHERE c.paciente.id = :pacienteId "
    		+ "AND c.motivoCancelamento IS NULL "
    		+ "AND c.data BETWEEN :inicioHoje AND :fimInfinito")
    List<Consulta> buscarAtivasEFuturas(@Param("pacienteId") Long pacienteId,
    									@Param("inicioHoje") LocalDateTime inicioHoje, 
    									@Param("fimInfinito") LocalDateTime fimInfinito);
}
