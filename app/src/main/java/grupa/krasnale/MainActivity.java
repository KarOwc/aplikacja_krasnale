package grupa.krasnale;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import grupa.krasnale.models.DwarfModel;
import grupa.krasnale.services.DataService;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    DataService service;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        service = new DataService(this);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setSupportActionBar(((Toolbar) findViewById(R.id.toolbar)));
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (DwarfModel dwarf : service.dwarfs) {
            LatLng latLng = dwarf.marker.getLatLng();
            MarkerOptions markerOptions = new MarkerOptions().position(latLng);
            Marker marker = googleMap.addMarker(markerOptions);
            marker.setTag(dwarf);
            builder.include(latLng);
        }

        googleMap.setInfoWindowAdapter(new InfoWindowAdapter(this));

        LatLngBounds bounds = builder.build();
        final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 0);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                googleMap.moveCamera(cu);
            }
        }, 1000);
    }
}
