package med.voll.api.domain.paciente;

import med.voll.api.domain.endereco.Endereco;

public record DadosPacienteUsuario(Long idPaciente,
        Long idUsuario,
        String nome,
        String email,
        String telefone,
        String cpf,
        Endereco endereco) {

	public DadosPacienteUsuario(Paciente p) {
        this(
            p.getId(),
            p.getUsuario().getId(),
            p.getNome(),
            p.getEmail(),
            p.getTelefone(),
            p.getCpf(),
            p.getEndereco()
        );
    }
}
