package com.magomed.gamzatov.universalmarket.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.magomed.gamzatov.universalmarket.R;
import com.magomed.gamzatov.universalmarket.network.VolleySingleton;

import java.util.List;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PersonViewHolder>{

    private ImageLoader imageLoader;

    public static class PersonViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView itemName;
        TextView itemDescription;
        ImageView itemPhoto;
        PersonViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            itemName = (TextView)itemView.findViewById(R.id.item_name);
            itemDescription = (TextView)itemView.findViewById(R.id.item_description);
            itemPhoto = (ImageView)itemView.findViewById(R.id.item_photo);
        }
    }

    List<Item> items;

    public RVAdapter(List<Item> item){
        this.items = item;
        VolleySingleton volleySingleton = VolleySingleton.getsInstance();
        imageLoader = volleySingleton.getImageLoader();
    }

    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new PersonViewHolder(v);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(final PersonViewHolder holder, int position) {
        holder.itemName.setText(items.get(position).name);
        holder.itemDescription.setText(items.get(position).description);
        String url = items.get(position).photoUrl;

        imageLoader.get(url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                holder.itemPhoto.setImageBitmap(response.getBitmap());
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

