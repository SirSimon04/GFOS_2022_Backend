/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entities;

import java.io.Serializable;
import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author simon
 */
@Entity
@Table(name="NACHRICHT")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name="Nachricht.findAll", query="SELECT n FROM Nachricht n"),
    @NamedQuery(name="Nachricht.findByNachrichtid", query="SELECT n FROM Nachricht n WHERE n.nachrichtid = :nachrichtid"),
    @NamedQuery(name="Nachricht.findByDatum", query="SELECT n FROM Nachricht n WHERE n.datum = :datum"),
    @NamedQuery(name="Nachricht.findByVonbewerber", query="SELECT n FROM Nachricht n WHERE n.vonbewerber = :vonbewerber")})
public class Nachricht implements Serializable{

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Basic(optional=false)
    @Column(name="NACHRICHTID")
    private Integer nachrichtid;
    @Column(name="DATUM")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datum;
    @Column(name="VONBEWERBER")
    private Boolean vonbewerber;

    public Nachricht(){
    }

    public Nachricht(Integer nachrichtid){
        this.nachrichtid = nachrichtid;
    }

    public Integer getNachrichtid(){
        return nachrichtid;
    }

    public void setNachrichtid(Integer nachrichtid){
        this.nachrichtid = nachrichtid;
    }

    public Date getDatum(){
        return datum;
    }

    public void setDatum(Date datum){
        this.datum = datum;
    }

    public Boolean getVonbewerber(){
        return vonbewerber;
    }

    public void setVonbewerber(Boolean vonbewerber){
        this.vonbewerber = vonbewerber;
    }

    @Override
    public int hashCode(){
        int hash = 0;
        hash += (nachrichtid != null ? nachrichtid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object){
        // TODO: Warning - this method won't work in the case the id fields are not set
        if(!(object instanceof Nachricht)){
            return false;
        }
        Nachricht other = (Nachricht) object;
        if((this.nachrichtid == null && other.nachrichtid != null) || (this.nachrichtid != null && !this.nachrichtid.equals(other.nachrichtid))){
            return false;
        }
        return true;
    }

    @Override
    public String toString(){
        return "Entities.Nachricht[ nachrichtid=" + nachrichtid + " ]";
    }

}
