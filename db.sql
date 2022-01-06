CREATE TABLE ADRESSE(
    ADRESSEID INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    STRAßE VARCHAR(128) NOT NULL,
    HAUSNUMMER VARCHAR(8) NOT NULL,
    PLZ INT NOT NULL,
    STADT VARCHAR(64) NOT NULL,
    LAND VARCHAR(64) NOT NULL
);

CREATE TABLE FACHGEBIET(
    FACHGEBIETID INT NOT NULL PRIMARY KEY,
    NAME VARCHAR(64)
);

CREATE TABLE FOTO(
    FOTOID INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    STRING BLOB
);

CREATE TABLE DATEI(
    DATEIID INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    STRING BLOB
);

CREATE TABLE BEWERBEREINSTELLUNGEN(
    BEWERBEREINSTELLUNGENID INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    ISPUBLIC BOOLEAN,
    getMails BOOLEAN
);

CREATE TABLE BEWERBER(
    BEWERBERID INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    NAME VARCHAR(64) NOT NULL,
    VORNAME VARCHAR(64) NOT NULL,
    EMAIL VARCHAR(64) NOT NULL,
    PASSWORTHASH VARCHAR(256),
    TELEFON VARCHAR(64) NOT NULL,
    GEBURTSTAG TIMESTAMP,
    AUTHCODE INT,
    ADRESSE INTEGER,
    FOREIGN KEY(ADRESSE) REFERENCES ADRESSE(ADRESSEID) ON DELETE CASCADE,
    FACHGEBIET INTEGER,
    FOREIGN KEY(FACHGEBIET) REFERENCES FACHGEBIET(FACHGEBIETID) ON DELETE CASCADE,
    PROFILBILD INT,
    FOREIGN KEY(PROFILBILD) REFERENCES FOTO(FOTOID) ON DELETE CASCADE,
    LEBENSLAUF INT,
    FOREIGN KEY(LEBENSLAUF) REFERENCES DATEI(DATEIID) ON DELETE CASCADE,
    EINSTELLUNGEN INT,
    FOREIGN KEY(EINSTELLUNGEN) REFERENCES BEWERBEREINSTELLUNGEN(BEWERBEREINSTELLUNGENID) ON DELETE CASCADE
);

CREATE TABLE LEBENSLAUFSTATION(
    LEBENSLAUFSTATIONID INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    START TIMESTAMP NOT NULL,
    ENDE TIMESTAMP NOT NULL,
    TÄTIGKEIT VARCHAR(512) NOT NULL,
    ZEUGNIS INT,
    FOREIGN KEY(ZEUGNIS) REFERENCES DATEI(DATEIID) ON DELETE CASCADE
);

CREATE TABLE INTERESSENFELDER(
    INTERESSENFELDERID INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    NAME VARCHAR(64)
);

CREATE TABLE PERSONALER(
    PERSONALERID INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    RANG INT NOT NULL,
    NAME VARCHAR(64) NOT NULL,
    VORNAME VARCHAR(64) NOT NULL,
    EMAIL VARCHAR(64) NOT NULL,
    PASSWORTHASH VARCHAR(256),
    TELEFON VARCHAR(64) NOT NULL,
    ADRESSE INT,
    FOREIGN KEY(ADRESSE) REFERENCES ADRESSE(ADRESSEID) ON DELETE CASCADE,
    FACHGEBIET INT,
    FOREIGN KEY(FACHGEBIET) REFERENCES FACHGEBIET(FACHGEBIETID),
    ISCHEF BOOLEAN
);

CREATE TABLE BEWERBUNGSTYP(
    BEWERBUNGSTYPID INT NOT NULL PRIMARY KEY,
    ART VARCHAR(64)
);

CREATE TABLE JOBANGEBOT(
    JOBANGEBOTID INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    FACHGEBIET INT,
    FOREIGN KEY(FACHGEBIET) REFERENCES FACHGEBIET(FACHGEBIETID) ON DELETE CASCADE,
    TITLE VARCHAR(128) NOT NULL,
    KURZBESCHREIBUNG VARCHAR(256) NOT NULL,
    LANGBESCHREIBUNG VARCHAR(8192) NOT NULL,
    MONATSGEHALT INT NOT NULL,
    JAHRESGEHALT INT NOT NULL,
    URLAUBSTAGE INT NOT NULL,
    VORTEILE VARCHAR(2048),
    ISTREMOTE BOOLEAN NOT NULL,
    ADRESSE INT,
    FOREIGN KEY(ADRESSE) REFERENCES ADRESSE(ADRESSEID) ON DELETE CASCADE,
    EINSTELLDATUM TIMESTAMP NOT NULL,
    BEWERBUNGSFRIST TIMESTAMP NOT NULL,
    BEWERBUNGSTYP INT,
    FOREIGN KEY(BEWERBUNGSTYP) REFERENCES BEWERBUNGSTYP(BEWERBUNGSTYPID) ON DELETE CASCADE,
    ANSPRECHPARTNER  INT,
    FOREIGN KEY(ANSPRECHPARTNER) REFERENCES PERSONALER(PERSONALERID) ON DELETE CASCADE,
    START TIMESTAMP NOT NULL,
    ISTBEFRISTET BOOLEAN NOT NULL,   
    ENDE TIMESTAMP,
    ENTFERNUNG DOUBLE
);

