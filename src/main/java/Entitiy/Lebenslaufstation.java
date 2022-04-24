/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entitiy;

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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Entity
@Table(name = "LEBENSLAUFSTATION")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Lebenslaufstation.findAll", query = "SELECT l FROM Lebenslaufstation l"),
    @NamedQuery(name = "Lebenslaufstation.findByLebenslaufstationid", query = "SELECT l FROM Lebenslaufstation l WHERE l.lebenslaufstationid = :lebenslaufstationid"),
    @NamedQuery(name = "Lebenslaufstation.findByStart", query = "SELECT l FROM Lebenslaufstation l WHERE l.start = :start"),
    @NamedQuery(name = "Lebenslaufstation.findByEnde", query = "SELECT l FROM Lebenslaufstation l WHERE l.ende = :ende"),
    @NamedQuery(name = "Lebenslaufstation.findByInfo", query = "SELECT l FROM Lebenslaufstation l WHERE l.info = :info"),
    @NamedQuery(name = "Lebenslaufstation.findByReferenz", query = "SELECT l FROM Lebenslaufstation l WHERE l.referenz = :referenz")})
public class Lebenslaufstation implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "LEBENSLAUFSTATIONID")
    private Integer lebenslaufstationid;
    @Basic(optional = false)
    @NotNull
    @Column(name = "START")
    @Temporal(TemporalType.TIMESTAMP)
    private Date start;
    @Basic(optional = false)
    @NotNull
    @Column(name = "ENDE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ende;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 512)
    @Column(name = "INFO")
    private String info;
    @Size(max = 1)
    @Column(name = "REFERENZ")
    private String referenz;

    public Lebenslaufstation() {
    }

    public Lebenslaufstation(Integer lebenslaufstationid) {
        this.lebenslaufstationid = lebenslaufstationid;
    }

    public Lebenslaufstation(Integer lebenslaufstationid, Date start, Date ende, String info) {
        this.lebenslaufstationid = lebenslaufstationid;
        this.start = start;
        this.ende = ende;
        this.info = info;
    }

    public Integer getLebenslaufstationid() {
        return lebenslaufstationid;
    }

    public void setLebenslaufstationid(Integer lebenslaufstationid) {
        this.lebenslaufstationid = lebenslaufstationid;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnde() {
        return ende;
    }

    public void setEnde(Date ende) {
        this.ende = ende;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getReferenz() {
        return referenz;
    }

    public void setReferenz(String referenz) {
        this.referenz = referenz;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (lebenslaufstationid != null ? lebenslaufstationid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Lebenslaufstation)) {
            return false;
        }
        Lebenslaufstation other = (Lebenslaufstation) object;
        if ((this.lebenslaufstationid == null && other.lebenslaufstationid != null) || (this.lebenslaufstationid != null && !this.lebenslaufstationid.equals(other.lebenslaufstationid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entitiy.Lebenslaufstation[ lebenslaufstationid=" + lebenslaufstationid + " ]";
    }

}
