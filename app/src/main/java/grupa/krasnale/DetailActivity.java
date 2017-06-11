package grupa.krasnale;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import grupa.krasnale.models.DwarfModel;

public class DetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    DwarfModel dwarfModel; //krasnal przeslany przez intent

    // referencje do elementow UI
    TextView descriptionTextView;
    TextView titleTextView;
    ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);

        // znajdowanie elementow z UI
        descriptionTextView = (TextView) findViewById(R.id.description);
        titleTextView = (TextView) findViewById(R.id.title);
        imageView = (ImageView) findViewById(R.id.image);

        // odebranie krasnala z intentu
        dwarfModel = (DwarfModel) getIntent().getSerializableExtra("krasnal");

        // tak jak wczesniej ladowanie mapy
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // ustawienie tytulu bo to juz mamy
        titleTextView.setText(dwarfModel.title);

        // zapytanie o reszte danych krasnala do strony krasnale.pl
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET,
                dwarfModel.objLink, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // jesli sie uda pobrac to wywola sie tak metoda
                dataDownloaded(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // jesli blad poleci pokaze sie tekst ze sie nie udalo pobrac
                descriptionTextView.setText(R.string.error);
            }
        });
        requestQueue.add(request);
    }

    private void dataDownloaded(String response) {
        //zamiana stringa z odpowiedzia na dokument HTML
        Document htmlDocument = Jsoup.parse(response);

        // odczytanie linku do obrazka z html
        String imageUrl = getImageUrl(htmlDocument);
        // odczytanie opisu krasnala z html
        Spanned description = getDescription(htmlDocument);

        // ustawienie opisu
        descriptionTextView.setText(description);
        // pobieranie i ustawienie obrazka przy pomocy biblioteki picasso
        Picasso.with(this).load(imageUrl).into(imageView);
    }

    // funkcja odczytujaca opis z htmla
    private Spanned getDescription(Document document) {
        // trzeba by zobaczyc jak wyglada strona html z tymi krasnalami ale znalezienie elementu
        // z taka klasa .entry-content-inner zalatwia sprawe
        Elements select = document.select(".entry-content-inner");
        String rawHtml = select.outerHtml();
        Spanned spanned;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            spanned = Html.fromHtml(rawHtml, Html.FROM_HTML_MODE_LEGACY);
        } else {
            spanned = Html.fromHtml(rawHtml);
        }
        return spanned;
    }

    private String getImageUrl(Document document) {
        // element klasy .single-krasnal-thumbnail-inner zawiera link do obrazka :)
        Elements select = document.select(".single-krasnal-thumbnail-inner");
        Element element = select.get(0);
        String style = element.attr("style");
        int startIndex = 0;
        int endIndex = 0;
        for (int i = 0; i < style.length(); i++) {
            char c = style.charAt(i);
            if (c == '(') {
                startIndex = i + 1;
            } else if (c == ')') {
                endIndex = i;
                break;
            }
        }
        return style.substring(startIndex, endIndex);
    }

    // to sie dzieje jak mapa sie zainicjuje
    // wyswietla marker z tym krasnalem i pokazuje mape zdentrowana na nim
    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng position = dwarfModel.marker.getLatLng();
        MarkerOptions markerOptions = new MarkerOptions().position(position);
        googleMap.addMarker(markerOptions);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, 16);
        googleMap.moveCamera(cameraUpdate);
    }
}
