package br.com.esig.domain.models;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2025-08-23T05:08:21.347-0300")
@StaticMetamodel(CargoVencimento.class)
public class CargoVencimento_ {
	public static volatile SingularAttribute<CargoVencimento, Long> id;
	public static volatile SingularAttribute<CargoVencimento, Cargo> cargo;
	public static volatile SingularAttribute<CargoVencimento, Vencimento> vencimento;
}
