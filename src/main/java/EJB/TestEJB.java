/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EJB;

import Entities.Test;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author simon
 */
@Stateless
@LocalBean
public class TestEJB{

    @PersistenceContext
    private EntityManager em;

    public List<Test> getAll(){
        return em.createNamedQuery("Test.findAll").getResultList();
    }

    public Test get(int id){
        return em.find(Test.class, id);
    }

    public void add(Test t){
        em.persist(t);
    }

    public boolean delete(int id){
        try{
            em.remove(this.get(id));
            return true;
        }catch(IllegalArgumentException e){
            return false;
        }
    }

    public boolean update(Test aktTest){
        int gesuchteId = aktTest.getId();
        try{
            Test aktuellInDatenbank = this.get(gesuchteId);
            aktuellInDatenbank.setName(aktTest.getName());
            return true;
        }catch(Exception e){
            return false;
        }
    }
}
