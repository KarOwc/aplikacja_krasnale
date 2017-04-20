package grupa.krasnale.models;

import com.google.android.gms.maps.model.LatLng;

public class MapLoc {
    public Double lat;
    public Double lng;

    public LatLng getLatLng() {
        return new LatLng(lat, lng);
    }
}
