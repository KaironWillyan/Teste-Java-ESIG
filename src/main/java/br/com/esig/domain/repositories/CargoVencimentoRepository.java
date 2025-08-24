package br.com.esig.domain.repositories;

import java.util.List;

import br.com.esig.domain.models.Cargo;
import br.com.esig.domain.models.CargoVencimento;

public class CargoVencimentoRepository extends GenericDAO<CargoVencimento> {

    public CargoVencimentoRepository() {
        super(CargoVencimento.class);
    }

    public List<CargoVencimento> findByCargo(Cargo cargo) {
    	String query = "SELECT cv FROM CargoVencimento cv JOIN FETCH cv.vencimento WHERE cv.cargo = :cargo";
        return getManager().createQuery(query, CargoVencimento.class)
                            .setParameter("cargo", cargo)
                            .getResultList();
    }
}