package com.magomed.gamzatov.universalmarket.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.magomed.gamzatov.universalmarket.R;
import com.magomed.gamzatov.universalmarket.entity.Items;
import com.magomed.gamzatov.universalmarket.network.VolleySingleton;
import com.wang.avi.AVLoadingIndicatorView;

public class ProductInfo extends AppCompatActivity {

    private Items items;
    private static final int MY_SOCKET_TIMEOUT_MS = 60_000;
    private AVLoadingIndicatorView avLoadingIndicatorView;
    private boolean imageLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_info);

        // Postpone the transition until the window's decor view has
        // finished its layout.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
            final View decor = getWindow().getDecorView();
            decor.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    decor.getViewTreeObserver().removeOnPreDrawListener(this);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        startPostponedEnterTransition();
                    }
                    return true;
                }
            });
        }

        Intent intent = getIntent();
        final String brand = intent.getStringExtra("brand");
        String type = intent.getStringExtra("type");
        String price = intent.getStringExtra("price");
        final int id = intent.getIntExtra("id", 0);
        initToolbar(type + " " + brand);

        String url = "http://e455.azurewebsites.net/TestTomcat-1.0-SNAPSHOT/getParticularProduct?id=" + id;

        TextView product_brand = (TextView) findViewById(R.id.product_brand);
        TextView product_type = (TextView) findViewById(R.id.product_type);
        TextView product_price = (TextView) findViewById(R.id.product_price);
        final TextView product_description = (TextView) findViewById(R.id.product_description);
        final TextView product_shop = (TextView) findViewById(R.id.product_shop);
        final TextView product_adress = (TextView) findViewById(R.id.product_adress);
        final TextView product_phone = (TextView) findViewById(R.id.product_phone);
        final ImageView imageView = (ImageView) findViewById(R.id.imageView);
        avLoadingIndicatorView = (AVLoadingIndicatorView) findViewById(R.id.avloadingIndicatorView);

        if (product_brand != null) {
            product_brand.setText(brand);
        }
        if (product_type != null) {
            product_type.setText(type);
        }
        if (product_price != null) {
            product_price.setText(price);
        }

        volleyRequest(product_description, product_shop, product_adress, product_phone, imageView, url);

        if (imageView != null) {
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(imageLoaded) {

                        Intent intent = new Intent(ProductInfo.this, ImagePreview.class);
                        intent.putExtra("image", "http://e455.azurewebsites.net/" + items.getImageUrls().get(0));

////                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
////                            View image = v.findViewById(R.id.imageView);
////                            Pair<View, String> pair = Pair.create(image, image.getTransitionName());
////                            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ProductInfo.this, pair);
////                            startActivity(intent, options.toBundle());
////                        } else {
                             startActivity(intent);
////                        }
                    }
                }
            });
        }
    }

    private void volleyRequest(final TextView product_description, final TextView product_shop, final TextView product_adress, final TextView product_phone, final ImageView imageView, String url) {
        RequestQueue requestQueue = VolleySingleton.getsInstance().getRequestQueue();
        startAnim();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response", response);
                jsonParser(response);
                product_description.setText(items.getDescription());
                product_shop.setText(items.getShop().getName());
                product_adress.setText(items.getShop().getAddress());
                product_phone.setText(items.getShop().getPhone());

                String imgUrl = items.getImageUrls().isEmpty()? "" : "http://e455.azurewebsites.net/"+items.getImageUrls().get(0);
                if(!"".equals(imgUrl)) {
                    final ImageLoader imageLoader = VolleySingleton.getsInstance().getImageLoader();
                    imageLoader.get(imgUrl, new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                            stopAnim();
                            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            imageView.setImageBitmap(response.getBitmap());
                            imageLoaded = true;
                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            stopAnim();
                            imageView.setImageResource(R.mipmap.no_image);
                        }
                    });
                } else {
                    stopAnim();
                    imageView.setImageResource(R.mipmap.no_image);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Error " + error, Toast.LENGTH_LONG).show();
                stopAnim();
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    private void jsonParser(String response){
        Gson gson = new Gson();
        items = gson.fromJson(response, Items.class);
        Log.d("json", gson.toJson(items));
    }

    private void initToolbar(String title) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            finish();
            overridePendingTransition(R.animator.back_in, R.animator.back_out);
        } else {
            supportFinishAfterTransition();
            super.onBackPressed();
        }
    }

}
