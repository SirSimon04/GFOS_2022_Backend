package Service;

import Entitiy.Adresse;
import com.google.gson.Gson;
import okhttp3.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * <h1>Service für Geocoding</h1>
 * <p>
 * Diese Klasse stellt Methoden zum Geocoding bereit</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
public class GeocodingService{

    private final OkHttpClient httpClient = new OkHttpClient();

    private final Gson parser = new Gson();

    /**
     * Diese Methode führt das Geocoding durch.
     * Das bedeutet, das aus einer Adresse die Koordinaten bestimmt werden
     *
     * @param a Adresse
     * @return Koordinaten
     * @throws Exception
     */
    public Double[] getCoordinates(Adresse a) throws Exception{

        Request request = new Request.Builder()
                .url("https://api.mapbox.com/geocoding/v5/mapbox.places/" + a.getHausnummer() + "%20" + a.getStrasse() + "%20" + a.getPlz() + "%20" + a.getStadt() + ".json?access_token=pk.eyJ1Ijoic2lyc2ltb24wNCIsImEiOiJja3h1anRzY3YweDE1Mm9vNW4xbmY2dGN1In0.1LfSFOli4OfQCqaz9qjwrg")
                .build();

        try(Response response = httpClient.newCall(request).execute()){

            if(!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }

            String json = response.body().string();
            GeocodingResponse geocodingResponse = parser.fromJson(json, GeocodingResponse.class);

            System.out.println(Arrays.toString(geocodingResponse.features.get(0).center));

            if(geocodingResponse.features.isEmpty()){
                throw new Exception();
            }else{
                return geocodingResponse.features.get(0).center;
            }
        }

    }
}

/**
 * <h1>Klasse für die Antwort des Geocoding-Services</h1>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
class GeocodingResponse{

    public String type;
    public List<String> query;
    public List<Feature> features;
}

/**
 * <h1>Klasse für die Antwort des Geocoding-Services</h1>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
class Feature{

    public String id;
    public Double[] center;
}
