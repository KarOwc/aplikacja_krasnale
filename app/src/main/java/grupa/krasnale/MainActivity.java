package grupa.krasnale;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import grupa.krasnale.adapters.InfoWindowAdapter;
import grupa.krasnale.adapters.SearchResultAdapter;
import grupa.krasnale.models.DwarfModel;
import grupa.krasnale.services.DataService;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class MainActivity
        extends AppCompatActivity
        implements OnMapReadyCallback, SearchResultAdapter.Callback, GoogleMap.OnInfoWindowClickListener {

    // referencja do serwisu z danymi
    DataService service;
    // referencja do mapy
    private GoogleMap googleMap;
    // lista markerow w postaci slownika dla latwiejszego znajdowania markerow
    private Map<String, Marker> markers = new HashMap<>();
    // adapter do wyszukiwanych krasnali
    SearchResultAdapter adapter;
    // referencja do widoku z wyszukiwanymi krasnalami
    View searchResultView;
    // referencja do wyszukiwarki krasnali
    EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //tworzony jest serwis ktory odczytuje wszystkie dane krasnali
        service = new DataService(this);
        // odnajduje na layoucie fragment z mapa
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        // inicjacja mapy tego ona wymaga jest w dokumentacji google: android google map getting started
        mapFragment.getMapAsync(this);


        // ustawienie listy z wyszukanymi krasnalami
        adapter = new SearchResultAdapter(this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchResultView = findViewById(R.id.searchResult);
        searchResultView.setVisibility(View.GONE);

        searchEditText = (EditText) findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // wyczysc wyszukiwania jesli wpisany tekst jest krotszy niz 2 znaki
                if (s.length() < 2) {
                    adapter.setNewFound(new ArrayList<DwarfModel>());
                    searchResultView.setVisibility(View.GONE);
                    return;
                }
                // wyszukiwanie krasnali
                List<DwarfModel> found = new ArrayList<>();
                String searchText = s.toString();
                List<DwarfModel> dwarfs = service.dwarfs;
                for (int i = 0; i < dwarfs.size(); i++) {
                    DwarfModel model = dwarfs.get(i);
                    if (model.title.contains(searchText)) {
                        found.add(model);
                    }
                }
                if (found.size() > 0) {
                    searchResultView.setVisibility(View.VISIBLE);
                } else {
                    searchResultView.setVisibility(View.GONE);
                }
                adapter.setNewFound(found);
            }
        });

        findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, 0);
            }
        } else {
            googleMap.setMyLocationEnabled(true);
        }
    }

    // to sie wywoluje jak mapa sie zainicjalizuje
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, 0);
            }
        } else {
            googleMap.setMyLocationEnabled(true);
        }
        // tworzenie wszystkich markerow z krasnalami
        LatLngBounds.Builder builder = new LatLngBounds.Builder(); // to sluzy do ustawienia kamery na mapie tak zeby widac bylo wszystkie krasnale
        List<DwarfModel> dwarfs = service.dwarfs;
        for (int i = 0; i < dwarfs.size(); i++) {
            DwarfModel dwarf = dwarfs.get(i);

            LatLng latLng = dwarf.marker.getLatLng();
            MarkerOptions markerOptions = new MarkerOptions().position(latLng);
            Marker marker = googleMap.addMarker(markerOptions);
            marker.setTag(dwarf);
            builder.include(latLng);
            markers.put(dwarf.title, marker);
        }

        // tutaj ustawiany jest info window adapter
        // on tworzy ten maly widok ktory sie pokazuje po kliknieciu na marker
        googleMap.setInfoWindowAdapter(new InfoWindowAdapter(this));

        googleMap.setOnInfoWindowClickListener(this);

        // ustawiam kamere tak zeby bylo widac wszystkie markery
        LatLngBounds bounds = builder.build();
        final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 0);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                googleMap.moveCamera(cu);
            }
        }, 200);
    }

    @Override
    public void selected(DwarfModel model) {
        searchResultView.setVisibility(View.GONE);

        // szukanie wybranego krasnala w markerach
        Marker marker = markers.get(model.title);
        if (marker != null) {
            LatLng position = marker.getPosition();
            marker.showInfoWindow();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, 15);
            googleMap.animateCamera(cameraUpdate);
        }
    }

    @Override
    public void onBackPressed() {
        if (searchEditText.getText().length() > 0) {
            searchEditText.setText("");
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Intent intent = new Intent(this, DetailActivity.class);
        DwarfModel dwarfModel = (DwarfModel) marker.getTag();
        intent.putExtra("krasnal", dwarfModel);
        startActivity(intent);
    }
}
