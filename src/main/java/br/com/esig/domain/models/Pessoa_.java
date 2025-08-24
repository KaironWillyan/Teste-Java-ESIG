package br.com.esig.domain.models;

import java.time.LocalDate;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2025-08-23T18:24:53.754-0300")
@StaticMetamodel(Pessoa.class)
public class Pessoa_ {
	public static volatile SingularAttribute<Pessoa, Long> id;
	public static volatile SingularAttribute<Pessoa, String> nome;
	public static volatile SingularAttribute<Pessoa, String> cidade;
	public static volatile SingularAttribute<Pessoa, String> email;
	public static volatile SingularAttribute<Pessoa, String> cep;
	public static volatile SingularAttribute<Pessoa, String> endereco;
	public static volatile SingularAttribute<Pessoa, String> pais;
	public static volatile SingularAttribute<Pessoa, String> usuario;
	public static volatile SingularAttribute<Pessoa, String> telefone;
	public static volatile SingularAttribute<Pessoa, LocalDate> dataNascimento;
	public static volatile SingularAttribute<Pessoa, Cargo> cargo;
	public static volatile SingularAttribute<Pessoa, Boolean> ativo;
}
