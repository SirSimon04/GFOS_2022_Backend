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
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Entity
@Table(name="JOBANGEBOT")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name="Jobangebot.findAll", query="SELECT j FROM Jobangebot j"),
    @NamedQuery(name="Jobangebot.findByJobangebotid", query="SELECT j FROM Jobangebot j WHERE j.jobangebotid = :jobangebotid"),
    @NamedQuery(name="Jobangebot.findByTitle", query="SELECT j FROM Jobangebot j WHERE j.title = :title"),
    @NamedQuery(name="Jobangebot.findByKurzbeschreibung", query="SELECT j FROM Jobangebot j WHERE j.kurzbeschreibung = :kurzbeschreibung"),
    @NamedQuery(name="Jobangebot.findByLangbeschreibung", query="SELECT j FROM Jobangebot j WHERE j.langbeschreibung = :langbeschreibung"),
    @NamedQuery(name="Jobangebot.findByMonatsgehalt", query="SELECT j FROM Jobangebot j WHERE j.monatsgehalt = :monatsgehalt"),
    @NamedQuery(name="Jobangebot.findByJahresgehalt", query="SELECT j FROM Jobangebot j WHERE j.jahresgehalt = :jahresgehalt"),
    @NamedQuery(name="Jobangebot.findByUrlaubstage", query="SELECT j FROM Jobangebot j WHERE j.urlaubstage = :urlaubstage"),
    @NamedQuery(name="Jobangebot.findByVorteile", query="SELECT j FROM Jobangebot j WHERE j.vorteile = :vorteile"),
    @NamedQuery(name="Jobangebot.findByIstremote", query="SELECT j FROM Jobangebot j WHERE j.istremote = :istremote"),
    @NamedQuery(name="Jobangebot.findByEinstelldatum", query="SELECT j FROM Jobangebot j WHERE j.einstelldatum = :einstelldatum"),
    @NamedQuery(name="Jobangebot.findByBewerbungsfrist", query="SELECT j FROM Jobangebot j WHERE j.bewerbungsfrist = :bewerbungsfrist"),
    @NamedQuery(name="Jobangebot.findByStart", query="SELECT j FROM Jobangebot j WHERE j.start = :start"),
    @NamedQuery(name="Jobangebot.findByIstbefristet", query="SELECT j FROM Jobangebot j WHERE j.istbefristet = :istbefristet"),
    @NamedQuery(name="Jobangebot.findByEnde", query="SELECT j FROM Jobangebot j WHERE j.ende = :ende"),
    @NamedQuery(name="Jobangebot.findByEntfernung", query="SELECT j FROM Jobangebot j WHERE j.entfernung = :entfernung"),
    @NamedQuery(name="Jobangebot.findByVonchefgepinnt", query="SELECT j FROM Jobangebot j WHERE j.vonchefgepinnt = :vonchefgepinnt")})
