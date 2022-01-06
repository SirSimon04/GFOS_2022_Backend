package Service;

import com.google.gson.Gson;
import okhttp3.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GeocodingService{

    private final OkHttpClient httpClient = new OkHttpClient();

    private final Gson parser = new Gson();

    public Double[] getCoordinates(String hausnummer, String straße, String plz, String stadt) throws Exception{

        Request request = new Request.Builder()
                .url("https://api.mapbox.com/geocoding/v5/mapbox.places/" + hausnummer + "%20" + straße + "%20" + plz + "%20" + stadt + ".json?access_token=pk.eyJ1Ijoic2lyc2ltb24wNCIsImEiOiJja3h1anRzY3YweDE1Mm9vNW4xbmY2dGN1In0.1LfSFOli4OfQCqaz9qjwrg")
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

class GeocodingResponse{

    public String type;
    public List<String> query;
    public List<Feature> features;
}

class Feature{

    public String id;
    public Double[] center;
}
