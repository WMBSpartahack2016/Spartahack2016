package com.moouonline.spartahack.wmb;

import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewTreeObserver;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RouteMapActivity extends FragmentActivity implements OnMapReadyCallback {
    transient private Map<String, BusLocation> _buses;
    private boolean _mapInitialized;
    private GoogleMap mMap;
    private CameraPosition _cameraPosition;
    private int _mapType;
    static final int UPDATE_INTERVAL = 30000;


    Handler _handler = new Handler();

    Runnable updater = new Runnable() {
        final JSONService jserve = new JSONService();
        @Override
        public void run() {
            new Thread(new Runnable() {
                public void run() {
                    System.out.println("threadForMapStart");
                    try {
                        Map<String, BusLocation> locations = jserve.getBusLocations();
                        updateBuses(locations);
                    } catch (JSONServiceException ex) {
                        Log.w("badjson", ex);
                    }
                }
            }).start();
            _handler.postDelayed(this, UPDATE_INTERVAL);
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _mapInitialized = false;
        setContentView(R.layout.activity_route_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if(savedInstanceState != null) {
            restoreFromBundle(savedInstanceState);
        } else {
            _mapType = GoogleMap.MAP_TYPE_SATELLITE;
            _cameraPosition = (new CameraPosition.Builder()).
                    bearing(0f).
                    target(new LatLng(
                            (42.737899398 + 42.703533239) / 2.0,
                            (-84.506256081 + -84.459735848) / 2.0)).
                    tilt(0f).
                    zoom(13.5f).
                    build();
        }
        _buses = new HashMap<>();
        new Thread(new Runnable() {
            final JSONService jserve = new JSONService();
            @Override
            public void run() {
                System.out.println("threadForMapStart");
                Map<String, BusLocation> locations = jserve.getBusLocations();
                System.out.println("BeforeUpdateBuses");
                updateBuses(locations);
                System.out.println("AfterUpdateBuses");
                _handler.postDelayed(updater, UPDATE_INTERVAL);
            }
        }).start();

    }

    private void restoreFromBundle(Bundle bundle) {
        Log.i("restore", "Restore from bundle called.");
        _mapType = bundle.getInt(getString(R.string.persist_key_map_type));
        _cameraPosition = bundle.getParcelable(getString(R.string.persist_key_camera_position));
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        final GoogleMap mapArg = googleMap;

        final ViewTreeObserver vto = findViewById(R.id.map).getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                initMap(mapArg);
            }
        });
    }

    private void initMap(GoogleMap map) {
        if(!_mapInitialized) {
            map.moveCamera(CameraUpdateFactory.newCameraPosition(_cameraPosition));
            map.setMapType(_mapType);
            _mapInitialized = true;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.i("save", "save instance state called.");
        savedInstanceState.putInt(getString(R.string.persist_key_map_type), mMap.getMapType());
        savedInstanceState.putParcelable(getString(R.string.persist_key_camera_position), mMap.getCameraPosition());
    }

    public void updateBuses(Map<String, BusLocation> locations) {
        final Map<String, BusLocation> locationUpdates = locations;
        final GoogleMap theMap = mMap;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Set<String> keys = _buses.keySet();
                BusLocation target, source;
                for (String key : keys) {
                    if (!locationUpdates.containsKey(key)) {
                        BusLocation bl = _buses.remove(key);
                        if (bl.getMarker() != null) {
                            bl.getMarker().remove();
                        }
                        bl = null;
                    }
                }

                keys = locationUpdates.keySet();
                for (String key : keys) {
                    if (_buses.containsKey(key)) {
                        target = _buses.get(key);
                        source = locationUpdates.get(key);
                        target.setLatitude(source.getLatitude());
                        target.setLongitude(source.getLongitude());
                        target.updateMarker();
                    } else {
                        source = locationUpdates.get(key);
                        _buses.put(key, source);
                        source.setMarker(
                                mMap.addMarker(
                                        new MarkerOptions().
                                                position(source.getGoogleLatLng()).
                                                draggable(false).
                                                flat(true).
                                                anchor(0.5f, 0.5f).
                                                icon(BitmapDescriptorFactory.fromResource(source.getImageID()))));
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        _handler.removeCallbacks(updater);
        super.onDestroy();
    }
}
