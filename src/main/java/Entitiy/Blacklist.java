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
@Table(name = "BLACKLIST")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Blacklist.findAll", query = "SELECT b FROM Blacklist b"),
    @NamedQuery(name = "Blacklist.findById", query = "SELECT b FROM Blacklist b WHERE b.id = :id"),
    @NamedQuery(name = "Blacklist.findByZeit", query = "SELECT b FROM Blacklist b WHERE b.zeit = :zeit"),
    @NamedQuery(name = "Blacklist.findByToken", query = "SELECT b FROM Blacklist b WHERE b.token = :token")})
public class Blacklist implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Column(name = "ZEIT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date zeit;
    @Size(max = 250)
    @Column(name = "TOKEN")
    private String token;

    public Blacklist() {
    }

    public Blacklist(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getZeit() {
        return zeit;
    }

    public void setZeit(Date zeit) {
        this.zeit = zeit;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Blacklist)) {
            return false;
        }
        Blacklist other = (Blacklist) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entities.Blacklist[ id=" + id + " ]";
    }

}
