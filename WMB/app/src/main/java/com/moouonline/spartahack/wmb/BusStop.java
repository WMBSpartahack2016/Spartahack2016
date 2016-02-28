package com.moouonline.spartahack.wmb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by perrych2 on 2/28/16.
 */
public class BusStop implements Serializable {
    private String _stopid;
    private String _description;
    private List<Integer> routes = new ArrayList<>();
    private List<Calendar> dates = new ArrayList<>();

    public BusStop() {
        _stopid = "notset";
        _description = "description";
    }


    public BusStop(String stopid, String description) {
        _stopid = stopid;
        _description = description;
    }

    public void setStopid(String stopid) {
        _stopid = stopid;
    }

    public void setDescription(String description) {
        _description = description;
    }

    public String getStopid() {
        return _stopid;
    }

    public String getDescription() {
        return _description;
    }

    public void clearRoutes() {
        routes.clear();
        dates.clear();
    }

    public void addRoute(Integer route, Calendar date) {
        routes.add(route);
        dates.add(date);
    }

    public int getRouteSize() {
        return routes.size();
    }

    public int getRoute(int index) {
        return routes.get(index);
    }

    public Calendar getDate(int index) {
        return dates.get(index);
    }
}
