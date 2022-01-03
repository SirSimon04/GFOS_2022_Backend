//package Service;
//
//import org.apache.http.Header;
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpHeaders;
//import org.apache.http.NameValuePair;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.message.BasicNameValuePair;
//import org.apache.http.util.EntityUtils;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//public class GeocodingService{
//
//    // one instance, reuse
//    private final CloseableHttpClient httpClient = HttpClients.createDefault();
//
//    public static void main() throws Exception{
//
//        System.out.println("main");
//
//        GeocodingService obj = new GeocodingService();
//
//        try{
//            System.out.println("Testing 1 - Send Http GET request");
//            obj.sendGet();
//
//            System.out.println("Testing 2 - Send Http POST request");
//            obj.sendPost();
//        }finally{
//            obj.close();
//        }
//    }
//
//    private void close() throws IOException{
//        httpClient.close();
//    }
//
//    private void sendGet() throws Exception{
//
//        HttpGet request = new HttpGet("https://www.google.com/search?q=mkyong");
//
//        // add request headers
//        request.addHeader("custom-key", "mkyong");
//        request.addHeader(HttpHeaders.USER_AGENT, "Googlebot");
//
//        try(CloseableHttpResponse response = httpClient.execute(request)){
//
//            // Get HttpResponse Status
//            System.out.println(response.getStatusLine().toString());
//
//            HttpEntity entity = response.getEntity();
//            Header headers = entity.getContentType();
//            System.out.println(headers);
//
//            if(entity != null){
//                // return it as a String
//                String result = EntityUtils.toString(entity);
//                System.out.println(result);
//            }
//
//        }
//
//    }
//
//    private void sendPost() throws Exception{
//
//        HttpPost post = new HttpPost("https://httpbin.org/post");
//
//        // add request parameter, form parameters
//        List<NameValuePair> urlParameters = new ArrayList<>();
//        urlParameters.add(new BasicNameValuePair("username", "abc"));
//        urlParameters.add(new BasicNameValuePair("password", "123"));
//        urlParameters.add(new BasicNameValuePair("custom", "secret"));
//
//        post.setEntity(new UrlEncodedFormEntity(urlParameters));
//
//        try(CloseableHttpClient httpClient = HttpClients.createDefault();
//                CloseableHttpResponse response = httpClient.execute(post)){
//
//            System.out.println(EntityUtils.toString(response.getEntity()));
//        }
//
//    }
//
//}
package Service;

import okhttp3.*;

import java.io.IOException;

public class GeocodingService{

    // one instance, reuse
    private final OkHttpClient httpClient = new OkHttpClient();

    public static void main() throws Exception{

        GeocodingService obj = new GeocodingService();

        System.out.println("Testing 1 - Send Http GET request");
        obj.sendGet();

//        System.out.println("Testing 2 - Send Http POST request");
//        obj.sendPost();
    }

    private void sendGet() throws Exception{

        Request request = new Request.Builder()
                .url("https://api.mapbox.com/geocoding/v5/mapbox.places/50A%20Scharpenhang%2045257%20Essen.json?access_token=pk.eyJ1Ijoic2lyc2ltb24wNCIsImEiOiJja3h1anRzY3YweDE1Mm9vNW4xbmY2dGN1In0.1LfSFOli4OfQCqaz9qjwrg")
                .build();

        try(Response response = httpClient.newCall(request).execute()){

            if(!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }

            // Get response body
            System.out.println(response.body().string());
        }

    }

    private void sendPost() throws Exception{

        // form parameters
        RequestBody formBody = new FormBody.Builder()
                .add("username", "abc")
                .add("password", "123")
                .add("custom", "secret")
                .build();

        Request request = new Request.Builder()
                .url("https://httpbin.org/post")
                .addHeader("User-Agent", "OkHttp Bot")
                .post(formBody)
                .build();

        try(Response response = httpClient.newCall(request).execute()){

            if(!response.isSuccessful()){
                throw new IOException("Unexpected code " + response);
            }

            // Get response body
            System.out.println(response.body().string());
        }

    }

}
