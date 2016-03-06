package com.magomed.gamzatov.universalmarket;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.magomed.gamzatov.universalmarket.adapter.Item;
import com.magomed.gamzatov.universalmarket.adapter.ItemClickSupport;
import com.magomed.gamzatov.universalmarket.adapter.RVAdapter;
import com.magomed.gamzatov.universalmarket.entity.Items;
import com.magomed.gamzatov.universalmarket.network.VolleySingleton;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

public class ItemsList extends AppCompatActivity {

    private List<Item> items = new ArrayList<>();
    private AVLoadingIndicatorView avLoadingIndicatorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items_list);
        initToolbar("Все");
        initFab();

        String url = "http://e455.azurewebsites.net/TestTomcat-1.0-SNAPSHOT/getProducts?limit=10&offset=0";
        final RecyclerView rv = (RecyclerView) findViewById(R.id.rv);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rv.setLayoutManager(gridLayoutManager);
        avLoadingIndicatorView = (AVLoadingIndicatorView) findViewById(R.id.avloadingIndicatorView);

        RequestQueue requestQueue = VolleySingleton.getsInstance().getRequestQueue();
        startAnim();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response", response);
                jsonParser(response);
                stopAnim();
                RVAdapter adapter = new RVAdapter(items);
                rv.setAdapter(adapter);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                stopAnim();
                Toast.makeText(getApplicationContext(), "Error " + error, Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(stringRequest);

        ItemClickSupport.addTo(rv).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Intent intent = new Intent(ItemsList.this, ProductInfo.class);
                intent.putExtra("brand", items.get(position).getName());
                intent.putExtra("type", items.get(position).getType());
                intent.putExtra("price", items.get(position).getDescription());
                intent.putExtra("id", items.get(position).getId());

                startActivity(intent);
                overridePendingTransition(R.animator.push_down_in, R.animator.push_down_out);
            }
        });
    }

    private void initToolbar(String title) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private void initFab() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void jsonParser(String response){
        Gson gson = new Gson();
        ArrayList<Items> itemsLists = gson.fromJson(response, new TypeToken<ArrayList<Items>>(){}.getType());
        Log.d("json", gson.toJson(itemsLists));
        items.clear();
        for(Items itemsList:itemsLists){
            String imgUrl = itemsList.getImageUrls().isEmpty()? "" : "http://e455.azurewebsites.net"+itemsList.getImageUrls().get(0);
            items.add(new Item(itemsList.getBrand(), ""+itemsList.getPrice()+" руб.", imgUrl, itemsList.getId(), itemsList.getType().getName()));
        }
    }

    private void startAnim(){
        avLoadingIndicatorView.setVisibility(View.VISIBLE);
    }

    private void stopAnim(){
        avLoadingIndicatorView.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.animator.back_in, R.animator.back_out);
    }


}
