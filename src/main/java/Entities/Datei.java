/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entities;

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
@Table(name="DATEI")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name="Datei.findAll", query="SELECT d FROM Datei d"),
    @NamedQuery(name="Datei.findByDateiid", query="SELECT d FROM Datei d WHERE d.dateiid = :dateiid")})
public class Datei implements Serializable{

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Basic(optional=false)
    @Column(name="DATEIID")
    private Integer dateiid;
    @Lob
    @Column(name="STRING")
    private Serializable string;

    public Datei(){
    }

    public Datei(Integer dateiid){
        this.dateiid = dateiid;
    }

    public Integer getDateiid(){
        return dateiid;
    }

    public void setDateiid(Integer dateiid){
        this.dateiid = dateiid;
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
        hash += (dateiid != null ? dateiid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object){
        // TODO: Warning - this method won't work in the case the id fields are not set
        if(!(object instanceof Datei)){
            return false;
        }
        Datei other = (Datei) object;
        if((this.dateiid == null && other.dateiid != null) || (this.dateiid != null && !this.dateiid.equals(other.dateiid))){
            return false;
        }
        return true;
    }

    @Override
    public String toString(){
        return "Entities.Datei[ dateiid=" + dateiid + " ]";
    }

}
