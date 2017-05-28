package grupa.krasnale.models;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class MapLoc implements Serializable {
    public Double lat;
    public Double lng;

    public LatLng getLatLng() {
        return new LatLng(lat, lng);
    }
}
