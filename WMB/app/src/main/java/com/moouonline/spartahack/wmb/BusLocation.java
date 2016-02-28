package com.moouonline.spartahack.wmb;

import android.util.JsonReader;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.io.Serializable;

/**
 * Created by perrych2 on 2/27/16.
 */
public class BusLocation implements Serializable {
    /** The map marker store in the map object for this bus location. */
    private Marker _mapMarker;

    /** Yes, it's kludgey, but it makes for cleaner processing in other classes. */
    private int[] _busImageIds = {
            0,
            R.drawable.bus01, R.drawable.bus02, R.drawable.bus03, R.drawable.bus04,
            R.drawable.bus05, R.drawable.bus06, R.drawable.bus07, R.drawable.bus08,
            R.drawable.bus09, R.drawable.bus10, R.drawable.bus11, R.drawable.bus12,
            R.drawable.bus13, R.drawable.bus14, R.drawable.bus15, R.drawable.bus16,
            R.drawable.bus17, R.drawable.bus18, R.drawable.bus19, R.drawable.bus20,
            R.drawable.bus21, R.drawable.bus22, R.drawable.bus23, R.drawable.bus24,
            R.drawable.bus25, R.drawable.bus26, R.drawable.bus27, R.drawable.bus28,
            R.drawable.bus29, R.drawable.bus30, R.drawable.bus31, R.drawable.bus32,
            R.drawable.bus33, R.drawable.bus34, R.drawable.bus35, R.drawable.bus36,
            R.drawable.bus37, R.drawable.bus38, R.drawable.bus39, R.drawable.bus40,
            R.drawable.bus41, R.drawable.bus42, R.drawable.bus43, R.drawable.bus44,
            R.drawable.bus45, R.drawable.bus46, R.drawable.bus47, R.drawable.bus48,
            R.drawable.bus49, R.drawable.bus50
    };

    /** The latitude of the bus in WGS 84 Coordinates */
    private double _lat;

    /** The longitude of the bus in WGS 84 Coordinates */
    private double _lon;

    /** The route number of this bus */
    private int _route_number;

    /** The direction of this bus (believe true is outbound, and false is inbound */
    private boolean _direction;

    /** The unique trip identifier for this bus.  Each trip of a bus throughout the day has a unique
     * ID
     */
    private String _tripid;

    /**
     * Constructs a bus object from a JsonReader source.  It reads the opening and closing braces
     * of the array element defining the bus location.
     * @param source The JsonReader input stream to read the bus entry from.
     * @throws java.io.IOException If an error occurs while riding decoding the object
     */
    public BusLocation(JsonReader source) throws java.io.IOException {
         source.beginArray();
        _tripid = source.nextString();
        _route_number = Integer.parseInt(source.nextString());
        _direction = "1".equals(source.nextString());
        _lon = source.nextDouble();
        _lat = source.nextDouble();
        source.endArray();
        _mapMarker = null;
    }

    /**
     * Required zero-arg constructor for serialization.  Should never be used except for deserializaing
     * instances.
     */
    protected BusLocation() {
        _lat = 0.0;
        _lon = 0.0;
        _route_number = -1;
        _direction = true;
        _tripid = "";
        _mapMarker = null;
    }

    /**
     * Gets the bus's latitude in WGS 84 coordinates.
     * @return  The bus's latitude in WGS 84 coordinates.
     */
    public double getLatitude() {
        return _lat;
    }

    /**
     * Gets the bus's longitude in WGS 84 coordinates.
     * @return The bus's longitude in WGS 84 coordinates
     */
    public double getLongitude() {
        return _lon;
    }

    /**
     * Sets the latitude of the location in WGS 84 coordinates
     * @param latitude of the bus
     */
    public void setLatitude(double latitude) {
        _lat = latitude;
    }

    /**
     * Sets the longitude of the location in WGS 84 coordinates
     * @param longitude of the bus
     */
    public void setLongitude(double longitude) {
        _lon = longitude;
    }

    /**
     * Gets this buses position as a google latlng object
     * @return LatLng position of bus
     */
    public LatLng getGoogleLatLng() {
        return new LatLng(_lat, _lon);
    }


    /**
     * Updates the value of this locations marker object with its lat and long.  Does nothing if
     * this location does not have a marker.
     */
    public void updateMarker() {
        if(_mapMarker != null) {
            _mapMarker.setPosition(getGoogleLatLng());
        }
    }

    /**
     * Gets the trip id for the bus.  The trip id is a unique identifer for each traversal of a
     * route each day.  It is used to distinguish between multiple buses traversing the same route
     * at the same time.
     * @return The unique trip identfier of the bus.
     */
    public String getTripID() {
        return _tripid;
    }

    /**
     * Gets the direction, inbound or outbound, of the bus.  Our belief is that true is outbound and
     * false is inbound, but the actual meaning of those two terms is a matter of perspective anyway.
     * @return The direction, inbound (F) or outbound (T), of the bus on this trip.
     */
    public boolean getDirection() {
        return _direction;
    }

    /**
     * Gets the route number of the bus as an integer.
     * @return The route number of the bus.
     */
    public int getRouteNumber() {
        return _route_number;
    }

    /**
     * Lazy convenience method to get the correct image identifier for this bus.  This is a really
     * stupid way to do it, but it is the only reasonably safe one.
     * @return The drawable resource id of the map image for this bus.
     */
    public int getImageID() {
        return _busImageIds[_route_number % 51];
    }

    /**
     * Sets the map marker associated with this bus location.
     * @param marker  The marker value that was returned from addMarker call of map element.
     */
    public void setMarker(Marker marker) {
        _mapMarker = marker;
    }


    /**
     * Gets the map marker associated with this bus location.  This value CAN be null.
     * @return the map marker associated with this bus location.
     */
    public Marker getMarker() {
        return _mapMarker;
    }
}
