package grupa.krasnale.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import grupa.krasnale.R;
import grupa.krasnale.models.DwarfModel;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.DwarfVH> {

    private List<DwarfModel> dwarfs = new ArrayList<>();
    private Callback callback;

    public SearchResultAdapter(Callback callback) {
        this.callback = callback;
    }

    @Override
    public DwarfVH onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.search_result_list_item, parent, false);
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

    public void setNewFound(List<DwarfModel> data) {
        dwarfs = data;
        notifyDataSetChanged();
    }

    class DwarfVH extends RecyclerView.ViewHolder {

        TextView name;
        TextView address;
        DwarfModel model;

        DwarfVH(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.name);
            address = (TextView) itemView.findViewById(R.id.address);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.selected(model);
                }
            });
        }

        void setModel(DwarfModel model) {
            this.model = model;
            name.setText(model.title);
            address.setText(model.adres);
        }
    }

    public interface Callback {
        void selected(DwarfModel model);
    }
}
