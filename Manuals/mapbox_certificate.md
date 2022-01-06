# Anleitung zum Hinzufügen des MapBox-Zertifikats

## Zertifikat herunterladen
true | openssl s_client -connect api.mapbox.com:443 2>/dev/null | openssl x509 > api.mapbox.com

## Zertifikat dem Keystore hinzufügen
keytool -importcert -file api.mapbox.com -alias MapBox -keystore cacerts.jks -storepass changeit
in *glassfish/domains/domainN/config* ausführen, wobei **N** die Domainnumer ist

