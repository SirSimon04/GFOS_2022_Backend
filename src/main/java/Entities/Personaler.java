/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entities;

import java.io.Serializable;
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author simon
 */
@Entity
@Table(name="PERSONALER")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name="Personaler.findAll", query="SELECT p FROM Personaler p"),
    @NamedQuery(name="Personaler.findByPersonalerid", query="SELECT p FROM Personaler p WHERE p.personalerid = :personalerid"),
    @NamedQuery(name="Personaler.findByRang", query="SELECT p FROM Personaler p WHERE p.rang = :rang"),
    @NamedQuery(name="Personaler.findByName", query="SELECT p FROM Personaler p WHERE p.name = :name"),
    @NamedQuery(name="Personaler.findByVorname", query="SELECT p FROM Personaler p WHERE p.vorname = :vorname"),
    @NamedQuery(name="Personaler.findByEmail", query="SELECT p FROM Personaler p WHERE p.email = :email"),
    @NamedQuery(name="Personaler.findByPassworthash", query="SELECT p FROM Personaler p WHERE p.passworthash = :passworthash"),
    @NamedQuery(name="Personaler.findByTelefon", query="SELECT p FROM Personaler p WHERE p.telefon = :telefon")})
public class Personaler implements Serializable{

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Basic(optional=false)
    @Column(name="PERSONALERID")
    private Integer personalerid;
    @Basic(optional=false)
    @NotNull
    @Column(name="RANG")
    private int rang;
    @Basic(optional=false)
    @NotNull
    @Size(min=1, max=64)
    @Column(name="NAME")
    private String name;
    @Basic(optional=false)
    @NotNull
    @Size(min=1, max=64)
    @Column(name="VORNAME")
    private String vorname;
    // @Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message="Invalid email")//if the field contains email address consider using this annotation to enforce field validation
    @Basic(optional=false)
    @NotNull
    @Size(min=1, max=64)
    @Column(name="EMAIL")
    private String email;
    @Size(max=256)
    @Column(name="PASSWORTHASH")
    private String passworthash;
    @Basic(optional=false)
    @NotNull
    @Size(min=1, max=64)
    @Column(name="TELEFON")
    private String telefon;
    @JoinTable(name="ARBEITETAN", joinColumns={
        @JoinColumn(name="PERSONALERID", referencedColumnName="PERSONALERID")}, inverseJoinColumns={
        @JoinColumn(name="BEWERBUNGID", referencedColumnName="BEWERBUNGID")})
    @ManyToMany
    private List<Bewerbung> bewerbungList;
    @JoinTable(name="PERSONALERTEAM", joinColumns={
        @JoinColumn(name="CHEFID", referencedColumnName="PERSONALERID")}, inverseJoinColumns={
        @JoinColumn(name="ARBEITERID", referencedColumnName="PERSONALERID")})
    @ManyToMany
    private List<Personaler> personalerList;
    @ManyToMany(mappedBy="personalerList")
    private List<Personaler> personalerList1;
    @OneToMany(mappedBy="ansprechpartner")
    private List<Jobangebot> jobangebotList;
    @JoinColumn(name="ADRESSE", referencedColumnName="ADRESSEID")
    @ManyToOne
    private Adresse adresse;
    @JoinColumn(name="FACHGEBIET", referencedColumnName="FACHGEBIETID")
    @ManyToOne
    private Fachgebiet fachgebiet;

    public Personaler(){
    }

    public Personaler(Integer personalerid){
        this.personalerid = personalerid;
    }

    public Personaler(Integer personalerid, int rang, String name, String vorname, String email, String telefon){
        this.personalerid = personalerid;
        this.rang = rang;
        this.name = name;
        this.vorname = vorname;
        this.email = email;
        this.telefon = telefon;
    }

    public Integer getPersonalerid(){
        return personalerid;
    }

    public void setPersonalerid(Integer personalerid){
        this.personalerid = personalerid;
    }

    public int getRang(){
        return rang;
    }

    public void setRang(int rang){
        this.rang = rang;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getVorname(){
        return vorname;
    }

    public void setVorname(String vorname){
        this.vorname = vorname;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getPassworthash(){
        return passworthash;
    }

    public void setPassworthash(String passworthash){
        this.passworthash = passworthash;
    }

    public String getTelefon(){
        return telefon;
    }

    public void setTelefon(String telefon){
        this.telefon = telefon;
    }

    @XmlTransient
    public List<Bewerbung> getBewerbungList(){
        return bewerbungList;
    }

    public void setBewerbungList(List<Bewerbung> bewerbungList){
        this.bewerbungList = bewerbungList;
    }

    @XmlTransient
    public List<Personaler> getPersonalerList(){
        return personalerList;
    }

    public void setPersonalerList(List<Personaler> personalerList){
        this.personalerList = personalerList;
    }

    @XmlTransient
    public List<Personaler> getPersonalerList1(){
        return personalerList1;
    }

    public void setPersonalerList1(List<Personaler> personalerList1){
        this.personalerList1 = personalerList1;
    }

    @XmlTransient
    public List<Jobangebot> getJobangebotList(){
        return jobangebotList;
    }

    public void setJobangebotList(List<Jobangebot> jobangebotList){
        this.jobangebotList = jobangebotList;
    }

    public Adresse getAdresse(){
        return adresse;
    }

    public void setAdresse(Adresse adresse){
        this.adresse = adresse;
    }

    public Fachgebiet getFachgebiet(){
        return fachgebiet;
    }

    public void setFachgebiet(Fachgebiet fachgebiet){
        this.fachgebiet = fachgebiet;
    }

    @Override
    public int hashCode(){
        int hash = 0;
        hash += (personalerid != null ? personalerid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object){
        // TODO: Warning - this method won't work in the case the id fields are not set
        if(!(object instanceof Personaler)){
            return false;
        }
        Personaler other = (Personaler) object;
        if((this.personalerid == null && other.personalerid != null) || (this.personalerid != null && !this.personalerid.equals(other.personalerid))){
            return false;
        }
        return true;
    }

    @Override
    public String toString(){
        return "Entities.Personaler[ personalerid=" + personalerid + " ]";
    }

    @Override
    public Personaler clone(){
        Personaler newPersonaler = new Personaler(this.getPersonalerid());
        newPersonaler.setAdresse(this.adresse);
        newPersonaler.setEmail(this.email);
//        if(this.fachgebiet != null){
//            newPersonaler.setFachgebiet(fachgebiet.clone());
//        }
        newPersonaler.setRang(this.rang);
        newPersonaler.setTelefon(this.telefon);
        newPersonaler.setName(this.name);
        newPersonaler.setVorname(this.vorname);
        return newPersonaler;
    }

}
