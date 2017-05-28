package grupa.krasnale.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import grupa.krasnale.R;
import grupa.krasnale.models.DwarfModel;

public class InfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Context context;

    public InfoWindowAdapter(Context context) {
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        DwarfModel dwarf = (DwarfModel) marker.getTag();
        if (dwarf == null) {
            throw new RuntimeException("nie ma dwarfa!;<");
        }
        View view = LayoutInflater.from(context).inflate(R.layout.info_window, null);
        ((TextView) view.findViewById(R.id.name)).setText(dwarf.title);
        ((TextView) view.findViewById(R.id.adress)).setText(dwarf.adres);
        return view;
    }
}
