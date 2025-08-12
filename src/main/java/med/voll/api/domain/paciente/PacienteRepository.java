package med.voll.api.domain.paciente;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import med.voll.api.domain.medico.Medico;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
	
	Optional<Paciente> findByUsuarioLogin(String login);
	
	Optional<Paciente> findByUsuarioId(Long usuarioId);
	
	Optional<Paciente> findById(Long Id);
	
    Page<Paciente> findAllByAtivoTrue(Pageable paginacao);

    @Query("""
            select distinct p.ativo
            from Paciente p
            where
            p.id = :id
            """)
    Boolean findAtivoById(Long id);
}
