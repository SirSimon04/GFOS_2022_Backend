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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

//Folgende Fachgebiete gibt es
//Softwareentwicklung
//IT-Sicherheit
//Marketing
//Human Ressources
//Buchhaltung
//Kundendienst
//Vertrieb
//Produktion
//Techniker
//Einkauf
/**
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Entity
@Table(name = "FACHGEBIET")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Fachgebiet.findAll", query = "SELECT f FROM Fachgebiet f"),
    @NamedQuery(name = "Fachgebiet.findByFachgebietid", query = "SELECT f FROM Fachgebiet f WHERE f.fachgebietid = :fachgebietid"),
    @NamedQuery(name = "Fachgebiet.findByName", query = "SELECT f FROM Fachgebiet f WHERE f.name = :name"),
    @NamedQuery(name = "Fachgebiet.findByVonchefgepinnt", query = "SELECT f FROM Fachgebiet f WHERE f.vonchefgepinnt = :vonchefgepinnt"),
    @NamedQuery(name = "Fachgebiet.findByAnzahljobs", query = "SELECT f FROM Fachgebiet f WHERE f.anzahljobs = :anzahljobs")})
public class Fachgebiet implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "FACHGEBIETID")
    private Integer fachgebietid;
    @Size(max = 64)
    @Column(name = "NAME")
    private String name;
    @Column(name = "VONCHEFGEPINNT")
    private Boolean vonchefgepinnt;
    @Column(name = "ANZAHLJOBS")
    private Integer anzahljobs;
    @OneToMany(mappedBy = "fachgebiet")
    private List<Jobangebot> jobangebotList;
    @OneToMany(mappedBy = "fachgebiet")
    private List<Bewerber> bewerberList;
    @OneToMany(mappedBy = "fachgebiet")
    private List<Personaler> personalerList;

    public Fachgebiet() {
    }

    public Fachgebiet(String name) {
        this.name = name;
    }

    public Fachgebiet(Integer fachgebietid) {
        this.fachgebietid = fachgebietid;
    }

    public Integer getFachgebietid() {
        return fachgebietid;
    }

    public void setFachgebietid(Integer fachgebietid) {
        this.fachgebietid = fachgebietid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getVonchefgepinnt() {
        return vonchefgepinnt;
    }

    public void setVonchefgepinnt(Boolean vonchefgepinnt) {
        this.vonchefgepinnt = vonchefgepinnt;
    }

    public Integer getAnzahljobs() {
        return anzahljobs;
    }

    public void setAnzahljobs(Integer anzahljobs) {
        this.anzahljobs = anzahljobs;
    }

    @XmlTransient
    public List<Jobangebot> getJobangebotList() {
        return jobangebotList;
    }

    public void setJobangebotList(List<Jobangebot> jobangebotList) {
        this.jobangebotList = jobangebotList;
    }

    @XmlTransient
    public List<Bewerber> getBewerberList() {
        return bewerberList;
    }

    public void setBewerberList(List<Bewerber> bewerberList) {
        this.bewerberList = bewerberList;
    }

    @XmlTransient
    public List<Personaler> getPersonalerList() {
        return personalerList;
    }

    public void setPersonalerList(List<Personaler> personalerList) {
        this.personalerList = personalerList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (fachgebietid != null ? fachgebietid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Fachgebiet)) {
            return false;
        }
        Fachgebiet other = (Fachgebiet) object;
        if ((this.fachgebietid == null && other.fachgebietid != null) || (this.fachgebietid != null && !this.fachgebietid.equals(other.fachgebietid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entitiy.Fachgebiet[ fachgebietid=" + fachgebietid + " ]";
    }

    @Override
    public Fachgebiet clone() {
        Fachgebiet output = new Fachgebiet();
        output.setFachgebietid(fachgebietid);
        output.setName(name);
        output.setAnzahljobs(anzahljobs);
        return output;
    }
}
