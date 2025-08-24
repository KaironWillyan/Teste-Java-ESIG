package br.com.esig.domain.repositories;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

public class GenericDAO<T> {

	@Inject
    private EntityManager manager;
	
    private Class<T> aClass;

    public GenericDAO(Class<T> entityClass) {
        this.aClass = entityClass;
    }

    public void save(T entity) {        
        manager.persist(entity);  
    }
    
    public void saveMerge(T entity) {
        manager.merge(entity);
    }


    public void remove(T entity) {
        manager.remove(manager.contains(entity) ? entity : manager.merge(entity));
    }

    public T find(Long id) {
        return manager.find(aClass, id);
    }

    public List<T> findAll() {
        return manager.createQuery("from " + aClass.getSimpleName(), aClass).getResultList();
    }
    
    protected EntityManager getManager() {
        return manager;
    }
}