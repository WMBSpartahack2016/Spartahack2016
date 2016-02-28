package com.moouonline.spartahack.wmb;

import android.util.JsonReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by perrych2 on 2/27/16.
 */
public class JSONService {
    private static final String BUS_LOCATIONS_ENDPOINT = "http://spartahack.moouonline.com/service2.php";
    private static final String TRIP_UPDATES_URL = "http://spartahack.moouonline.com/test1intro.php";
//    private static final String TRIP_UPDATES_URL = "http://spartahack.moouonline.com/tripupdates.php";
// private static final String BUS_LOCATIONS_ENDPOINT = "http://spartahack.moouonline.com/testmap6.php";

    public Map<String, BusLocation> getBusLocations() throws JSONServiceException {
        Map<String, BusLocation> busLocations = new HashMap<>();
        InputStream stream;
        try {
            URL url = new URL(BUS_LOCATIONS_ENDPOINT);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int responseCode = conn.getResponseCode();
            if(responseCode != HttpURLConnection.HTTP_OK) {
                throw new JSONServiceException("Http request received errorcode " + responseCode);
            }

            JsonReader reader = new JsonReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            reader.beginArray();
            BusLocation tempLocation;
            while(reader.hasNext()) {
                tempLocation = new BusLocation(reader);
                busLocations.put(tempLocation.getTripID(), tempLocation);
            }
            reader.endArray();

            reader.close();

        } catch (MalformedURLException e) {
            throw new JSONServiceException(e);
        } catch (IOException e) {
            throw new JSONServiceException(e);
        }
        return busLocations;
    }


    public JSONObject getStops(double lon, double lat, List<String> stops) throws JSONServiceException {
        JSONObject returnvalue = null;
        Map<String, BusLocation> busLocations = new HashMap<>();
        InputStream stream;
        try {
            URL url = new URL(TRIP_UPDATES_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);            JSONObject payload = new JSONObject();
            payload.put("lon", lon);
            payload.put("lat", lat);
            JSONArray stopList = new JSONArray();
            for(String stop : stops) {
                stopList.put(stop);
            }
            payload.put("stops", stopList);
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(payload.toString());
            System.err.println("payload.toString(): " + payload.toString());
            writer.flush();
            writer.close();
            int responseCode = conn.getResponseCode();
            if(responseCode != HttpURLConnection.HTTP_OK) {
                throw new JSONServiceException("Http request received errorcode " + responseCode);
            }

            StringBuilder sb = new StringBuilder();
            InputStream is = conn.getInputStream();
            byte[] buffer = new byte[500];
            int readsize;
            while((readsize = is.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, readsize));
            }
            is.close();
            returnvalue = new JSONObject(sb.toString());
        } catch (MalformedURLException e) {
            throw new JSONServiceException(e);
        } catch (IOException e) {
            throw new JSONServiceException(e);
        } catch (JSONException e) {
            throw new JSONServiceException(e);
        }
        return returnvalue;
    }
    public JSONObject getStops(List<String> stops) throws JSONServiceException {
        JSONObject returnvalue = null;
        Map<String, BusLocation> busLocations = new HashMap<>();
        InputStream stream;
        try {
            URL url = new URL(TRIP_UPDATES_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            JSONObject payload = new JSONObject();
            JSONArray stopList = new JSONArray();
            for(String stop : stops) {
                stopList.put(stop);
            }
            payload.put("stops", stopList);
            payload.put("lat", -84.0);
            payload.put("lon", 48);
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            System.err.println("Payload: " + payload.toString());
            writer.write(payload.toString());
            writer.flush();
            writer.close();
            int responseCode = conn.getResponseCode();
            if(responseCode != HttpURLConnection.HTTP_OK) {
                throw new JSONServiceException("Http request received errorcode " + responseCode);
            }

            StringBuilder sb = new StringBuilder();
            InputStream is = conn.getInputStream();
            byte[] buffer = new byte[500];
            int readsize;
            while((readsize = is.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, readsize));
            }
            is.close();
            System.err.println("sb.toString(): " + sb.toString());
            returnvalue = new JSONObject(sb.toString());
        } catch (MalformedURLException e) {
            throw new JSONServiceException(e);
        } catch (IOException e) {
            throw new JSONServiceException(e);
        } catch (JSONException e) {
            throw new JSONServiceException(e);
        }
        return returnvalue;
    }

}
