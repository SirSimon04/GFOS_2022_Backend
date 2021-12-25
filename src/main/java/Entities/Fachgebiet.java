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
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author simon
 */
@Entity
@Table(name="FACHGEBIET")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name="Fachgebiet.findAll", query="SELECT f FROM Fachgebiet f"),
    @NamedQuery(name="Fachgebiet.findByFachgebietid", query="SELECT f FROM Fachgebiet f WHERE f.fachgebietid = :fachgebietid"),
    @NamedQuery(name="Fachgebiet.findByName", query="SELECT f FROM Fachgebiet f WHERE f.name = :name")})
public class Fachgebiet implements Serializable{

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional=false)
    @NotNull
    @Column(name="FACHGEBIETID")
    private Integer fachgebietid;
    @Size(max=64)
    @Column(name="NAME")
    private String name;
    @OneToMany(mappedBy="fachgebiet")
    private List<Jobangebot> jobangebotList;

    public Fachgebiet(){
    }

    public Fachgebiet(Integer fachgebietid){
        this.fachgebietid = fachgebietid;
    }

    public Integer getFachgebietid(){
        return fachgebietid;
    }

    public void setFachgebietid(Integer fachgebietid){
        this.fachgebietid = fachgebietid;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    @XmlTransient
    public List<Jobangebot> getJobangebotList(){
        return jobangebotList;
    }

    public void setJobangebotList(List<Jobangebot> jobangebotList){
        this.jobangebotList = jobangebotList;
    }

    @Override
    public int hashCode(){
        int hash = 0;
        hash += (fachgebietid != null ? fachgebietid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object){
        // TODO: Warning - this method won't work in the case the id fields are not set
        if(!(object instanceof Fachgebiet)){
            return false;
        }
        Fachgebiet other = (Fachgebiet) object;
        if((this.fachgebietid == null && other.fachgebietid != null) || (this.fachgebietid != null && !this.fachgebietid.equals(other.fachgebietid))){
            return false;
        }
        return true;
    }

    @Override
    public String toString(){
        return "Entities.Fachgebiet[ fachgebietid=" + fachgebietid + " ]";
    }

}
