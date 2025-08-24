package br.com.esig.domain.models;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.*;

@Entity
@Table(name = "cargo_vencimentos")
public class CargoVencimento implements Serializable {

	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_id", nullable = false)
    private Cargo cargo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vencimento_id", nullable = false)
    private Vencimento vencimento;
    
    public CargoVencimento() {}

    public CargoVencimento(Cargo cargo, Vencimento vencimento) {
		super();
		this.cargo = cargo;
		this.vencimento = vencimento;
	}
    
    public CargoVencimento(Long id, Cargo cargo, Vencimento vencimento) {
		super();
		this.id = id;
		this.cargo = cargo;
		this.vencimento = vencimento;
	}

	// Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cargo getCargo() {
        return cargo;
    }

    public void setCargo(Cargo cargo) {
        this.cargo = cargo;
    }

    public Vencimento getVencimento() {
        return vencimento;
    }

    public void setVencimento(Vencimento vencimento) {
        this.vencimento = vencimento;
    }

    // hashCode e equals
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CargoVencimento other = (CargoVencimento) obj;
        return Objects.equals(id, other.id);
    }
}