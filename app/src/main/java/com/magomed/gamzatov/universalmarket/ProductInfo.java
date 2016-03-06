package com.magomed.gamzatov.universalmarket;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.magomed.gamzatov.universalmarket.entity.Items;
import com.magomed.gamzatov.universalmarket.network.VolleySingleton;

import java.util.ArrayList;

public class ProductInfo extends AppCompatActivity {

    private Items items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_info);

        Intent intent = getIntent();
        String brand = intent.getStringExtra("brand");
        String type = intent.getStringExtra("type");
        String price = intent.getStringExtra("price");
        final int id = intent.getIntExtra("id", 0);
        initToolbar(type + " " + brand);

        TextView product_brand = (TextView) findViewById(R.id.product_brand);
        TextView product_type = (TextView) findViewById(R.id.product_type);
        TextView product_price = (TextView) findViewById(R.id.product_price);
        final TextView product_description = (TextView) findViewById(R.id.product_description);
        final TextView product_shop = (TextView) findViewById(R.id.product_shop);
        final TextView product_adress = (TextView) findViewById(R.id.product_adress);
        final TextView product_phone = (TextView) findViewById(R.id.product_phone);
        final ImageView imageView = (ImageView) findViewById(R.id.imageView);

        product_brand.setText(brand);
        product_type.setText(type);
        product_price.setText(price);
        //product_description.setText("" + id);

        String url = "http://e455.azurewebsites.net/TestTomcat-1.0-SNAPSHOT/getParticularProduct?id=" + id;

        RequestQueue requestQueue = VolleySingleton.getsInstance().getRequestQueue();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response", response);
                jsonParser(response);
                product_description.setText(items.getDescription());
                product_shop.setText(items.getShop().getName());
                product_adress.setText(items.getShop().getAddress());
                product_phone.setText(items.getShop().getPhone());

                String imgUrl = items.getImageUrls().isEmpty()? "" : "http://e455.azurewebsites.net"+items.getImageUrls().get(0);
                if(!"".equals(imgUrl)) {
                    ImageLoader imageLoader = VolleySingleton.getsInstance().getImageLoader();
                    imageLoader.get(imgUrl, new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                            imageView.setImageBitmap(response.getBitmap());
                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            imageView.setImageResource(R.mipmap.no_image);
                        }
                    });
                } else {
                    imageView.setImageResource(R.mipmap.no_image);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Error " + error, Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(stringRequest);

    }

    private void jsonParser(String response){
        Gson gson = new Gson();
        items = gson.fromJson(response, Items.class);
        Log.d("json", gson.toJson(items));
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

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.animator.back_in, R.animator.back_out);
    }

}
