/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Service;

import EJB.BlacklistEJB;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

/**
 *
 * @author simon
 */
@Stateless
@LocalBean
public class Tokenizer{

    @EJB
    private BlacklistEJB blacklistEJB;

    private final String SECRET_KEY = "BsR8$ATrqyrR#I*G8KDR1D%P^U7q";

    private final int expirationTime = 10; //Token hat Gültigkeit von 10 Minuten
    private final int acceptsExpired = 5; //Token kann 5 Minuten nach Ablauf noch erneuert werden

    /**
     * Diese Methode erstellt ein JWT mit der UserID als Payload.
     *
     * @param userid UserID
     * @return JWT
     * @throws JWTCreationException
     */
    public String createToken(String userid) throws JWTCreationException{
        Algorithm algo = Algorithm.HMAC256(SECRET_KEY);

        return JWT.create()
                .withIssuer("GFOS")
                .withSubject(userid)
                .withExpiresAt(new Date(System.currentTimeMillis() + (expirationTime * 60 * 1000)))
                .sign(algo);
    }

    /**
     * Diese Methode verifiziert ein Token und verlängert es bei Bedarf.
     *
     * @param token Token aus dem Authorization-Header der Request
     * @param autoRequest boolean, ob es sich um eine automatisch reloadende Request handelt (dann darf Token nicht erneurt werden)
     * @return Map mit altem Token, wenn noch gültig; Map mit neuem Token, wenn es erneuert werden konnte; null, wenn Token ungültig
     */
    public Map<String, String> verifyToken(String token, boolean autoRequest){
        Map<String, String> res = new HashMap<String, String>(); //Dictionary für Response initialiosieren

        try{ //schauen ob Token noch offiziell gültig ist (10min, expirationTime)
            Algorithm algo = Algorithm.HMAC256(SECRET_KEY);

            JWTVerifier verifier = JWT.require(algo)
                    .withIssuer("GFOS")
                    .build();
            DecodedJWT decodedToken = verifier.verify(token);

            if(blacklistEJB.onBlacklist(token)){
                return null; //Token ist nicht gültig, da es auf der Blacklist steht
            }

            res.put("token", token); //Token wieder zurückgeben
            return res;
        }catch(JWTVerificationException e){ //schauen ob Token noch erneuert werden kann (10min + 5min, acceptsExpired)
            try{
                Algorithm algo = Algorithm.HMAC256(SECRET_KEY);

                JWTVerifier verifier = JWT.require(algo)
                        .withIssuer("GFOS")
                        .acceptExpiresAt(acceptsExpired * 60)
                        .build();
                DecodedJWT decodedToken = verifier.verify(token);

                if(autoRequest){ //wenn AutoRequest, trotzdem nicht erneuern, da User nicht zwingend "aktiv" da
                    res.put("token", token); // oken wieder zurückgeben, kann aber eventuell noch durch aktive Request erneuert werden
                    return res;
                }else{ //aktive Request, Token soll erneuert werden
                    if(token != null){
                        blacklistEJB.addToken(token); //altes Token auf Blacklist
                    }
                    res.put("newToken", createToken(decodedToken.getSubject())); //erneuertes Token zurückgeben
                    return res;
                }
            }catch(JWTVerificationException ex){ //Token ungültig und kann nicht erneuert werden
                //anmeldedatenEJB.logout(token); //damit der Online-Status auf false gesetzt wird
                return null;
            }
        }
    }
}
