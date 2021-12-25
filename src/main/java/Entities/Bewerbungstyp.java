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
@Table(name="BEWERBUNGSTYP")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name="Bewerbungstyp.findAll", query="SELECT b FROM Bewerbungstyp b"),
    @NamedQuery(name="Bewerbungstyp.findByBewerbungstypid", query="SELECT b FROM Bewerbungstyp b WHERE b.bewerbungstypid = :bewerbungstypid"),
    @NamedQuery(name="Bewerbungstyp.findByArt", query="SELECT b FROM Bewerbungstyp b WHERE b.art = :art")})
public class Bewerbungstyp implements Serializable{

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional=false)
    @NotNull
    @Column(name="BEWERBUNGSTYPID")
    private Integer bewerbungstypid;
    @Size(max=64)
    @Column(name="ART")
    private String art;
    @OneToMany(mappedBy="bewerbungstyp")
    private List<Jobangebot> jobangebotList;

    public Bewerbungstyp(){
    }

    public Bewerbungstyp(Integer bewerbungstypid){
        this.bewerbungstypid = bewerbungstypid;
    }

    public Integer getBewerbungstypid(){
        return bewerbungstypid;
    }

    public void setBewerbungstypid(Integer bewerbungstypid){
        this.bewerbungstypid = bewerbungstypid;
    }

    public String getArt(){
        return art;
    }

    public void setArt(String art){
        this.art = art;
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
        hash += (bewerbungstypid != null ? bewerbungstypid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object){
        // TODO: Warning - this method won't work in the case the id fields are not set
        if(!(object instanceof Bewerbungstyp)){
            return false;
        }
        Bewerbungstyp other = (Bewerbungstyp) object;
        if((this.bewerbungstypid == null && other.bewerbungstypid != null) || (this.bewerbungstypid != null && !this.bewerbungstypid.equals(other.bewerbungstypid))){
            return false;
        }
        return true;
    }

    @Override
    public String toString(){
        return "Entities.Bewerbungstyp[ bewerbungstypid=" + bewerbungstypid + " ]";
    }

}
