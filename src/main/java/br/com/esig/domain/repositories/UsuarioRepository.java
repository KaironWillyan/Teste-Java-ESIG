package br.com.esig.domain.repositories;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import br.com.esig.domain.models.Usuario;

public class UsuarioRepository extends GenericDAO<Usuario> implements Serializable {
    private static final long serialVersionUID = 1L;

    public UsuarioRepository() {
        super(Usuario.class);
    }

    public List<Usuario> findWithFilters(String nome, String email, Boolean isAdminStatus, boolean incluirInativos) {
        CriteriaBuilder cb = getManager().getCriteriaBuilder();
        CriteriaQuery<Usuario> cq = cb.createQuery(Usuario.class);
        Root<Usuario> root = cq.from(Usuario.class);
        cq.orderBy(cb.asc(root.get("nome")));

        List<Predicate> predicates = new ArrayList<>();

        if (nome != null && !nome.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%"));
        }
        if (email != null && !email.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
        }
        if (!incluirInativos) {
            predicates.add(cb.isTrue(root.get("ativo")));
        }
        
        if (isAdminStatus != null) {
            predicates.add(cb.equal(root.get("isAdmin"), isAdminStatus));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        return getManager().createQuery(cq).getResultList();
    }
    
    public Usuario findByEmail(String email) {
        try {
            return getManager().createQuery("select u from Usuario u where u.email = :email", Usuario.class)
                .setParameter("email", email)
                .getSingleResult();
        } catch (javax.persistence.NoResultException e) {
            return null; 
        }
    }
}