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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author simon
 */
@Entity
@Table(name="BLACKLIST")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name="Blacklist.findAll", query="SELECT b FROM Blacklist b"),
    @NamedQuery(name="Blacklist.findByAutoid", query="SELECT b FROM Blacklist b WHERE b.autoid = :autoid"),
    @NamedQuery(name="Blacklist.findByZeit", query="SELECT b FROM Blacklist b WHERE b.zeit = :zeit"),
    @NamedQuery(name="Blacklist.findByToken", query="SELECT b FROM Blacklist b WHERE b.token = :token")})
public class Blacklist implements Serializable{

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Basic(optional=false)
    @Column(name="AUTOID")
    private Integer autoid;
    @Column(name="ZEIT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date zeit;
    @Size(max=250)
    @Column(name="TOKEN")
    private String token;

    public Blacklist(){
    }

    public Blacklist(Integer autoid){
        this.autoid = autoid;
    }

    public Integer getAutoid(){
        return autoid;
    }

    public void setAutoid(Integer autoid){
        this.autoid = autoid;
    }

    public Date getZeit(){
        return zeit;
    }

    public void setZeit(Date zeit){
        this.zeit = zeit;
    }

    public String getToken(){
        return token;
    }

    public void setToken(String token){
        this.token = token;
    }

    @Override
    public int hashCode(){
        int hash = 0;
        hash += (autoid != null ? autoid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object){
        // TODO: Warning - this method won't work in the case the id fields are not set
        if(!(object instanceof Blacklist)){
            return false;
        }
        Blacklist other = (Blacklist) object;
        if((this.autoid == null && other.autoid != null) || (this.autoid != null && !this.autoid.equals(other.autoid))){
            return false;
        }
        return true;
    }

    @Override
    public String toString(){
        return "Entities.Blacklist[ autoid=" + autoid + " ]";
    }

}
