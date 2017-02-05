package com.magomed.gamzatov.universalmarket.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.magomed.gamzatov.universalmarket.R;
import com.magomed.gamzatov.universalmarket.activity.ProductInfo;
import com.magomed.gamzatov.universalmarket.network.VolleySingleton;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RVAdapter extends RecyclerView.Adapter{

    private ImageLoader imageLoader;
    static public final int VIEW_ITEM = 1;
    static public final int VIEW_PROG = 0;
    private Context context;

    public static class PersonViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView itemName;
        TextView itemDescription;
        ImageView itemPhoto;
        View itemView;
        PersonViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            itemName = (TextView)itemView.findViewById(R.id.item_name);
            itemDescription = (TextView)itemView.findViewById(R.id.item_description);
            itemPhoto = (ImageView)itemView.findViewById(R.id.item_photo);
            this.itemView = itemView;
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;
        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar1);
        }
    }

    List<Item> items;

    public RVAdapter(List<Item> item, Context context){
        this.context = context;
        this.items = item;
        VolleySingleton volleySingleton = VolleySingleton.getsInstance();
        imageLoader = volleySingleton.getImageLoader();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item, parent, false);

            vh = new PersonViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.progressbar_item, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof PersonViewHolder) {
            ((PersonViewHolder)holder).itemName.setText(items.get(position).name);
            ((PersonViewHolder)holder).itemDescription.setText(items.get(position).description);
            String url = items.get(position).photoUrl;
            if (!"".equals(url)) {
                Picasso.with(context).load(url)
                        .error(R.mipmap.no_image)
                        .into(((PersonViewHolder)holder).itemPhoto);
//                imageLoader.get(url, new ImageLoader.ImageListener() {
//                    @Override
//                    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
//                        ((PersonViewHolder)holder).itemPhoto.setImageBitmap(response.getBitmap());
//                    }
//
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        ((PersonViewHolder)holder).itemPhoto.setImageResource(R.mipmap.no_image);
//                    }
//                });
            } else {
                ((PersonViewHolder)holder).itemPhoto.setImageResource(R.mipmap.no_image);
            }
        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