CREATE TABLE BEWERBUNG(
    BEWERBUNGID INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    BEWERBER INT,
    FOREIGN KEY(BEWERBER) REFERENCES BEWERBER(BEWERBERID),
    JOBANGEBOT INT,
    FOREIGN KEY(JOBANGEBOT) REFERENCES JOBANGEBOT(JOBANGEBOTID),
    DATUM TIMESTAMP NOT NULL,
    BEWERBUNGSCHREIBEN INT,
    FOREIGN KEY(BEWERBUNGSCHREIBEN) REFERENCES DATEI(DATEIID),
    STATUS INT
);

CREATE TABLE BEWERBUNGSNACHRICHT(
    BEWERBUNGSNACHRICHTID INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    TEXT VARCHAR(2048),
    VONBEWERBER BOOLEAN,
    BEWERBUNG INT,
    FOREIGN KEY(BEWERBUNG) REFERENCES BEWERBUNG(BEWERBUNGID),
    DATUM TIMESTAMP
);

CREATE TABLE LEBENSLAUF(
    LEBENSLAUFSTATIONID INT NOT NULL,
    BEWERBERID INT NOT NULL,
    FOREIGN KEY(LEBENSLAUFSTATIONID) REFERENCES LEBENSLAUFSTATION(LEBENSLAUFSTATIONID) ON DELETE CASCADE,
    FOREIGN KEY(BEWERBERID) REFERENCES BEWERBER(BEWERBERID) ON DELETE CASCADE
);

CREATE TABLE INTERESSEN(
    INTERESSENFELDERID INT NOT NULL,
    BEWERBERID INT NOT NULL,
    FOREIGN KEY(INTERESSENFELDERID) REFERENCES INTERESSENFELDER(INTERESSENFELDERID) ON DELETE CASCADE,
    FOREIGN KEY(BEWERBERID) REFERENCES BEWERBER(BEWERBERID) ON DELETE CASCADE
);

CREATE TABLE ARBEITETAN(
    PERSONALERID INT NOT NULL,
    BEWERBUNGID INT NOT NULL,
    FOREIGN KEY(PERSONALERID) REFERENCES PERSONALER(PERSONALERID) ON DELETE CASCADE,
    FOREIGN KEY(BEWERBUNGID) REFERENCES BEWERBUNG(BEWERBUNGID) ON DELETE CASCADE
);

CREATE TABLE PERSONALERTEAM(
    CHEFID INT NOT NULL,
    ARBEITERID INT NOT NULL,
    FOREIGN KEY(CHEFID) REFERENCES PERSONALER(PERSONALERID) ON DELETE CASCADE,
    FOREIGN KEY(ARBEITERID) REFERENCES PERSONALER(PERSONALERID) ON DELETE CASCADE
);

CREATE TABLE BLACKLIST(
	ID INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY(START WITH 1, INCREMENT BY 1),
	ZEIT TIMESTAMP,
	TOKEN VARCHAR(250)
);


INSERT INTO BEWERBER (NAME, VORNAME, EMAIL, PASSWORTHASH, TELEFON, GEBURTSTAG, AUTHCODE, ADRESSE) VALUES('MailAuth', 'MailAuth', 'disputatio.messenger@gmail.com', 'myQP$15ncg&oEzg1', '', NULL, NULL, NULL);

INSERT INTO INTERESSENFELDER(NAME) VALUES ('Programmieren');
INSERT INTO INTERESSENFELDER(NAME) VALUES ('Design');
INSERT INTO INTERESSENFELDER(NAME) VALUES ('Management');

<!--Testweise ein paar FACHGEBIETE, müssen noch festgelegt werden-->
INSERT INTO FACHGEBIET VALUES (0, 'Software Entwicklung');
INSERT INTO FACHGEBIET VALUES (1, 'Marketing');
INSERT INTO FACHGEBIET VALUES (2, 'Human Resources');
INSERT INTO FACHGEBIET VALUES (3, 'IT-Sicherheit');

INSERT INTO BEWERBUNGSTYP VALUES(0, 'Teilzeit');
INSERT INTO BEWERBUNGSTYP VALUES(1, 'Vollzeit');
INSERT INTO BEWERBUNGSTYP VALUES(2, 'Praktikum');
INSERT INTO BEWERBUNGSTYP VALUES(3, 'Ausbildung');
INSERT INTO BEWERBUNGSTYP VALUES(4, 'Duales Studium');
INSERT INTO BEWERBUNGSTYP VALUES(5, 'Minijob');


INSERT INTO PERSONALER(RANG, NAME, VORNAME, EMAIL, PASSWORTHASH, TELEFON, ISCHEF) VALUES(0, 'Mustermann', 'Max', 'max.mustermann@firma.de', 'asdf', '123456789', true);