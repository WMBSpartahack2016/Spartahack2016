package com.moouonline.spartahack.wmb;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.SearchManager;
import android.content.Context;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StopsActivity extends AppCompatActivity implements LocationListener {
    private static final int IDX_FAVORITES = 0;
    private static final int IDX_NEARBY = 1;
    private static final int IDX_RECENT = 2;

    private boolean _initialized = false;
    private static float MIN_X = 100f;
    private static float MAX_Y = 50f;
    private float downX, downY;
    private boolean down = false;
    transient private List<BusStop> _favorites;
    transient private List<BusStop> _recent;
    SharedPreferences sharedpreferences;

    static String DEBUG_TAG = "WMB_STOPSACTIVITY";
    StopAdapter listAdapter;
    ExpandableListView expListView;
    List<String> Headers;
    HashMap<String, List<String>> mapOfLists;

    private class StopArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StopArrayAdapter(Context context, int textViewResourceId,
                                List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

    private void loadFavsandRecent() {
        _favorites = new ArrayList<BusStop>();
        _recent = new ArrayList<BusStop>();
        JSONObject favs;
        try {
            favs = new JSONObject(
                    sharedpreferences.getString(
                            getString(R.string.persist_key_favorites), "{}"
                    )
            );
        } catch (JSONException ex) {
            favs = new JSONObject();
        }
        Iterator<String> iter = favs.keys();
        while(iter.hasNext()) {
            String key = iter.next();
            try {
                _favorites.add(new BusStop(key, favs.getString(key)));
            } catch (Exception ex) {
                // I could care less if that part failed.
            }
        }


        JSONObject rec;
        try {
            rec = new JSONObject(
                    sharedpreferences.getString(
                            getString(R.string.persist_key_recent), "{}"
                    )
            );
        } catch (JSONException ex) {
            rec = new JSONObject();
        }
        iter = rec.keys();
        while(iter.hasNext()) {
            String key = iter.next();
            try {
                _recent.add(new BusStop(key, rec.getString(key)));
            } catch (Exception ex) {
                // I could care less if that part failed.
            }
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedpreferences = getSharedPreferences(getString(R.string.prefs_key_stops), Context.MODE_PRIVATE);
        loadFavsandRecent();
        setContentView(R.layout.activity_stops);
        expListView = (ExpandableListView) findViewById(R.id.lvStops);

        // preparing list data
        prepareListData();

        listAdapter = new StopAdapter(this, Headers, mapOfLists);

        // setting list adapter
        expListView.setAdapter(listAdapter);
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("NO GPS ###############################");
            // TODO:  No GPS Case
        } else {
            final List<String> stops = new ArrayList<>();
            stops.add("3988");
            stops.add("4537");
            stops.add("1675");
            stops.add("1681");
            new Thread(new Runnable() {
                final JSONService jserve = new JSONService();
                @Override
                public void run() {
                    List<BusStop> nearby = new ArrayList<>();

                    System.err.println("Stops: " + stops.toString());
                    JSONObject stopdata = jserve.getStops(stops);
                    Iterator<String> iter = stopdata.keys();
                    while(iter.hasNext()) {
                        String key = iter.next();
                        for(BusStop stop : _favorites) {
                            if(stop.getStopid().equals(key)) {
                                continue;
                            }
                        }
                        for(BusStop stop : _recent) {
                            if(stop.getStopid().equals(key)) {
                                continue;
                            }
                        }
                        try {
                            JSONArray props = (JSONArray)stopdata.get(key);
                            BusStop stop = new BusStop(key, props.getString(0));
                            mapOfLists.get(Headers.get(IDX_FAVORITES)).add(stop.getStopid() + "-" + stop.getDescription());
//                            if(((JSONArray)props[1]).size.)
//                            JSONObject rec = stopdata.get(key);
//                            nearby.add(stopdata.get(key))
                        } catch (JSONException ex) {
                            // Don't care
                        }
                        // put it in nearby
                    }
                    System.out.println(stopdata.toString());
                    // ON Main Thread:
                    mapOfLists.get(Headers.get(IDX_FAVORITES)).clear();
                    mapOfLists.get(Headers.get(IDX_RECENT)).clear();
                    mapOfLists.get(Headers.get(IDX_NEARBY)).clear();
                }
            }).start();
            System.out.println("Initializing GPS ######################");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
        }

//        new Thread(new Runnable() {
//            final JSONService jserve = new JSONService();
//            @Override
//            public void run() {
//                System.out.println("threadForMapStart");
//                Map<String, BusLocation> locations = jserve.getBusLocations();
//                System.out.println("BeforeUpdateBuses");
//                updateBuses(locations);
//                System.out.println("AfterUpdateBuses");
//                _handler.postDelayed(updater, UPDATE_INTERVAL);
//            }
//        }).start();

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.i("save", "save instance state called.");
        Log.i("save", "save instance state called.");
        JSONObject favs = new JSONObject();
        for(BusStop bs : _favorites) {
            try {
                favs.put(bs.getStopid(), bs.getDescription());
            } catch (JSONException ex) {
                // Could care less if this failed.
            }
        }
        sharedpreferences.edit().putString(getString(R.string.persist_key_favorites), favs.toString());
        JSONObject rec = new JSONObject();
        for(BusStop bs : _recent) {
            try {
                rec.put(bs.getStopid(), bs.getDescription());
            } catch (JSONException ex) {
                // Could care less if this failed.
            }
        }
        sharedpreferences.edit().putString(getString(R.string.persist_key_recent), favs.toString());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        // Inflate menu to add items to action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }


    /*
      * Preparing the list data
      */
    private void prepareListData() {
        Headers = new ArrayList<String>();
        mapOfLists = new HashMap<String, List<String>>();

        // Adding child data
        Headers.add("Favorites");
        Headers.add("Nearby");
        Headers.add("Recent");
        // Adding child data
        List<String> favorites = new ArrayList<String>();

        List<String> nearby = new ArrayList<String>();

        List<String> recent = new ArrayList<String>();

        mapOfLists.put(Headers.get(IDX_FAVORITES), favorites); // Header, Child data
        mapOfLists.put(Headers.get(IDX_NEARBY), nearby);
        mapOfLists.put(Headers.get(IDX_RECENT), recent);
    }
    // This example shows an Activity, but you would use the same approach if
// you were subclassing a View.
    @Override
    public boolean onTouchEvent(MotionEvent event){

        int action = MotionEventCompat.getActionMasked(event);
        switch(action) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                down = true;
                System.out.println("A");
                return true;
            case MotionEvent.ACTION_MOVE:
                System.out.println("B");
                return super.onTouchEvent(event);
            case MotionEvent.ACTION_UP:
                if(down == true) {
                    System.out.println("C");
                    double deltaX = event.getX() - downX;
                    double deltaY = Math.abs(event.getY() - downY);
                    if (deltaX > MIN_X && deltaX > (2.0 * deltaY)) {
                        System.out.println("D");
                        Intent intent = new Intent(this, RouteMapActivity.class);
                        System.out.print("Switching intents.");
                        startActivity(intent);
                    }
                    down = false;
                    return true;
                } else {
                    System.out.println("E");
                    return super.onTouchEvent(event);
                }
            default:
                System.out.println("F");
                return super.onTouchEvent(event);
        }
    }

    @Override
    public void onLocationChanged(Location loc) {
        final double lon = loc.getLongitude();
        final double lat = loc.getLatitude();
        final List<String> stops = new ArrayList<>();
        for(BusStop b : _favorites) {
            stops.add(b.getStopid());
        }
        for(BusStop b : _recent) {
            stops.add(b.getStopid());
        }
        new Thread(new Runnable() {
            final JSONService jserve = new JSONService();
            @Override
            public void run() {
                JSONObject stopdata = jserve.getStops(lon, lat, stops);
                System.out.println(stopdata.toString());
            }
        }).start();
    }
    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(getBaseContext(), "Gps turned on ", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

}
