package grupa.krasnale;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

// to jest aktywnosc androidowa- działanie androida
public class MainActivity
        extends AppCompatActivity
        implements OnMapReadyCallback, SearchResultAdapter.Callback, GoogleMap.OnInfoWindowClickListener {

    // referencja do serwisu z danymi
    DataService service;
    // referencja do mapy
    private GoogleMap googleMap;
    // lista markerow w postaci slownika (dla latwiejszego znajdowania markerow)
    private Map<String, Marker> markers = new HashMap<>();
    // adapter do wyszukiwanych krasnali
    SearchResultAdapter adapter;
    // referencja do widoku z wyszukiwanymi krasnalami
    View searchResultView;
    // referencja do wyszukiwarki krasnali
    EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //inicjonowanie aktywności oraz ustawienie odpowiedniego pliku widoku( layout'u) dla aktywnosci
        super.onCreate(savedInstanceState);
        setContentView(grupa.krasnale.R.layout.activity_main);

        //tworzenie  serwisu, ktory odczytuje wszystkie dane krasnali
        service = new DataService(this);
        // odnajduje na layoucie fragment z mapa
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(grupa.krasnale.R.id.map);
        // inicjacja mapy google
        mapFragment.getMapAsync(this);


        // ustawienie listy z wyszukanymi krasnalami
        adapter = new SearchResultAdapter(this);
        RecyclerView recyclerView = (RecyclerView) findViewById(grupa.krasnale.R.id.recyclerView);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchResultView = findViewById(grupa.krasnale.R.id.searchResult);
        searchResultView.setVisibility(View.GONE);

        searchEditText = (EditText) findViewById(grupa.krasnale.R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // wyczyszczenie wyszukiwania jesli wpisany tekst jest krotszy niz 2 znaki
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
    }

    // W momencie zainicjalizowania mapy pojawia się to:
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        // tworzenie wszystkich markerow z krasnalami
        LatLngBounds.Builder builder = new LatLngBounds.Builder(); // to sluzy do zasięgu na mapie tak zeby widac bylo wszystkie krasnale
        List<DwarfModel> dwarfs = service.dwarfs;
        for (int i = 0; i < dwarfs.size(); i++) {
            DwarfModel dwarf = dwarfs.get(i);

            LatLng latLng = dwarf.marker.getLatLng(); // pozycja krasnala na mapie
            MarkerOptions markerOptions = new MarkerOptions().position(latLng); // tworzenie markera
            Marker marker = googleMap.addMarker(markerOptions); // dodanie markera do mapy
            marker.setTag(dwarf); // dodanie do markera dane model krasnala
            builder.include(latLng); //dodanie  pozycje do buildera
            markers.put(dwarf.title, marker);
        }

        // wstawienie info window adapter
        //  tworzy maly widok w postaci okna, ktory sie pokazuje po kliknieciu na marker
        googleMap.setInfoWindowAdapter(new InfoWindowAdapter(this));

        googleMap.setOnInfoWindowClickListener(this);

        // ustawienie zasięgu w taki sposób tak zeby bylo widac wszystkie markery
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
