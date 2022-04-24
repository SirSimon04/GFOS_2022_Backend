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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Entity
@Table(name = "BEWERBEREINSTELLUNGEN")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Bewerbereinstellungen.findAll", query = "SELECT b FROM Bewerbereinstellungen b"),
    @NamedQuery(name = "Bewerbereinstellungen.findByBewerbereinstellungenid", query = "SELECT b FROM Bewerbereinstellungen b WHERE b.bewerbereinstellungenid = :bewerbereinstellungenid"),
    @NamedQuery(name = "Bewerbereinstellungen.findByGetmails", query = "SELECT b FROM Bewerbereinstellungen b WHERE b.getmails = :getmails"),
    @NamedQuery(name = "Bewerbereinstellungen.findByTwofa", query = "SELECT b FROM Bewerbereinstellungen b WHERE b.twofa = :twofa")})
public class Bewerbereinstellungen implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "BEWERBEREINSTELLUNGENID")
    private Integer bewerbereinstellungenid;
    @Column(name = "GETMAILS")
    private Boolean getmails;
    @Column(name = "TWOFA")
    private Boolean twofa;

    public Bewerbereinstellungen() {
    }

    public Bewerbereinstellungen(boolean getmails, boolean twofa) {
        this.getmails = getmails;
        this.twofa = twofa;
    }

    public Bewerbereinstellungen(Integer bewerbereinstellungenid) {
        this.bewerbereinstellungenid = bewerbereinstellungenid;
    }

    public Integer getBewerbereinstellungenid() {
        return bewerbereinstellungenid;
    }

    public void setBewerbereinstellungenid(Integer bewerbereinstellungenid) {
        this.bewerbereinstellungenid = bewerbereinstellungenid;
    }

    public Boolean getGetmails() {
        return getmails;
    }

    public void setGetmails(Boolean getmails) {
        this.getmails = getmails;
    }

    public Boolean getTwofa() {
        return twofa;
    }

    public void setTwofa(Boolean twofa) {
        this.twofa = twofa;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (bewerbereinstellungenid != null ? bewerbereinstellungenid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Bewerbereinstellungen)) {
            return false;
        }
        Bewerbereinstellungen other = (Bewerbereinstellungen) object;
        if ((this.bewerbereinstellungenid == null && other.bewerbereinstellungenid != null) || (this.bewerbereinstellungenid != null && !this.bewerbereinstellungenid.equals(other.bewerbereinstellungenid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entitiy.Bewerbereinstellungen[ bewerbereinstellungenid=" + bewerbereinstellungenid + " ]";
    }

}
