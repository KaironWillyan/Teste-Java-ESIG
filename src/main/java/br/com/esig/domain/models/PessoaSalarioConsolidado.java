package br.com.esig.domain.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import javax.persistence.*;

@Entity
@Table(name = "pessoa_salario_consolidado")
public class PessoaSalarioConsolidado implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long pessoaId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "pessoa_id")
    private Pessoa pessoa;

    @Column(name = "nome_pessoa", nullable = false)
    private String nomePessoa;

    @Column(name = "nome_cargo", nullable = false)
    private String nomeCargo;

    @Column(name = "salario", nullable = false, precision = 10, scale = 2)
    private BigDecimal salario;
    
	public PessoaSalarioConsolidado() {
		super();
	}

	public PessoaSalarioConsolidado(Pessoa pessoa, String nomePessoa, String nomeCargo,
			BigDecimal salario) {
		super();
		this.pessoa = pessoa;
		this.pessoaId = pessoa.getId();
		this.nomePessoa = nomePessoa;
		this.nomeCargo = nomeCargo;
		this.salario = salario;
	}

	public Long getPessoaId() {
		return pessoaId;
	}

	public void setPessoaId(Long pessoaId) {
		this.pessoaId = pessoaId;
	}

	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}

	public String getNomePessoa() {
		return nomePessoa;
	}

	public void setNomePessoa(String nomePessoa) {
		this.nomePessoa = nomePessoa;
	}

	public String getNomeCargo() {
		return nomeCargo;
	}

	public void setNomeCargo(String nomeCargo) {
		this.nomeCargo = nomeCargo;
	}

	public BigDecimal getSalario() {
		return salario;
	}

	public void setSalario(BigDecimal salario) {
		this.salario = salario;
	}

	@Override
	public int hashCode() {
		return Objects.hash(nomeCargo, nomePessoa, pessoa, pessoaId, salario);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PessoaSalarioConsolidado other = (PessoaSalarioConsolidado) obj;
		return Objects.equals(nomeCargo, other.nomeCargo) && Objects.equals(nomePessoa, other.nomePessoa)
				&& Objects.equals(pessoa, other.pessoa) && Objects.equals(pessoaId, other.pessoaId)
				&& Objects.equals(salario, other.salario);
	}	
    
}