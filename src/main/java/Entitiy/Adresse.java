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
@Table(name = "ADRESSE")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Adresse.findAll", query = "SELECT a FROM Adresse a"),
    @NamedQuery(name = "Adresse.findByAdresseid", query = "SELECT a FROM Adresse a WHERE a.adresseid = :adresseid"),
    @NamedQuery(name = "Adresse.findByStrasse", query = "SELECT a FROM Adresse a WHERE a.strasse = :strasse"),
    @NamedQuery(name = "Adresse.findByHausnummer", query = "SELECT a FROM Adresse a WHERE a.hausnummer = :hausnummer"),
    @NamedQuery(name = "Adresse.findByPlz", query = "SELECT a FROM Adresse a WHERE a.plz = :plz"),
    @NamedQuery(name = "Adresse.findByStadt", query = "SELECT a FROM Adresse a WHERE a.stadt = :stadt"),
    @NamedQuery(name = "Adresse.findByLand", query = "SELECT a FROM Adresse a WHERE a.land = :land")})
public class Adresse implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ADRESSEID")
    private Integer adresseid;
    @Size(max = 128)
    @Column(name = "STRASSE")
    private String strasse;
    @Size(max = 8)
    @Column(name = "HAUSNUMMER")
    private String hausnummer;
    @Column(name = "PLZ")
    private Integer plz;
    @Size(max = 64)
    @Column(name = "STADT")
    private String stadt;
    @Size(max = 64)
    @Column(name = "LAND")
    private String land;

    public Adresse() {
    }

    public Adresse(Integer adresseid) {
        this.adresseid = adresseid;
    }

    public Integer getAdresseid() {
        return adresseid;
    }

    public void setAdresseid(Integer adresseid) {
        this.adresseid = adresseid;
    }

    public String getStrasse() {
        return strasse;
    }

    public void setStrasse(String strasse) {
        this.strasse = strasse;
    }

    public String getHausnummer() {
        return hausnummer;
    }

    public void setHausnummer(String hausnummer) {
        this.hausnummer = hausnummer;
    }

    public Integer getPlz() {
        return plz;
    }

    public void setPlz(Integer plz) {
        this.plz = plz;
    }

    public String getStadt() {
        return stadt;
    }

    public void setStadt(String stadt) {
        this.stadt = stadt;
    }

    public String getLand() {
        return land;
    }

    public void setLand(String land) {
        this.land = land;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (adresseid != null ? adresseid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Adresse)) {
            return false;
        }
        Adresse other = (Adresse) object;
        if ((this.adresseid == null && other.adresseid != null) || (this.adresseid != null && !this.adresseid.equals(other.adresseid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entitiy.Adresse[ adresseid=" + adresseid + " ]";
    }

}
