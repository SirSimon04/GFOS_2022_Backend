/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entities;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author simon
 */
@Entity
@Table(name="BEWERBUNG")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name="Bewerbung.findAll", query="SELECT b FROM Bewerbung b"),
    @NamedQuery(name="Bewerbung.findByBewerbungid", query="SELECT b FROM Bewerbung b WHERE b.bewerbungid = :bewerbungid"),
    @NamedQuery(name="Bewerbung.findByDatum", query="SELECT b FROM Bewerbung b WHERE b.datum = :datum"),
    @NamedQuery(name="Bewerbung.findByBewerbungschreiben", query="SELECT b FROM Bewerbung b WHERE b.bewerbungschreiben = :bewerbungschreiben")})
public class Bewerbung implements Serializable{

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Basic(optional=false)
    @Column(name="BEWERBUNGID")
    private Integer bewerbungid;
    @Basic(optional=false)
    @NotNull
    @Column(name="DATUM")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datum;
    @Size(max=64)
    @Column(name="BEWERBUNGSCHREIBEN")
    private String bewerbungschreiben;
    @ManyToMany(mappedBy="bewerbungList")
    private List<Personaler> personalerList;
    @JoinColumn(name="BEWERBER", referencedColumnName="BEWERBERID")
    @ManyToOne
    private Bewerber bewerber;
    @JoinColumn(name="JOBANGEBOT", referencedColumnName="JOBANGEBOTID")
    @ManyToOne
    private Jobangebot jobangebot;

    public Bewerbung(){
    }

    public Bewerbung(Integer bewerbungid){
        this.bewerbungid = bewerbungid;
    }

    public Bewerbung(Integer bewerbungid, Date datum){
        this.bewerbungid = bewerbungid;
        this.datum = datum;
    }

    public Integer getBewerbungid(){
        return bewerbungid;
    }

    public void setBewerbungid(Integer bewerbungid){
        this.bewerbungid = bewerbungid;
    }

    public Date getDatum(){
        return datum;
    }

    public void setDatum(Date datum){
        this.datum = datum;
    }

    public String getBewerbungschreiben(){
        return bewerbungschreiben;
    }

    public void setBewerbungschreiben(String bewerbungschreiben){
        this.bewerbungschreiben = bewerbungschreiben;
    }

    @XmlTransient
    public List<Personaler> getPersonalerList(){
        return personalerList;
    }

    public void setPersonalerList(List<Personaler> personalerList){
        this.personalerList = personalerList;
    }

    public Bewerber getBewerber(){
        return bewerber;
    }

    public void setBewerber(Bewerber bewerber){
        this.bewerber = bewerber;
    }

    public Jobangebot getJobangebot(){
        return jobangebot;
    }

    public void setJobangebot(Jobangebot jobangebot){
        this.jobangebot = jobangebot;
    }

    @Override
    public int hashCode(){
        int hash = 0;
        hash += (bewerbungid != null ? bewerbungid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object){
        // TODO: Warning - this method won't work in the case the id fields are not set
        if(!(object instanceof Bewerbung)){
            return false;
        }
        Bewerbung other = (Bewerbung) object;
        if((this.bewerbungid == null && other.bewerbungid != null) || (this.bewerbungid != null && !this.bewerbungid.equals(other.bewerbungid))){
            return false;
        }
        return true;
    }

    @Override
    public String toString(){
        return "Entities.Bewerbung[ bewerbungid=" + bewerbungid + " ]";
    }

}
