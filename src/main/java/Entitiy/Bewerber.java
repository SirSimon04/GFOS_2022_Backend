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
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
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
@Table(name = "BEWERBER")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Bewerber.findAll", query = "SELECT b FROM Bewerber b"),
    @NamedQuery(name = "Bewerber.findByBewerberid", query = "SELECT b FROM Bewerber b WHERE b.bewerberid = :bewerberid"),
    @NamedQuery(name = "Bewerber.findByName", query = "SELECT b FROM Bewerber b WHERE b.name = :name"),
    @NamedQuery(name = "Bewerber.findByVorname", query = "SELECT b FROM Bewerber b WHERE b.vorname = :vorname"),
    @NamedQuery(name = "Bewerber.findByEmail", query = "SELECT b FROM Bewerber b WHERE b.email = :email"),
    @NamedQuery(name = "Bewerber.findByPassworthash", query = "SELECT b FROM Bewerber b WHERE b.passworthash = :passworthash"),
    @NamedQuery(name = "Bewerber.findByTelefon", query = "SELECT b FROM Bewerber b WHERE b.telefon = :telefon"),
    @NamedQuery(name = "Bewerber.findByGeburtstag", query = "SELECT b FROM Bewerber b WHERE b.geburtstag = :geburtstag"),
    @NamedQuery(name = "Bewerber.findByAuthcode", query = "SELECT b FROM Bewerber b WHERE b.authcode = :authcode")})
public class Bewerber implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "BEWERBERID")
    private Integer bewerberid;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 64)
    @Column(name = "NAME")
    private String name;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 64)
    @Column(name = "VORNAME")
    private String vorname;
    // @Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message="Invalid email")//if the field contains email address consider using this annotation to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 64)
    @Column(name = "EMAIL")
    private String email;
    @Size(max = 256)
    @Column(name = "PASSWORTHASH")
    private String passworthash;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 64)
    @Column(name = "TELEFON")
    private String telefon;
    @Column(name = "GEBURTSTAG")
    @Temporal(TemporalType.TIMESTAMP)
    private Date geburtstag;
    @Column(name = "AUTHCODE")
    private Integer authcode;
    @JoinTable(name = "LEBENSLAUF", joinColumns = {
        @JoinColumn(name = "BEWERBERID", referencedColumnName = "BEWERBERID")}, inverseJoinColumns = {
        @JoinColumn(name = "LEBENSLAUFSTATIONID", referencedColumnName = "LEBENSLAUFSTATIONID")})
    @ManyToMany
    private List<Lebenslaufstation> lebenslaufstationList;
    @JoinTable(name = "INTERESSEN", joinColumns = {
        @JoinColumn(name = "BEWERBERID", referencedColumnName = "BEWERBERID")}, inverseJoinColumns = {
        @JoinColumn(name = "INTERESSENFELDERID", referencedColumnName = "INTERESSENFELDERID")})
    @ManyToMany
    private List<Interessenfelder> interessenfelderList;
    @JoinColumn(name = "ADRESSE", referencedColumnName = "ADRESSEID")
    @ManyToOne
    private Adresse adresse;
    @JoinColumn(name = "EINSTELLUNGEN", referencedColumnName = "BEWERBEREINSTELLUNGENID")
    @ManyToOne
    private Bewerbereinstellungen einstellungen;
    @JoinColumn(name = "FACHGEBIET", referencedColumnName = "FACHGEBIETID")
    @ManyToOne
    private Fachgebiet fachgebiet;
    @OneToMany(mappedBy = "bewerber")
    private List<Bewerbung> bewerbungList;

    public Bewerber() {
    }

    public Bewerber(Integer bewerberid) {
        this.bewerberid = bewerberid;
    }

    public Bewerber(Integer bewerberid, String name, String vorname, String email, String telefon) {
        this.bewerberid = bewerberid;
        this.name = name;
        this.vorname = vorname;
        this.email = email;
        this.telefon = telefon;
    }

    public Integer getBewerberid() {
        return bewerberid;
    }

    public void setBewerberid(Integer bewerberid) {
        this.bewerberid = bewerberid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVorname() {
        return vorname;
    }

    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassworthash() {
        return passworthash;
    }

    public void setPassworthash(String passworthash) {
        this.passworthash = passworthash;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public Date getGeburtstag() {
        return geburtstag;
    }

    public void setGeburtstag(Date geburtstag) {
        this.geburtstag = geburtstag;
    }

    public Integer getAuthcode() {
        return authcode;
    }

    public void setAuthcode(Integer authcode) {
        this.authcode = authcode;
    }

    @XmlTransient
    public List<Lebenslaufstation> getLebenslaufstationList() {
        return lebenslaufstationList;
    }

    public void setLebenslaufstationList(List<Lebenslaufstation> lebenslaufstationList) {
        this.lebenslaufstationList = lebenslaufstationList;
    }

    @XmlTransient
    public List<Interessenfelder> getInteressenfelderList() {
        return interessenfelderList;
    }

    public void setInteressenfelderList(List<Interessenfelder> interessenfelderList) {
        this.interessenfelderList = interessenfelderList;
    }

    public Adresse getAdresse() {
        return adresse;
    }

    public void setAdresse(Adresse adresse) {
        this.adresse = adresse;
    }

    public Bewerbereinstellungen getEinstellungen() {
        return einstellungen;
    }

    public void setEinstellungen(Bewerbereinstellungen einstellungen) {
        this.einstellungen = einstellungen;
    }

    public Fachgebiet getFachgebiet() {
        return fachgebiet;
    }

    public void setFachgebiet(Fachgebiet fachgebiet) {
        this.fachgebiet = fachgebiet;
    }

    @XmlTransient
    public List<Bewerbung> getBewerbungList() {
        return bewerbungList;
    }

    public void setBewerbungList(List<Bewerbung> bewerbungList) {
        this.bewerbungList = bewerbungList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (bewerberid != null ? bewerberid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Bewerber)) {
            return false;
        }
        Bewerber other = (Bewerber) object;
        if ((this.bewerberid == null && other.bewerberid != null) || (this.bewerberid != null && !this.bewerberid.equals(other.bewerberid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entitiy.Bewerber[ bewerberid=" + bewerberid + " ]";
    }

    @Override
    public Bewerber clone() {
        Bewerber output = new Bewerber(bewerberid);
        output.setEmail(email);
        output.setFachgebiet(fachgebiet.clone());
        output.setGeburtstag(geburtstag);
        output.setName(name);
        output.setTelefon(telefon);
        output.setVorname(vorname);
        return output;
    }

}
