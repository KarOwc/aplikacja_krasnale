package grupa.krasnale.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import grupa.krasnale.R;
import grupa.krasnale.models.DwarfModel;
import grupa.krasnale.models.DwarfWithDistance;

public class ListSearchAdapter extends RecyclerView.Adapter<ListSearchAdapter.DwarfVH> {

    private List<DwarfWithDistance> dwarfs = new ArrayList<>();
    private Callback callback;

    public ListSearchAdapter(Callback callback) {
        this.callback = callback;
    }

    @Override
    public DwarfVH onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item_with_distance, parent, false);
        return new DwarfVH(view);
    }

    @Override
    public void onBindViewHolder(DwarfVH holder, int position) {
        holder.setModel(dwarfs.get(position));
    }

    @Override
    public int getItemCount() {
        return dwarfs.size();
    }

    public void setNewFound(List<DwarfWithDistance> data) {
        dwarfs = data;
        notifyDataSetChanged();
    }

    class DwarfVH extends RecyclerView.ViewHolder {

        TextView name;
        TextView address;
        TextView distance;
        DwarfWithDistance model;

        DwarfVH(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.name);
            address = (TextView) itemView.findViewById(R.id.address);
            distance = (TextView) itemView.findViewById(R.id.distance);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.selected(model);
                }
            });
        }

        void setModel(DwarfWithDistance model) {
            this.model = model;
            DwarfModel dwarfModel = model.getDwarfModel();
            name.setText(dwarfModel.title);
            address.setText(dwarfModel.adres);
            distance.setText(model.getDistance() == null ? "" : String.format(Locale.US, "%.2f m", model.getDistance()));
        }
    }

    public interface Callback {
        void selected(DwarfWithDistance model);
    }
}