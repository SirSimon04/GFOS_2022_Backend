/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entitiy;

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
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Entity
@Table(name = "BEWERBUNGSNACHRICHT")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Bewerbungsnachricht.findAll", query = "SELECT b FROM Bewerbungsnachricht b"),
    @NamedQuery(name = "Bewerbungsnachricht.findByBewerbungsnachrichtid", query = "SELECT b FROM Bewerbungsnachricht b WHERE b.bewerbungsnachrichtid = :bewerbungsnachrichtid"),
    @NamedQuery(name = "Bewerbungsnachricht.findByText", query = "SELECT b FROM Bewerbungsnachricht b WHERE b.text = :text"),
    @NamedQuery(name = "Bewerbungsnachricht.findByVonbewerber", query = "SELECT b FROM Bewerbungsnachricht b WHERE b.vonbewerber = :vonbewerber"),
    @NamedQuery(name = "Bewerbungsnachricht.findByDatum", query = "SELECT b FROM Bewerbungsnachricht b WHERE b.datum = :datum")})
public class Bewerbungsnachricht implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "BEWERBUNGSNACHRICHTID")
    private Integer bewerbungsnachrichtid;
    @Size(max = 2048)
    @Column(name = "TEXT")
    private String text;
    @Column(name = "VONBEWERBER")
    private Boolean vonbewerber;
    @Column(name = "DATUM")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datum;
    @JoinColumn(name = "BEWERBUNG", referencedColumnName = "BEWERBUNGID")
    @ManyToOne
    private Bewerbung bewerbung;

    public Bewerbungsnachricht() {
    }

    public Bewerbungsnachricht(Integer bewerbungsnachrichtid) {
        this.bewerbungsnachrichtid = bewerbungsnachrichtid;
    }

    public Integer getBewerbungsnachrichtid() {
        return bewerbungsnachrichtid;
    }

    public void setBewerbungsnachrichtid(Integer bewerbungsnachrichtid) {
        this.bewerbungsnachrichtid = bewerbungsnachrichtid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean getVonbewerber() {
        return vonbewerber;
    }

    public void setVonbewerber(Boolean vonbewerber) {
        this.vonbewerber = vonbewerber;
    }

    public Date getDatum() {
        return datum;
    }

    public void setDatum(Date datum) {
        this.datum = datum;
    }

    public Bewerbung getBewerbung() {
        return bewerbung;
    }

    public void setBewerbung(Bewerbung bewerbung) {
        this.bewerbung = bewerbung;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (bewerbungsnachrichtid != null ? bewerbungsnachrichtid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Bewerbungsnachricht)) {
            return false;
        }
        Bewerbungsnachricht other = (Bewerbungsnachricht) object;
        if ((this.bewerbungsnachrichtid == null && other.bewerbungsnachrichtid != null) || (this.bewerbungsnachrichtid != null && !this.bewerbungsnachrichtid.equals(other.bewerbungsnachrichtid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entities.Bewerbungsnachricht[ bewerbungsnachrichtid=" + bewerbungsnachrichtid + " ]";
    }

    @Override
    public Bewerbungsnachricht clone() {
        Bewerbungsnachricht output = new Bewerbungsnachricht();
        output.setBewerbungsnachrichtid(bewerbungsnachrichtid);
        output.setDatum(datum);
        output.setText(text);
        output.setVonbewerber(vonbewerber);
        return output;
    }

}
