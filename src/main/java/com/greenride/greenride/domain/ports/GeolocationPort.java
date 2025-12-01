package com.greenride.greenride.domain.ports;


import com.greenride.greenride.dto.Coordinates;
import com.greenride.greenride.dto.RouteEstimate;

public interface GeolocationPort {
    RouteEstimate calculateRouteEstimate(Coordinates start, Coordinates destination);
    Coordinates geocodeAddress(String address);
}