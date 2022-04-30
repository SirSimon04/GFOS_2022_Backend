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
    TWOFA INT,
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


--INSERT INTO BEWERBER (NAME, VORNAME, EMAIL, PASSWORTHASH, TELEFON, GEBURTSTAG, AUTHCODE, ADRESSE) VALUES('MailAuth', 'MailAuth', 'innovationsaward2022@gymnasium-essen-werden.de', 'Kug30420', '', NULL, NULL, NULL);

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


--INSERT INTO PERSONALER(RANG, NAME, VORNAME, EMAIL, PASSWORTHASH, TELEFON, ISCHEF) VALUES(0, 'Mustermann', 'Max', 'simon.engel@engelnetz.de', 'db3b64d143fb02fe1f97a008a6364366a14ecdd5d84a98551bca8b4db4a81d3bfb7fbbbfb9a18f80a1ba6d74ce6e672abe16786820c8de282e1e7e4f092b6066', '123456789', true);
INSERT INTO APP.ADRESSE (STRASSE, HAUSNUMMER, PLZ, STADT, LAND) 
	VALUES ('Am Lichtbogen', '9', 45151, 'Essen', 'Deutschland');
INSERT INTO APP.ADRESSE (STRASSE, HAUSNUMMER, PLZ, STADT, LAND) 
	VALUES ('Am Lichtbogen', '9', 45151, 'Essen', 'Deutschland');
INSERT INTO APP.ADRESSE (STRASSE, HAUSNUMMER, PLZ, STADT, LAND) 
	VALUES ('Am Lichtbogen', '9', 45151, 'Essen', 'Deutschland');
INSERT INTO APP.ADRESSE (STRASSE, HAUSNUMMER, PLZ, STADT, LAND) 
	VALUES ('Musterstraße', '1', 1111, 'Musterstadt', 'Musterland');
INSERT INTO APP.ADRESSE (STRASSE, HAUSNUMMER, PLZ, STADT, LAND) 
	VALUES ('Am Lichtbogen', '9', 45151, 'Essen', 'Deutschland');
INSERT INTO APP.ADRESSE (STRASSE, HAUSNUMMER, PLZ, STADT, LAND) 
	VALUES ('Am Lichtbogen', '9', 45151, 'Essen', 'Deutschland');
INSERT INTO APP.ADRESSE (STRASSE, HAUSNUMMER, PLZ, STADT, LAND) 
	VALUES ('Am Lichtbogen', '9', 45151, 'Essen', 'Deutschland');

INSERT INTO APP.BEWERBEREINSTELLUNGEN (GETMAILS, TWOFA) 
	VALUES (true, false);
INSERT INTO APP.BEWERBEREINSTELLUNGEN (GETMAILS, TWOFA) 
	VALUES (true, false);
INSERT INTO APP.BEWERBEREINSTELLUNGEN (GETMAILS, TWOFA) 
	VALUES (true, false);
INSERT INTO APP.BEWERBEREINSTELLUNGEN (GETMAILS, TWOFA) 
	VALUES (true, false);


INSERT INTO APP.PERSONALER (RANG, "NAME", VORNAME, EMAIL, PASSWORTHASH, TELEFON, ADRESSE, FACHGEBIET, ISCHEF, TWOFA, PASSWORDRESETCODE) 
	VALUES (0, 'Mustermann', 'Max', 'simon.engel@engelnetz.de', '1a327dde57b56142141cf584ab74d4659b50bd899fe9994a14a57277b53ad95bbee8ebb96b7bf33318dc4bfa1707ce1ef829fa5773ce18f5b05a181bc28454d8', '123456789', NULL, NULL, true, NULL, NULL);
INSERT INTO APP.PERSONALER (RANG, "NAME", VORNAME, EMAIL, PASSWORTHASH, TELEFON, ADRESSE, FACHGEBIET, ISCHEF, TWOFA, PASSWORDRESETCODE) 
	VALUES (1, 'Chef', 'Marketing', 'marketing_chef@gfos.com', '1a327dde57b56142141cf584ab74d4659b50bd899fe9994a14a57277b53ad95bbee8ebb96b7bf33318dc4bfa1707ce1ef829fa5773ce18f5b05a181bc28454d8', '123456789', NULL, 2, true, NULL, NULL);
