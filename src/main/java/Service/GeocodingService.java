package Service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import java.io.IOException;
import java.util.ArrayList;

public class GeocodingService{

    private final OkHttpClient httpClient = new OkHttpClient();

    private final Gson parser = new Gson();

    public void sendGet(String hausnummer, String straße, String plz, String stadt) throws Exception{

        Request request = new Request.Builder()
                .url("https://api.mapbox.com/geocoding/v5/mapbox.places/" + hausnummer + "%20" + straße + "%20" + plz + "%20" + stadt + ".json?access_token=pk.eyJ1Ijoic2lyc2ltb24wNCIsImEiOiJja3h1anRzY3YweDE1Mm9vNW4xbmY2dGN1In0.1LfSFOli4OfQCqaz9qjwrg")
                //                .url("https://api.mapbox.com")
                .build();

        try(Response response = httpClient.newCall(request).execute()){

            if(!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }

            // Get response body
            System.out.println(response.body().string());

            String jsonObject = parser.toJson(response.body());

            System.out.println(jsonObject);
        }

    }

//    private void sendPost() throws Exception{
//
//        // form parameters
//        RequestBody formBody = new FormBody.Builder()
//                .add("username", "abc")
//                .add("password", "123")
//                .add("custom", "secret")
//                .build();
//
//        Request request = new Request.Builder()
//                .url("https://httpbin.org/post")
//                .addHeader("User-Agent", "OkHttp Bot")
//                .post(formBody)
//                .build();
//
//        try(Response response = httpClient.newCall(request).execute()){
//
//            if(!response.isSuccessful()){
//                throw new IOException("Unexpected code " + response);
//            }
//
//            // Get response body
//            System.out.println(response.body().string());
//        }
//
//    }
}
