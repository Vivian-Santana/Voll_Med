package med.voll.api.domain.usuario;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collection;
import java.util.List;

@Table(name = "usuarios")
@Entity(name = "Usuario")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String login;
    private String senha;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    public enum Role {
        ROLE_ADMIN,
        ROLE_MEDICO,
        ROLE_PACIENTE
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
    	if (this.role == null) {
            throw new IllegalStateException("Usuário sem role definida!");
        }
        return List.of(new SimpleGrantedAuthority(this.role.name()));
    }

    public Long getId() {
        return id;
    }
    
    public Enum getRole() {
        return role;
    }
    
    @Override
    @JsonIgnore
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

	public void setSenha(String novaSenha) {
		this.senha = novaSenha;
	}
	
	//admin
	public void setRole(Role roleAdmin) {
	    this.role = roleAdmin;
	}

	public void setLogin(String loginAdmin) {
	    this.login = loginAdmin;
	}
}
