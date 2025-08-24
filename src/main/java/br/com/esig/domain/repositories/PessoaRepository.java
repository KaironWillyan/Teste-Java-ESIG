package br.com.esig.domain.repositories;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import br.com.esig.domain.models.Pessoa;

public class PessoaRepository extends GenericDAO<Pessoa> {

    public PessoaRepository() {
        super(Pessoa.class);
    }

    public List<Pessoa> findWithFilters(String nome, Long cargoId, boolean incluirInativos) {
        CriteriaBuilder cb = getManager().getCriteriaBuilder();
        CriteriaQuery<Pessoa> cq = cb.createQuery(Pessoa.class);
        Root<Pessoa> root = cq.from(Pessoa.class);
        cq.orderBy(cb.asc(root.get("nome")));

        List<Predicate> predicates = new ArrayList<>();

        if (nome != null && !nome.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%"));
        }
        if (cargoId != null) {
            predicates.add(cb.equal(root.get("cargo").get("id"), cargoId));
        }
        
        if (!incluirInativos) {
            predicates.add(cb.isTrue(root.get("ativo")));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        return getManager().createQuery(cq).getResultList();
    }
}