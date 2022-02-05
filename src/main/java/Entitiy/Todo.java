/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entitiy;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Entity
@Table(name="TODO")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name="Todo.findAll", query="SELECT t FROM Todo t"),
    @NamedQuery(name="Todo.findByTodoid", query="SELECT t FROM Todo t WHERE t.todoid = :todoid"),
    @NamedQuery(name="Todo.findByTitle", query="SELECT t FROM Todo t WHERE t.title = :title"),
    @NamedQuery(name="Todo.findByOrderid", query="SELECT t FROM Todo t WHERE t.orderid = :orderid")})
public class Todo implements Serializable{

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Basic(optional=false)
    @Column(name="TODOID")
    private Integer todoid;
    @Size(max=128)
    @Column(name="TITLE")
    private String title;
    @Column(name="ORDERID")
    private Integer orderid;
    @JoinColumn(name="PERSONALER", referencedColumnName="PERSONALERID")
    @ManyToOne
    private Personaler personaler;

    public Todo(){
    }

    public Todo(Integer todoid){
        this.todoid = todoid;
    }

    public Integer getTodoid(){
        return todoid;
    }

    public void setTodoid(Integer todoid){
        this.todoid = todoid;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public Integer getOrderid(){
        return orderid;
    }

    public void setOrderid(Integer orderid){
        this.orderid = orderid;
    }

    public Personaler getPersonaler(){
        return personaler;
    }

    public void setPersonaler(Personaler personaler){
        this.personaler = personaler;
    }

    @Override
    public int hashCode(){
        int hash = 0;
        hash += (todoid != null ? todoid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object){
        // TODO: Warning - this method won't work in the case the id fields are not set
        if(!(object instanceof Todo)){
            return false;
        }
        Todo other = (Todo) object;
        if((this.todoid == null && other.todoid != null) || (this.todoid != null && !this.todoid.equals(other.todoid))){
            return false;
        }
        return true;
    }

    @Override
    public String toString(){
        return "Entitiy.Todo[ todoid=" + todoid + " ]";
    }

    @Override
    public Todo clone(){
        Todo newTodo = new Todo(this.todoid);
        newTodo.setOrderid(orderid);
        newTodo.setTitle(title);
        return newTodo;
    }

}