public class Jobangebot implements Serializable, Comparable<Jobangebot>{

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Basic(optional=false)
    @Column(name="JOBANGEBOTID")
    private Integer jobangebotid;
    @Basic(optional=false)
    @NotNull
    @Size(min=1, max=128)
    @Column(name="TITLE")
    private String title;
    @Basic(optional=false)
    @NotNull
    @Size(min=1, max=256)
    @Column(name="KURZBESCHREIBUNG")
    private String kurzbeschreibung;
    @Basic(optional=false)
    @NotNull
    @Size(min=1, max=8192)
    @Column(name="LANGBESCHREIBUNG")
    private String langbeschreibung;
    @Basic(optional=false)
    @NotNull
    @Column(name="MONATSGEHALT")
    private int monatsgehalt;
    @Basic(optional=false)
    @NotNull
    @Column(name="JAHRESGEHALT")
    private int jahresgehalt;
    @Basic(optional=false)
    @NotNull
    @Column(name="URLAUBSTAGE")
    private int urlaubstage;
    @Size(max=2048)
    @Column(name="VORTEILE")
    private String vorteile;
    @Basic(optional=false)
    @NotNull
    @Column(name="ISTREMOTE")
    private Boolean istremote;
    @Basic(optional=false)
    @NotNull
    @Column(name="EINSTELLDATUM")
    @Temporal(TemporalType.TIMESTAMP)
    private Date einstelldatum;
    @Basic(optional=false)
    @NotNull
    @Column(name="BEWERBUNGSFRIST")
    @Temporal(TemporalType.TIMESTAMP)
    private Date bewerbungsfrist;
    @Basic(optional=false)
    @NotNull
    @Column(name="START")
    @Temporal(TemporalType.TIMESTAMP)
    private Date start;
    @Basic(optional=false)
    @NotNull
    @Column(name="ISTBEFRISTET")
    private Boolean istbefristet;
    @Column(name="ENDE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ende;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name="ENTFERNUNG")
    private Double entfernung;
    @Column(name="VONCHEFGEPINNT")
    private Boolean vonchefgepinnt;
    @JoinColumn(name="ADRESSE", referencedColumnName="ADRESSEID")
    @ManyToOne
    private Adresse adresse;
    @JoinColumn(name="BEWERBUNGSTYP", referencedColumnName="BEWERBUNGSTYPID")
    @ManyToOne
    private Bewerbungstyp bewerbungstyp;
    @JoinColumn(name="FACHGEBIET", referencedColumnName="FACHGEBIETID")
    @ManyToOne
    private Fachgebiet fachgebiet;
    @JoinColumn(name="ANSPRECHPARTNER", referencedColumnName="PERSONALERID")
    @ManyToOne
    private Personaler ansprechpartner;
    @OneToMany(mappedBy="jobangebot")
    private List<Bewerbung> bewerbungList;

    public Jobangebot(){
    }

    public Jobangebot(Integer jobangebotid){
        this.jobangebotid = jobangebotid;
    }

    public Jobangebot(Integer jobangebotid, String title, String kurzbeschreibung, String langbeschreibung, int monatsgehalt, int jahresgehalt, int urlaubstage, Boolean istremote, Date einstelldatum, Date bewerbungsfrist, Date start, Boolean istbefristet){
        this.jobangebotid = jobangebotid;
        this.title = title;
        this.kurzbeschreibung = kurzbeschreibung;
        this.langbeschreibung = langbeschreibung;
        this.monatsgehalt = monatsgehalt;
        this.jahresgehalt = jahresgehalt;
        this.urlaubstage = urlaubstage;
        this.istremote = istremote;
        this.einstelldatum = einstelldatum;
        this.bewerbungsfrist = bewerbungsfrist;
        this.start = start;
        this.istbefristet = istbefristet;
    }

    public Integer getJobangebotid(){
        return jobangebotid;
    }

    public void setJobangebotid(Integer jobangebotid){
        this.jobangebotid = jobangebotid;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getKurzbeschreibung(){
        return kurzbeschreibung;
    }

    public void setKurzbeschreibung(String kurzbeschreibung){
        this.kurzbeschreibung = kurzbeschreibung;
    }

    public String getLangbeschreibung(){
        return langbeschreibung;
    }

    public void setLangbeschreibung(String langbeschreibung){
        this.langbeschreibung = langbeschreibung;
    }

    public int getMonatsgehalt(){
        return monatsgehalt;
    }

    public void setMonatsgehalt(int monatsgehalt){
        this.monatsgehalt = monatsgehalt;
    }

    public int getJahresgehalt(){
        return jahresgehalt;
    }

    public void setJahresgehalt(int jahresgehalt){
        this.jahresgehalt = jahresgehalt;
    }

    public int getUrlaubstage(){
        return urlaubstage;
    }

    public void setUrlaubstage(int urlaubstage){
        this.urlaubstage = urlaubstage;
    }

    public String getVorteile(){
        return vorteile;
    }

    public void setVorteile(String vorteile){
        this.vorteile = vorteile;
    }

    public Boolean getIstremote(){
        return istremote;
    }

    public void setIstremote(Boolean istremote){
        this.istremote = istremote;
    }

    public Date getEinstelldatum(){
        return einstelldatum;
    }

    public void setEinstelldatum(Date einstelldatum){
        this.einstelldatum = einstelldatum;
    }

    public Date getBewerbungsfrist(){
        return bewerbungsfrist;
    }

    public void setBewerbungsfrist(Date bewerbungsfrist){
        this.bewerbungsfrist = bewerbungsfrist;
    }

    public Date getStart(){
        return start;
    }

    public void setStart(Date start){
        this.start = start;
    }

    public Boolean getIstbefristet(){
        return istbefristet;
    }

    public void setIstbefristet(Boolean istbefristet){
        this.istbefristet = istbefristet;
    }

    public Date getEnde(){
        return ende;
    }

    public void setEnde(Date ende){
        this.ende = ende;
    }

    public Double getEntfernung(){
        return entfernung;
    }

    public void setEntfernung(Double entfernung){
        this.entfernung = entfernung;
    }

    public Boolean getVonchefgepinnt(){
        return vonchefgepinnt;
    }

    public void setVonchefgepinnt(Boolean vonchefgepinnt){
        this.vonchefgepinnt = vonchefgepinnt;
    }

    public Adresse getAdresse(){
        return adresse;
    }

    public void setAdresse(Adresse adresse){
        this.adresse = adresse;
    }

    public Bewerbungstyp getBewerbungstyp(){
        return bewerbungstyp;
    }

    public void setBewerbungstyp(Bewerbungstyp bewerbungstyp){
        this.bewerbungstyp = bewerbungstyp;
    }

    public Fachgebiet getFachgebiet(){
        return fachgebiet;
    }

    public void setFachgebiet(Fachgebiet fachgebiet){
        this.fachgebiet = fachgebiet;
    }

    public Personaler getAnsprechpartner(){
        return ansprechpartner;
    }

    public void setAnsprechpartner(Personaler ansprechpartner){
        this.ansprechpartner = ansprechpartner;
    }

    @XmlTransient
    public List<Bewerbung> getBewerbungList(){
        return bewerbungList;
    }

    public void setBewerbungList(List<Bewerbung> bewerbungList){
        this.bewerbungList = bewerbungList;
    }

    @Override
    public int hashCode(){
        int hash = 0;
        hash += (jobangebotid != null ? jobangebotid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object){
        // TODO: Warning - this method won't work in the case the id fields are not set
        if(!(object instanceof Jobangebot)){
            return false;
        }
        Jobangebot other = (Jobangebot) object;
        if((this.jobangebotid == null && other.jobangebotid != null) || (this.jobangebotid != null && !this.jobangebotid.equals(other.jobangebotid))){
            return false;
        }
        return true;
    }

    @Override
    public String toString(){
        return "Entities.Jobangebot[ jobangebotid=" + jobangebotid + " ]";
    }

    @Override
    public Jobangebot clone(){
        Jobangebot output = new Jobangebot(this.jobangebotid);
        output.setTitle(title);
        output.setAdresse(adresse);
        output.setBewerbungsfrist(bewerbungsfrist);
        output.setAnsprechpartner(ansprechpartner.clone());
        output.setBewerbungstyp(bewerbungstyp.clone());
        output.setEinstelldatum(einstelldatum);
        output.setEnde(ende);
        output.setFachgebiet(fachgebiet.clone());
        output.setIstbefristet(istbefristet);
        output.setIstremote(istremote);
        output.setJahresgehalt(jahresgehalt);
        output.setKurzbeschreibung(kurzbeschreibung);
        output.setLangbeschreibung(langbeschreibung);
        output.setMonatsgehalt(monatsgehalt);
        output.setStart(start);
        output.setTitle(title);
        output.setUrlaubstage(urlaubstage);
        output.setEntfernung(entfernung);
        return output;
    }

    @Override
    public int compareTo(Jobangebot j){
        return j.getEinstelldatum().compareTo(this.getEinstelldatum());
    }

}
