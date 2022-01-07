/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entitiy;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author simon
 */
@Entity
@Table(name="FOTO")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name="Foto.findAll", query="SELECT f FROM Foto f"),
    @NamedQuery(name="Foto.findByFotoid", query="SELECT f FROM Foto f WHERE f.fotoid = :fotoid")})
public class Foto implements Serializable{

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Basic(optional=false)
    @Column(name="FOTOID")
    private Integer fotoid;
    @Lob
    @Column(name="STRING")
    private Serializable string;

    public Foto(){
    }

    public Foto(Integer fotoid){
        this.fotoid = fotoid;
    }

    public Integer getFotoid(){
        return fotoid;
    }

    public void setFotoid(Integer fotoid){
        this.fotoid = fotoid;
    }

    public Serializable getString(){
        return string;
    }

    public void setString(Serializable string){
        this.string = string;
    }

    @Override
    public int hashCode(){
        int hash = 0;
        hash += (fotoid != null ? fotoid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object){
        // TODO: Warning - this method won't work in the case the id fields are not set
        if(!(object instanceof Foto)){
            return false;
        }
        Foto other = (Foto) object;
        if((this.fotoid == null && other.fotoid != null) || (this.fotoid != null && !this.fotoid.equals(other.fotoid))){
            return false;
        }
        return true;
    }

    @Override
    public String toString(){
        return "Entities.Foto[ fotoid=" + fotoid + " ]";
    }

}
