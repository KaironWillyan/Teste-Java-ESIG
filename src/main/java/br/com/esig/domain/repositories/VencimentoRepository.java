package br.com.esig.domain.repositories;

import br.com.esig.domain.models.Vencimento;

public class VencimentoRepository extends GenericDAO<Vencimento> {

    public VencimentoRepository() {
        super(Vencimento.class);
    }

}