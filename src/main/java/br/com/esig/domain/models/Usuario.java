package br.com.esig.domain.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.*;

@Entity
@Table(name = "usuario")
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 60) 
    private String senha;

    @Column(name = "codigo_recuperacao", length = 36)
    private String codigoRecuperacao;

    @Column(name = "data_expiracao_codigo")
    private LocalDateTime dataExpiracaoCodigo;

    @Column(nullable = false)
    private boolean ativo = true;
    
    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin = false;
    
    public Usuario() {}

	public Usuario(Long id, String nome, String email, String senha, String codigoRecuperacao,
			LocalDateTime dataExpiracaoCodigo, boolean ativo, boolean isAdmin) {
		super();
		this.id = id;
		this.nome = nome;
		this.email = email;
		this.senha = senha;
		this.codigoRecuperacao = codigoRecuperacao;
		this.dataExpiracaoCodigo = dataExpiracaoCodigo;
		this.ativo = ativo;
		this.isAdmin = isAdmin;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getCodigoRecuperacao() {
		return codigoRecuperacao;
	}

	public void setCodigoRecuperacao(String codigoRecuperacao) {
		this.codigoRecuperacao = codigoRecuperacao;
	}

	public LocalDateTime getDataExpiracaoCodigo() {
		return dataExpiracaoCodigo;
	}

	public void setDataExpiracaoCodigo(LocalDateTime dataExpiracaoCodigo) {
		this.dataExpiracaoCodigo = dataExpiracaoCodigo;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}
	
	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	@Override
	public int hashCode() {
		return Objects.hash(ativo, codigoRecuperacao, dataExpiracaoCodigo, email, id, nome, senha);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Usuario other = (Usuario) obj;
		return ativo == other.ativo && Objects.equals(codigoRecuperacao, other.codigoRecuperacao)
				&& Objects.equals(dataExpiracaoCodigo, other.dataExpiracaoCodigo) && Objects.equals(email, other.email)
				&& Objects.equals(id, other.id) && Objects.equals(nome, other.nome)
				&& Objects.equals(senha, other.senha);
	}

    
    
}