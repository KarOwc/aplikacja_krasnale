package grupa.krasnale;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import grupa.krasnale.adapters.ListSearchAdapter;
import grupa.krasnale.models.DwarfModel;
import grupa.krasnale.models.DwarfWithDistance;
import grupa.krasnale.services.DataService;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class ListActivity extends AppCompatActivity implements ListSearchAdapter.Callback {

    SortType sortType = SortType.Distance;
    String searchText = "";
    LatLng location = null;

    List<DwarfModel> originalList;
    ListSearchAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.dwarf_list);

        listenLocation();

        DataService service = new DataService(this);
        originalList = new ArrayList<>(service.dwarfs);

        setUpSearchEditText();
        setUpRecyclerView();

        refreshList();
    }

    private void setUpSearchEditText() {
        ((EditText) findViewById(R.id.search)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchText = s.toString();
                refreshList();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void refreshList() {
        SortType sortType = this.sortType;
        if (sortType == SortType.Distance && location == null) {
            sortType = SortType.Alphabetic;
        }

        List<DwarfWithDistance> found = new ArrayList<>();
        for (int i = 0; i < originalList.size(); i++) {
            DwarfModel dwarfModel = originalList.get(i);
            if (dwarfModel.title.contains(searchText)) {
                found.add(new DwarfWithDistance(dwarfModel, calculateDistance(dwarfModel)));
            }
        }
        Comparator<DwarfWithDistance> comparator;
        if (sortType == SortType.Distance) {
            comparator = new Comparator<DwarfWithDistance>() {
                @Override
                public int compare(DwarfWithDistance o1, DwarfWithDistance o2) {
                    return o1.getDistance().compareTo(o2.getDistance());
                }
            };
        } else {
            comparator = new Comparator<DwarfWithDistance>() {
                @Override
                public int compare(DwarfWithDistance o1, DwarfWithDistance o2) {
                    return o1.getDwarfModel().title.compareTo(o2.getDwarfModel().title);
                }
            };
        }
        Collections.sort(found, comparator);
        adapter.setNewFound(found);
    }

    private Float calculateDistance(DwarfModel dwarfModel) {
        if (location == null) {
            return null;
        }
        float[] distance = new float[1];
        Location.distanceBetween(location.latitude, location.longitude, dwarfModel.marker.lat, dwarfModel.marker.lng, distance);
        return distance[0];
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        listenLocation();
    }

    private void listenLocation() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, 0);
            }
            return;
        }
        Location lastKnownLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        location = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 500, new LocationListener() {
            @Override
            public void onLocationChanged(Location newLocation) {
                location = new LatLng(newLocation.getLatitude(), newLocation.getLongitude());
                refreshList();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        }, getMainLooper());
    }

    private void setUpRecyclerView() {
        adapter = new ListSearchAdapter(this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.sortAlphabetically) {
            sortType = SortType.Alphabetic;
            refreshList();
            return true;
        } else if (item.getItemId() == R.id.sortDistance) {
            sortType = SortType.Distance;
            refreshList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void selected(DwarfWithDistance model) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("krasnal", model.getDwarfModel());
        startActivity(intent);
    }

    public enum SortType {
        Alphabetic, Distance;
    }
}

