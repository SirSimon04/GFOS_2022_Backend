CREATE TABLE ADRESSE(
    ADRESSEID INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    STRAßE VARCHAR(128),
    HAUSNUMMER VARCHAR(8),
    PLZ INT,
    STADT VARCHAR(64),
    LAND VARCHAR(64)
);

CREATE TABLE FACHGEBIET(
    FACHGEBIETID INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1),
    NAME VARCHAR(64),
    VONCHEFGEPINNT BOOLEAN,
    ANZAHLJOBS INT
);

CREATE TABLE BEWERBEREINSTELLUNGEN(
    BEWERBEREINSTELLUNGENID INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    GETMAILS BOOLEAN,
    TWOFA BOOLEAN
);

CREATE TABLE BEWERBER(
    BEWERBERID INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    NAME VARCHAR(64) NOT NULL,
    VORNAME VARCHAR(64) NOT NULL,
    EMAIL VARCHAR(64) NOT NULL,
    PASSWORTHASH VARCHAR(256),
    TELEFON VARCHAR(64),
    GEBURTSTAG TIMESTAMP,
    AUTHCODE INT,
    TWOFACODE INT,
    PASSWORDRESETCODE INT,
    ADRESSE INTEGER,
    FOREIGN KEY(ADRESSE) REFERENCES ADRESSE(ADRESSEID) ON DELETE CASCADE,
    FACHGEBIET INTEGER,
    FOREIGN KEY(FACHGEBIET) REFERENCES FACHGEBIET(FACHGEBIETID) ON DELETE CASCADE,
    EINSTELLUNGEN INT,
    FOREIGN KEY(EINSTELLUNGEN) REFERENCES BEWERBEREINSTELLUNGEN(BEWERBEREINSTELLUNGENID) ON DELETE CASCADE
);

CREATE TABLE LEBENSLAUFSTATION(
    LEBENSLAUFSTATIONID INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    START TIMESTAMP NOT NULL,
    ENDE TIMESTAMP NOT NULL,
    INFO VARCHAR(512) NOT NULL,
    REFERENZ VARCHAR(1)
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
    ISCHEF BOOLEAN,
    TWOFACODE INT,
    PASSWORDRESETCODE INT
);

CREATE TABLE TODO(
    TODOID INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    TITLE VARCHAR(128),
    ORDERID INT,
    PERSONALER INTEGER,
    FOREIGN KEY(PERSONALER) REFERENCES PERSONALER(PERSONALERID)
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
    ENTFERNUNG DOUBLE,
    VONCHEFGEPINNT BOOLEAN
);

CREATE TABLE BEWERBUNG(
    BEWERBUNGID INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    BEWERBER INT,
    FOREIGN KEY(BEWERBER) REFERENCES BEWERBER(BEWERBERID),
    JOBANGEBOT INT,
    FOREIGN KEY(JOBANGEBOT) REFERENCES JOBANGEBOT(JOBANGEBOTID),
    DATUM TIMESTAMP NOT NULL,
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


INSERT INTO BEWERBER (NAME, VORNAME, EMAIL, PASSWORTHASH, TELEFON, GEBURTSTAG, AUTHCODE, ADRESSE) VALUES('MailAuth', 'MailAuth', 'innovationsaward2022@gymnasium-essen-werden.de', 'Kug30420', '', NULL, NULL, NULL);

INSERT INTO INTERESSENFELDER(NAME) VALUES ('Programmieren');
INSERT INTO INTERESSENFELDER(NAME) VALUES ('Design');
INSERT INTO INTERESSENFELDER(NAME) VALUES ('Management');

INSERT INTO FACHGEBIET(NAME, VONCHEFGEPINNT, ANZAHLJOBS) VALUES ('Softwareentwicklung', false, 0);
INSERT INTO FACHGEBIET(NAME, VONCHEFGEPINNT, ANZAHLJOBS) VALUES ('IT-Sicherheit', false, 0);
INSERT INTO FACHGEBIET(NAME, VONCHEFGEPINNT, ANZAHLJOBS) VALUES ('Marketing', false, 0);
INSERT INTO FACHGEBIET(NAME, VONCHEFGEPINNT, ANZAHLJOBS) VALUES ('Human Ressources', false, 0);
INSERT INTO FACHGEBIET(NAME, VONCHEFGEPINNT, ANZAHLJOBS) VALUES ('Buchhaltung', false, 0);
INSERT INTO FACHGEBIET(NAME, VONCHEFGEPINNT, ANZAHLJOBS) VALUES ('Kundendienst', false, 0);
INSERT INTO FACHGEBIET(NAME, VONCHEFGEPINNT, ANZAHLJOBS) VALUES ('Vertrieb', false, 0);
INSERT INTO FACHGEBIET(NAME, VONCHEFGEPINNT, ANZAHLJOBS) VALUES ('Techniker', false, 0);
INSERT INTO FACHGEBIET(NAME, VONCHEFGEPINNT, ANZAHLJOBS) VALUES ('Produktion', false, 0);
INSERT INTO FACHGEBIET(NAME, VONCHEFGEPINNT, ANZAHLJOBS) VALUES ('Einkauf', false, 0);

INSERT INTO BEWERBUNGSTYP VALUES(0, 'Teilzeit');
INSERT INTO BEWERBUNGSTYP VALUES(1, 'Vollzeit');
INSERT INTO BEWERBUNGSTYP VALUES(2, 'Praktikum');
INSERT INTO BEWERBUNGSTYP VALUES(3, 'Ausbildung');
INSERT INTO BEWERBUNGSTYP VALUES(4, 'Duales Studium');
INSERT INTO BEWERBUNGSTYP VALUES(5, 'Minijob');


INSERT INTO PERSONALER(RANG, NAME, VORNAME, EMAIL, PASSWORTHASH, TELEFON, ISCHEF) VALUES(0, 'Mustermann', 'Max', 'max.mustermann@firma.de', 'db3b64d143fb02fe1f97a008a6364366a14ecdd5d84a98551bca8b4db4a81d3bfb7fbbbfb9a18f80a1ba6d74ce6e672abe16786820c8de282e1e7e4f092b6066', '123456789', true);