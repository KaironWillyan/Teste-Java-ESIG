package br.com.esig.domain.repositories;

import br.com.esig.domain.models.Cargo;

public class CargoRepository extends GenericDAO<Cargo> {
    public CargoRepository() {
        super(Cargo.class);
    }
}