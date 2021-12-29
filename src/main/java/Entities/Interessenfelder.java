/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author simon
 */
@Entity
@Table(name="INTERESSENFELDER")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name="Interessenfelder.findAll", query="SELECT i FROM Interessenfelder i"),
    @NamedQuery(name="Interessenfelder.findByInteressenfelderid", query="SELECT i FROM Interessenfelder i WHERE i.interessenfelderid = :interessenfelderid"),
    @NamedQuery(name="Interessenfelder.findByName", query="SELECT i FROM Interessenfelder i WHERE i.name = :name")})
public class Interessenfelder implements Serializable{

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Basic(optional=false)
    @Column(name="INTERESSENFELDERID")
    private Integer interessenfelderid;
    @Size(max=64)
    @Column(name="NAME")
    private String name;

    public Interessenfelder(){
    }

    public Interessenfelder(Integer interessenfelderid){
        this.interessenfelderid = interessenfelderid;
    }

    public Integer getInteressenfelderid(){
        return interessenfelderid;
    }

    public void setInteressenfelderid(Integer interessenfelderid){
        this.interessenfelderid = interessenfelderid;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    @Override
    public int hashCode(){
        int hash = 0;
        hash += (interessenfelderid != null ? interessenfelderid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object){
        // TODO: Warning - this method won't work in the case the id fields are not set
        if(!(object instanceof Interessenfelder)){
            return false;
        }
        Interessenfelder other = (Interessenfelder) object;
        if((this.interessenfelderid == null && other.interessenfelderid != null) || (this.interessenfelderid != null && !this.interessenfelderid.equals(other.interessenfelderid))){
            return false;
        }
        return true;
    }

    @Override
    public String toString(){
        return "Entities.Interessenfelder[ interessenfelderid=" + interessenfelderid + " ]";
    }

}
