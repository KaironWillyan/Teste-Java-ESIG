package br.com.esig.domain.models;

import br.com.esig.domain.enums.TipoVencimento;
import java.math.BigDecimal;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2025-08-23T05:08:21.361-0300")
@StaticMetamodel(Vencimento.class)
public class Vencimento_ {
	public static volatile SingularAttribute<Vencimento, Long> id;
	public static volatile SingularAttribute<Vencimento, String> descricao;
	public static volatile SingularAttribute<Vencimento, BigDecimal> valor;
	public static volatile SingularAttribute<Vencimento, TipoVencimento> tipo;
}
