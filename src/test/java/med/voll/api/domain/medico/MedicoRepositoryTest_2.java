package med.voll.api.domain.medico;

import med.voll.api.domain.endereco.Endereco;
import med.voll.api.domain.usuario.Usuario;
import med.voll.api.domain.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class MedicoRepositoryTest_2 {

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void deveRetornarUsuarioAssociadoAoMedico() {
        
        Usuario usuario = new Usuario(null, "teste@medico.com", "senha123", Usuario.Role.ROLE_MEDICO);
        usuario = usuarioRepository.save(usuario);

        Endereco endereco = new Endereco(
                "Rua XPTO",       // logradouro
                "123",            // numero
                "Apto 10",        // complemento
                "Centro",         // bairro
                "00000-000",      // cep
                "São Paulo",      // cidade
                "SP"              // uf
        );
        
        // Cria e salva um médico associado ao usuário
        Medico medico = new Medico();
        medico.setNome("Dr. Teste");
        medico.setEmail("teste@medico.com");
        medico.setCrm("123456");
        medico.setEspecialidade(Especialidade.CARDIOLOGIA);
        medico.setTelefone("99999-9999");
        medico.setAtivo(true);
        medico.setUsuario(usuario); // associa o usuário
        medico.setEndereco(endereco);
        medico = medicoRepository.save(medico);

        // Recupera o médico do banco
        Medico medicoEncontrado = medicoRepository.findById(medico.getId()).orElseThrow();

        // Verifica se o relacionamento está funcionando
        assertNotNull(medicoEncontrado.getUsuario());
        assertEquals("teste@medico.com", medicoEncontrado.getUsuario().getLogin());
        
        System.out.println("ID do médico salvo: " + medico.getId());
    }
}
