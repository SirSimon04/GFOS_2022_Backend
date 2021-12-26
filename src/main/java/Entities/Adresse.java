/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entities;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
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
@Table(name="ADRESSE")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name="Adresse.findAll", query="SELECT a FROM Adresse a"),
    @NamedQuery(name="Adresse.findByAdresseid", query="SELECT a FROM Adresse a WHERE a.adresseid = :adresseid"),
    @NamedQuery(name="Adresse.findByStrasse", query="SELECT a FROM Adresse a WHERE a.strasse = :strasse"),
    @NamedQuery(name="Adresse.findByHausnummer", query="SELECT a FROM Adresse a WHERE a.hausnummer = :hausnummer"),
    @NamedQuery(name="Adresse.findByPlz", query="SELECT a FROM Adresse a WHERE a.plz = :plz"),
    @NamedQuery(name="Adresse.findByStadt", query="SELECT a FROM Adresse a WHERE a.stadt = :stadt"),
    @NamedQuery(name="Adresse.findByLand", query="SELECT a FROM Adresse a WHERE a.land = :land")})
public class Adresse implements Serializable{

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Basic(optional=false)
    @Column(name="ADRESSEID")
    private Integer adresseid;
    @Basic(optional=false)
    @NotNull
    @Size(min=1, max=128)
    @Column(name="STRASSE")
    private String strasse;
    @Basic(optional=false)
    @NotNull
    @Size(min=1, max=8)
    @Column(name="HAUSNUMMER")
    private String hausnummer;
    @Basic(optional=false)
    @NotNull
    @Column(name="PLZ")
    private int plz;
    @Basic(optional=false)
    @NotNull
    @Size(min=1, max=64)
    @Column(name="STADT")
    private String stadt;
    @Basic(optional=false)
    @NotNull
    @Size(min=1, max=64)
    @Column(name="LAND")
    private String land;
    @OneToMany(mappedBy="adresse")
    private List<Jobangebot> jobangebotList;
    @OneToMany(mappedBy="adresse")
    private List<Bewerber> bewerberList;
    @OneToMany(cascade=CascadeType.ALL, mappedBy="adresse")
    private List<Personaler> personalerList;

    public Adresse(){
    }

    public Adresse(Integer adresseid){
        this.adresseid = adresseid;
    }

    public Adresse(Integer adresseid, String strasse, String hausnummer, int plz, String stadt, String land){
        this.adresseid = adresseid;
        this.strasse = strasse;
        this.hausnummer = hausnummer;
        this.plz = plz;
        this.stadt = stadt;
        this.land = land;
    }

    public Integer getAdresseid(){
        return adresseid;
    }

    public void setAdresseid(Integer adresseid){
        this.adresseid = adresseid;
    }

    public String getStrasse(){
        return strasse;
    }

    public void setStrasse(String strasse){
        this.strasse = strasse;
    }

    public String getHausnummer(){
        return hausnummer;
    }

    public void setHausnummer(String hausnummer){
        this.hausnummer = hausnummer;
    }

    public int getPlz(){
        return plz;
    }

    public void setPlz(int plz){
        this.plz = plz;
    }

    public String getStadt(){
        return stadt;
    }

    public void setStadt(String stadt){
        this.stadt = stadt;
    }

    public String getLand(){
        return land;
    }

    public void setLand(String land){
        this.land = land;
    }

    @XmlTransient
    public List<Jobangebot> getJobangebotList(){
        return jobangebotList;
    }

    public void setJobangebotList(List<Jobangebot> jobangebotList){
        this.jobangebotList = jobangebotList;
    }

    @XmlTransient
    public List<Bewerber> getBewerberList(){
        return bewerberList;
    }

    public void setBewerberList(List<Bewerber> bewerberList){
        this.bewerberList = bewerberList;
    }

    @XmlTransient
    public List<Personaler> getPersonalerList(){
        return personalerList;
    }

    public void setPersonalerList(List<Personaler> personalerList){
        this.personalerList = personalerList;
    }

    @Override
    public int hashCode(){
        int hash = 0;
        hash += (adresseid != null ? adresseid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object){
        // TODO: Warning - this method won't work in the case the id fields are not set
        if(!(object instanceof Adresse)){
            return false;
        }
        Adresse other = (Adresse) object;
        if((this.adresseid == null && other.adresseid != null) || (this.adresseid != null && !this.adresseid.equals(other.adresseid))){
            return false;
        }
        return true;
    }

    @Override
    public String toString(){
        return "Entities.Adresse[ adresseid=" + adresseid + " ]";
    }

}
