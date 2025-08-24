package br.com.esig.infra.producers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
//import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

@ApplicationScoped
public class EntityManagerProducer {

//    private EntityManagerFactory factory;
//
//    public EntityManagerProducer() {
//        this.factory = Persistence.createEntityManagerFactory("esig-teste-jpa");
//    }

    @Produces
    @RequestScoped
    public EntityManager createEntityManager() {
        return Persistence.createEntityManagerFactory("esig-teste-jpa").createEntityManager();
    }

    public void closeEntityManager(@Disposes EntityManager manager) {
        if (manager.isOpen()) {
            manager.close();
        }
    }
}