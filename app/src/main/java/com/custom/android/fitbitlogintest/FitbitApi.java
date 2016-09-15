package com.custom.android.fitbitlogintest;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * Created by duele on 9/9/2016.
 */
public class FitbitApi extends Activity{

    private static String urlString; // string to pass in url
    private static String accessToken; // string to pass in access token
    private static String requestMethod; // string to pass in GET or POST
    private static String authHeader; // string to pass in authorization header first word
    private static Boolean isRevoke = false; // boolean to check if action is revoking access token
    private static String clientId;
    private static String clientSecret;

    // this method retrieves data from api and returns resulting json string
    public static String getData(String url, String aToken){
        urlString = url;
        accessToken = aToken;
        requestMethod = "GET";
        authHeader = "Bearer";
        isRevoke = false;
        try {
            return new RetrieveDataFromApi().execute().get();
        }
        catch(InterruptedException e){
            Log.e("ERROR", e.getMessage(), e);
            return null;
        }
        catch(ExecutionException e){
            Log.e("ERROR", e.getMessage(), e);
            return null;
        }
    }

    // this method extracts information from json string and stores them in a json object
    public static JSONObject convertStringToJson(String jsonString){
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return jsonObject;
        }
        catch (JSONException e){
            Log.e("ERROR", e.getMessage(), e);
            return null;
        }
    }

    // this method revokes current access token
    public static void revokeToken(String aToken, String id, String secret){
        urlString = "https://api.fitbit.com/oauth2/revoke";
        accessToken = aToken;
        requestMethod = "POST";
        authHeader = "Basic";
        isRevoke = true;
        clientId = id;
        clientSecret = secret;
        try {
            new RetrieveDataFromApi().execute().get();
        }
        catch(InterruptedException e){
            Log.e("ERROR", e.getMessage(), e);
        }
        catch(ExecutionException e){
            Log.e("ERROR", e.getMessage(), e);
        }
    }

    // asynctask to get fitbit information from web api
    static class RetrieveDataFromApi extends AsyncTask<Void, Void, String>{

        protected String doInBackground(Void... urls){
            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod(requestMethod);
                if (isRevoke){
                    urlConnection.setRequestProperty("Authorization", authHeader+" "+ Base64.encodeToString((clientId+":"+clientSecret).getBytes("UTF-8"), Base64.DEFAULT));
                    urlConnection.addRequestProperty("token", accessToken);
                }
                else{
                    urlConnection.setRequestProperty("Authorization", authHeader+" "+accessToken);
                }
                urlConnection.connect();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream())); // getInputStream connects to url and gets stream
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            }
            catch (SocketTimeoutException e) {
                Log.e("ERROR", e.getMessage(), e);
                return "THE CONNECTION HAS TIMED OUT";
            }
            catch (MalformedURLException e){
                Log.e("ERROR", e.getMessage(), e);
                return "INCORRECT URL";
            }
            catch (IOException e){
                Log.e("ERROR", e.getMessage(), e);
                return new IOException().toString();
            }
        }
    }

}
