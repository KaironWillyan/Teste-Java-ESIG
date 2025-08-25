package br.com.esig.domain.repositories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import br.com.esig.domain.models.Pessoa;



@ApplicationScoped
public class PessoaRepository {

    @Inject
    private EntityManager em;

    public Pessoa find(Long id) {
        return em.find(Pessoa.class, id);
    }

    public Pessoa save(Pessoa p) {
        if (p.getId() == null) {
            em.persist(p);
            return p;
        } else {
            return em.merge(p);
        }
    }
    
    public List<Pessoa> findAll() {
        return em.createQuery("select p from Pessoa p order by p.nome", Pessoa.class)
                 .getResultList();
    }
    

    public List<Pessoa> findWithFilters(String nome, Long cargoId, boolean incluirInativos) {
        StringBuilder jpql = new StringBuilder();
        jpql.append("select p from Pessoa p ");
        jpql.append("left join fetch p.cargo c ");
        jpql.append("where 1=1 ");

        Map<String, Object> params = new HashMap<>();

        if (nome != null && !nome.trim().isEmpty()) {
            jpql.append("and lower(p.nome) like :nome ");
            params.put("nome", "%" + nome.trim().toLowerCase() + "%");
        }
        if (cargoId != null) {
            jpql.append("and c.id = :cargoId ");
            params.put("cargoId", cargoId);
        }
        if (!incluirInativos) {
            jpql.append("and p.ativo = true ");
        }

        jpql.append("order by p.nome asc");

        TypedQuery<Pessoa> q = em.createQuery(jpql.toString(), Pessoa.class);
        params.forEach(q::setParameter);
        return q.getResultList();
    }
}
