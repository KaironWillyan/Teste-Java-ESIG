package br.com.esig.domain.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.primefaces.model.SortOrder;
import br.com.esig.domain.models.PessoaSalarioConsolidado;


public class PessoaSalarioConsolidadoRepository extends GenericDAO<PessoaSalarioConsolidado> {
    public PessoaSalarioConsolidadoRepository() {
        super(PessoaSalarioConsolidado.class);
    }
    
    private Predicate[] createPredicates(CriteriaBuilder cb, Root<PessoaSalarioConsolidado> root, Map<String, Object> filters) {
        List<Predicate> predicates = new ArrayList<>();

        // Filtro por NOME (agregado, case-insensitive)
        if (filters.containsKey("nomePessoa")) {
            String nome = (String) filters.get("nomePessoa");
            if (nome != null && !nome.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("nomePessoa")), "%" + nome.toLowerCase() + "%"));
            }
        }

        // Filtro por CARGO (agregado, exato)
        if (filters.containsKey("nomeCargo")) {
            String cargo = (String) filters.get("nomeCargo");
            if (cargo != null && !cargo.trim().isEmpty()) {
                 predicates.add(cb.equal(root.get("nomeCargo"), cargo));
            }
        }

        return predicates.toArray(new Predicate[0]);
    }

    public List<PessoaSalarioConsolidado> findAndFilter(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        CriteriaBuilder cb = getManager().getCriteriaBuilder();
        CriteriaQuery<PessoaSalarioConsolidado> cq = cb.createQuery(PessoaSalarioConsolidado.class);
        Root<PessoaSalarioConsolidado> root = cq.from(PessoaSalarioConsolidado.class);

        cq.where(createPredicates(cb, root, filters));

        if (sortField != null && !sortField.isEmpty()) {
            if (sortOrder == SortOrder.ASCENDING) {
                cq.orderBy(cb.asc(root.get(sortField)));
            } else {
                cq.orderBy(cb.desc(root.get(sortField)));
            }
        } else {
            cq.orderBy(cb.asc(root.get("nomePessoa")));
        }

        TypedQuery<PessoaSalarioConsolidado> query = getManager().createQuery(cq);
        query.setFirstResult(first);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    public int countWithFilters(Map<String, Object> filters) {
        CriteriaBuilder cb = getManager().getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<PessoaSalarioConsolidado> root = cq.from(PessoaSalarioConsolidado.class);

        cq.select(cb.count(root));
        cq.where(createPredicates(cb, root, filters));

        return getManager().createQuery(cq).getSingleResult().intValue();
    }   
}