INSERT INTO APP.PERSONALER (RANG, "NAME", VORNAME, EMAIL, PASSWORTHASH, TELEFON, ADRESSE, FACHGEBIET, ISCHEF, TWOFA, PASSWORDRESETCODE) 
	VALUES (1, '1', 'Marketing', 'marketing_1@gfos.com', '1a327dde57b56142141cf584ab74d4659b50bd899fe9994a14a57277b53ad95bbee8ebb96b7bf33318dc4bfa1707ce1ef829fa5773ce18f5b05a181bc28454d8', '123456789', NULL, 2, false, NULL, NULL);
INSERT INTO APP.PERSONALER (RANG, "NAME", VORNAME, EMAIL, PASSWORTHASH, TELEFON, ADRESSE, FACHGEBIET, ISCHEF, TWOFA, PASSWORDRESETCODE) 
	VALUES (1, '2', 'Marketing', 'marketing_2@gfos.com', '1a327dde57b56142141cf584ab74d4659b50bd899fe9994a14a57277b53ad95bbee8ebb96b7bf33318dc4bfa1707ce1ef829fa5773ce18f5b05a181bc28454d8', '123456789', NULL, 2, false, NULL, NULL);
INSERT INTO APP.PERSONALER (RANG, "NAME", VORNAME, EMAIL, PASSWORTHASH, TELEFON, ADRESSE, FACHGEBIET, ISCHEF, TWOFA, PASSWORDRESETCODE) 
	VALUES (1, '3', 'Marketing', 'marketing_3@gfos.com', '1a327dde57b56142141cf584ab74d4659b50bd899fe9994a14a57277b53ad95bbee8ebb96b7bf33318dc4bfa1707ce1ef829fa5773ce18f5b05a181bc28454d8', '123456789', NULL, 2, false, NULL, NULL);
INSERT INTO APP.PERSONALER (RANG, "NAME", VORNAME, EMAIL, PASSWORTHASH, TELEFON, ADRESSE, FACHGEBIET, ISCHEF, TWOFA, PASSWORDRESETCODE) 
	VALUES (1, 'Chef', 'IT-Security', 'it_sicherheit_chef@gfos.com', '1a327dde57b56142141cf584ab74d4659b50bd899fe9994a14a57277b53ad95bbee8ebb96b7bf33318dc4bfa1707ce1ef829fa5773ce18f5b05a181bc28454d8', '123456789', NULL, 1, true, NULL, NULL);
INSERT INTO APP.PERSONALER (RANG, "NAME", VORNAME, EMAIL, PASSWORTHASH, TELEFON, ADRESSE, FACHGEBIET, ISCHEF, TWOFA, PASSWORDRESETCODE) 
	VALUES (1, '4', 'IT-Security', 'it_sicherheit_4@gfos.com', '1a327dde57b56142141cf584ab74d4659b50bd899fe9994a14a57277b53ad95bbee8ebb96b7bf33318dc4bfa1707ce1ef829fa5773ce18f5b05a181bc28454d8', '123456789', NULL, 1, false, NULL, NULL);
INSERT INTO APP.PERSONALER (RANG, "NAME", VORNAME, EMAIL, PASSWORTHASH, TELEFON, ADRESSE, FACHGEBIET, ISCHEF, TWOFA, PASSWORDRESETCODE) 
	VALUES (1, '5', 'IT-Security', 'it_sicherheit_5@gfos.com', '1a327dde57b56142141cf584ab74d4659b50bd899fe9994a14a57277b53ad95bbee8ebb96b7bf33318dc4bfa1707ce1ef829fa5773ce18f5b05a181bc28454d8', '123456789', NULL, 1, false, NULL, NULL);
INSERT INTO APP.PERSONALER (RANG, "NAME", VORNAME, EMAIL, PASSWORTHASH, TELEFON, ADRESSE, FACHGEBIET, ISCHEF, TWOFA, PASSWORDRESETCODE) 
	VALUES (1, '6', 'IT-Security', 'it_sicherheit_6@gfos.com', '1a327dde57b56142141cf584ab74d4659b50bd899fe9994a14a57277b53ad95bbee8ebb96b7bf33318dc4bfa1707ce1ef829fa5773ce18f5b05a181bc28454d8', '123456789', NULL, 1, false, NULL, NULL);
INSERT INTO APP.PERSONALER (RANG, "NAME", VORNAME, EMAIL, PASSWORTHASH, TELEFON, ADRESSE, FACHGEBIET, ISCHEF, TWOFA, PASSWORDRESETCODE) 
	VALUES (2, 'Chef 2', 'IT-Security', 'it_sicherheit_chef_2@gfos.com', '1a327dde57b56142141cf584ab74d4659b50bd899fe9994a14a57277b53ad95bbee8ebb96b7bf33318dc4bfa1707ce1ef829fa5773ce18f5b05a181bc28454d8', '123456789', NULL, 1, true, NULL, NULL);
