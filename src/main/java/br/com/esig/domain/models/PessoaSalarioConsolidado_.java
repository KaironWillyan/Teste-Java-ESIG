package br.com.esig.domain.models;

import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2025-08-23T05:08:21.360-0300")
@StaticMetamodel(PessoaSalarioConsolidado.class)
public class PessoaSalarioConsolidado_ {
	public static volatile SingularAttribute<PessoaSalarioConsolidado, Long> pessoaId;
	public static volatile SingularAttribute<PessoaSalarioConsolidado, Pessoa> pessoa;
	public static volatile SingularAttribute<PessoaSalarioConsolidado, String> nomePessoa;
	public static volatile SingularAttribute<PessoaSalarioConsolidado, String> nomeCargo;
	public static volatile SingularAttribute<PessoaSalarioConsolidado, BigDecimal> salario;
}