INSERT INTO APP.PERSONALER (RANG, "NAME", VORNAME, EMAIL, PASSWORTHASH, TELEFON, ADRESSE, FACHGEBIET, ISCHEF, TWOFA, PASSWORDRESETCODE) 
	VALUES (2, '7', 'IT-Security', 'it_sicherheit_7@gfos.com', '1a327dde57b56142141cf584ab74d4659b50bd899fe9994a14a57277b53ad95bbee8ebb96b7bf33318dc4bfa1707ce1ef829fa5773ce18f5b05a181bc28454d8', '123456789', NULL, 1, false, NULL, NULL);
INSERT INTO APP.PERSONALER (RANG, "NAME", VORNAME, EMAIL, PASSWORTHASH, TELEFON, ADRESSE, FACHGEBIET, ISCHEF, TWOFA, PASSWORDRESETCODE) 
	VALUES (2, '8', 'IT-Security', 'it_sicherheit_8@gfos.com', '1a327dde57b56142141cf584ab74d4659b50bd899fe9994a14a57277b53ad95bbee8ebb96b7bf33318dc4bfa1707ce1ef829fa5773ce18f5b05a181bc28454d8', '123456789', NULL, 1, false, NULL, NULL);

INSERT INTO APP.BEWERBER ("NAME", VORNAME, EMAIL, PASSWORTHASH, TELEFON, GEBURTSTAG, AUTHCODE, TWOFACODE, PASSWORDRESETCODE, ADRESSE, FACHGEBIET, EINSTELLUNGEN) 
	VALUES ('MailAuth', 'MailAuth', 'innovationsaward2022@gymnasium-essen-werden.de', 'Kug30420', '', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO APP.BEWERBER ("NAME", VORNAME, EMAIL, PASSWORTHASH, TELEFON, GEBURTSTAG, AUTHCODE, TWOFACODE, PASSWORDRESETCODE, ADRESSE, FACHGEBIET, EINSTELLUNGEN) 
	VALUES ('Mustermann', 'Max', 'max@mustermann.de', '1a327dde57b56142141cf584ab74d4659b50bd899fe9994a14a57277b53ad95bbee8ebb96b7bf33318dc4bfa1707ce1ef829fa5773ce18f5b05a181bc28454d8', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1);
INSERT INTO APP.BEWERBER ("NAME", VORNAME, EMAIL, PASSWORTHASH, TELEFON, GEBURTSTAG, AUTHCODE, TWOFACODE, PASSWORDRESETCODE, ADRESSE, FACHGEBIET, EINSTELLUNGEN) 
	VALUES ('Musterfrau', 'Max', 'max@musterfrau.de', '1a327dde57b56142141cf584ab74d4659b50bd899fe9994a14a57277b53ad95bbee8ebb96b7bf33318dc4bfa1707ce1ef829fa5773ce18f5b05a181bc28454d8', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 2);
INSERT INTO APP.BEWERBER ("NAME", VORNAME, EMAIL, PASSWORTHASH, TELEFON, GEBURTSTAG, AUTHCODE, TWOFACODE, PASSWORDRESETCODE, ADRESSE, FACHGEBIET, EINSTELLUNGEN) 
	VALUES ('Mustermann', 'Tom', 'tom@mustermann.de', '1a327dde57b56142141cf584ab74d4659b50bd899fe9994a14a57277b53ad95bbee8ebb96b7bf33318dc4bfa1707ce1ef829fa5773ce18f5b05a181bc28454d8', '123456789', NULL, NULL, NULL, NULL, 4, 1, 4);

INSERT INTO APP.INTERESSENFELDER ("NAME") 
	VALUES ('Klaiver');
INSERT INTO APP.INTERESSENFELDER ("NAME") 
	VALUES ('Gitarre');
INSERT INTO APP.INTERESSENFELDER ("NAME") 
	VALUES ('Sport');
INSERT INTO APP.INTERESSENFELDER ("NAME") 
	VALUES ('Programmieren');
INSERT INTO APP.INTERESSENFELDER ("NAME") 
	VALUES ('Laufen');

INSERT INTO APP.INTERESSEN (INTERESSENFELDERID, BEWERBERID) 
	VALUES (1, 4);
INSERT INTO APP.INTERESSEN (INTERESSENFELDERID, BEWERBERID) 
	VALUES (2, 4);
INSERT INTO APP.INTERESSEN (INTERESSENFELDERID, BEWERBERID) 
	VALUES (3, 4);
INSERT INTO APP.INTERESSEN (INTERESSENFELDERID, BEWERBERID) 
	VALUES (4, 4);
INSERT INTO APP.INTERESSEN (INTERESSENFELDERID, BEWERBERID) 
	VALUES (5, 4);

INSERT INTO APP.LEBENSLAUFSTATION ("START", ENDE, INFO, REFERENZ) 
	VALUES ('2015-01-01 23:16:18.0', '2018-12-31 23:16:18.0', 'Bachelor', NULL);
INSERT INTO APP.LEBENSLAUFSTATION ("START", ENDE, INFO, REFERENZ) 
	VALUES ('2019-01-01 23:16:18.0', '2021-12-31 23:16:18.0', 'Master', NULL);
INSERT INTO APP.LEBENSLAUFSTATION ("START", ENDE, INFO, REFERENZ) 
	VALUES ('2022-01-01 23:16:18.0', '2022-05-01 00:16:18.0', 'Arbeit bei GFOS', NULL);

INSERT INTO APP.LEBENSLAUF (LEBENSLAUFSTATIONID, BEWERBERID) 
	VALUES (1, 4);
INSERT INTO APP.LEBENSLAUF (LEBENSLAUFSTATIONID, BEWERBERID) 
	VALUES (2, 4);
INSERT INTO APP.LEBENSLAUF (LEBENSLAUFSTATIONID, BEWERBERID) 
	VALUES (3, 4);

INSERT INTO APP.JOBANGEBOT (FACHGEBIET, TITLE, KURZBESCHREIBUNG, LANGBESCHREIBUNG, MONATSGEHALT, JAHRESGEHALT, URLAUBSTAGE, VORTEILE, ISTREMOTE, ADRESSE, EINSTELLDATUM, BEWERBUNGSFRIST, BEWERBUNGSTYP, ANSPRECHPARTNER, "START", ISTBEFRISTET, ENDE, ENTFERNUNG, VONCHEFGEPINNT) 
	VALUES (1, 'IT-SicherheitJob 1', 'Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata', 'Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat facer possim assum. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim v', 4000, 100000, 40, 'Du bekommst einen Laptop', true, 1, '2021-12-05 23:16:18.0', '2022-12-01 23:16:18.0', 1, 1, '2022-07-06 00:16:18.0', false, '2023-07-06 00:16:18.0', NULL, true);
INSERT INTO APP.JOBANGEBOT (FACHGEBIET, TITLE, KURZBESCHREIBUNG, LANGBESCHREIBUNG, MONATSGEHALT, JAHRESGEHALT, URLAUBSTAGE, VORTEILE, ISTREMOTE, ADRESSE, EINSTELLDATUM, BEWERBUNGSFRIST, BEWERBUNGSTYP, ANSPRECHPARTNER, "START", ISTBEFRISTET, ENDE, ENTFERNUNG, VONCHEFGEPINNT) 
	VALUES (1, 'IT-SicherheitJob 2', 'Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata', 'Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat facer possim assum. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim v', 4000, 100000, 40, 'Du bekommst einen Laptop', true, 2, '2021-12-05 23:16:18.0', '2022-12-01 23:16:18.0', 1, 1, '2022-07-06 00:16:18.0', false, '2023-07-06 00:16:18.0', NULL, true);
INSERT INTO APP.JOBANGEBOT (FACHGEBIET, TITLE, KURZBESCHREIBUNG, LANGBESCHREIBUNG, MONATSGEHALT, JAHRESGEHALT, URLAUBSTAGE, VORTEILE, ISTREMOTE, ADRESSE, EINSTELLDATUM, BEWERBUNGSFRIST, BEWERBUNGSTYP, ANSPRECHPARTNER, "START", ISTBEFRISTET, ENDE, ENTFERNUNG, VONCHEFGEPINNT) 
	VALUES (1, 'IT-SicherheitJob 3', 'Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata', 'Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat facer possim assum. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim v', 4000, 100000, 40, 'Du bekommst einen Laptop', true, 3, '2021-12-05 23:16:18.0', '2022-12-01 23:16:18.0', 1, 1, '2022-07-06 00:16:18.0', false, '2023-07-06 00:16:18.0', NULL, false);
INSERT INTO APP.JOBANGEBOT (FACHGEBIET, TITLE, KURZBESCHREIBUNG, LANGBESCHREIBUNG, MONATSGEHALT, JAHRESGEHALT, URLAUBSTAGE, VORTEILE, ISTREMOTE, ADRESSE, EINSTELLDATUM, BEWERBUNGSFRIST, BEWERBUNGSTYP, ANSPRECHPARTNER, "START", ISTBEFRISTET, ENDE, ENTFERNUNG, VONCHEFGEPINNT) 
	VALUES (2, 'Marketing Job 1', 'Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata', 'Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat facer possim assum. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim v', 3500, 80000, 40, 'Du bekommst einen Laptop und eine BahnCard', true, 5, '2021-12-05 23:16:18.0', '2022-12-01 23:16:18.0', 0, 1, '2022-07-06 00:16:18.0', false, '2023-07-06 00:16:18.0', NULL, true);
INSERT INTO APP.JOBANGEBOT (FACHGEBIET, TITLE, KURZBESCHREIBUNG, LANGBESCHREIBUNG, MONATSGEHALT, JAHRESGEHALT, URLAUBSTAGE, VORTEILE, ISTREMOTE, ADRESSE, EINSTELLDATUM, BEWERBUNGSFRIST, BEWERBUNGSTYP, ANSPRECHPARTNER, "START", ISTBEFRISTET, ENDE, ENTFERNUNG, VONCHEFGEPINNT) 
	VALUES (2, 'Marketing Job 2', 'Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata', 'Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat facer possim assum. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim v', 3500, 80000, 40, 'Du bekommst einen Laptop und eine BahnCard', true, 6, '2021-12-05 23:16:18.0', '2022-12-01 23:16:18.0', 0, 1, '2022-07-06 00:16:18.0', false, '2023-07-06 00:16:18.0', NULL, true);
INSERT INTO APP.JOBANGEBOT (FACHGEBIET, TITLE, KURZBESCHREIBUNG, LANGBESCHREIBUNG, MONATSGEHALT, JAHRESGEHALT, URLAUBSTAGE, VORTEILE, ISTREMOTE, ADRESSE, EINSTELLDATUM, BEWERBUNGSFRIST, BEWERBUNGSTYP, ANSPRECHPARTNER, "START", ISTBEFRISTET, ENDE, ENTFERNUNG, VONCHEFGEPINNT) 
	VALUES (2, 'Marketing Job 3', 'Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata', 'Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat facer possim assum. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim v', 3500, 80000, 40, 'Du bekommst einen Laptop und eine BahnCard', true, 7, '2021-12-05 23:16:18.0', '2022-12-01 23:16:18.0', 0, 1, '2022-07-06 00:16:18.0', false, '2023-07-06 00:16:18.0', NULL, true);

INSERT INTO APP.BEWERBUNG (BEWERBER, JOBANGEBOT, DATUM, STATUS) 
	VALUES (4, 1, '2022-05-01 00:16:18.0', 0);
INSERT INTO APP.BEWERBUNG (BEWERBER, JOBANGEBOT, DATUM, STATUS) 
	VALUES (4, 2, '2022-05-01 00:16:18.0', 3);
INSERT INTO APP.BEWERBUNG (BEWERBER, JOBANGEBOT, DATUM, STATUS) 
	VALUES (4, 3, '2022-05-01 00:16:18.0', 1);

INSERT INTO APP.ARBEITETAN (PERSONALERID, BEWERBUNGID) 
	VALUES (1, 2);
INSERT INTO APP.ARBEITETAN (PERSONALERID, BEWERBUNGID) 
	VALUES (1, 3);
INSERT INTO APP.ARBEITETAN (PERSONALERID, BEWERBUNGID) 
	VALUES (6, 1);

INSERT INTO APP.TODO (TITLE, ORDERID, PERSONALER) 
	VALUES ('Bewerbung von Tom Mustermann bearbeiten', 1, 1);
INSERT INTO APP.TODO (TITLE, ORDERID, PERSONALER) 
	VALUES ('neue Personaler einstellen', 2, 1);
INSERT INTO APP.TODO (TITLE, ORDERID, PERSONALER) 
	VALUES ('Kaffee holen', 3, 1);

INSERT INTO APP.BEWERBUNGSNACHRICHT (TEXT, VONBEWERBER, BEWERBUNG, DATUM) 
	VALUES ('Hallo Tom, wir freuen uns auf deine Bewerbung', false, 3, '2022-04-30 23:31:55.805');
INSERT INTO APP.BEWERBUNGSNACHRICHT (TEXT, VONBEWERBER, BEWERBUNG, DATUM) 
	VALUES ('Dankeschön, ich hoffe Ihnen gefällt mein Lebenslauf', true, 3, '2022-04-30 23:32:57.749');
