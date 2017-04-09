package com.magomed.gamzatov.universalmarket.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
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
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.google.gson.Gson;
import com.magomed.gamzatov.universalmarket.R;
import com.magomed.gamzatov.universalmarket.entity.Items;
import com.magomed.gamzatov.universalmarket.network.ServiceGenerator;
import com.magomed.gamzatov.universalmarket.network.VolleySingleton;
import com.wang.avi.AVLoadingIndicatorView;

public class ProductInfo extends AppCompatActivity implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {

    private Items items;
    private static final int MY_SOCKET_TIMEOUT_MS = 60_000;
    private AVLoadingIndicatorView avLoadingIndicatorView;
    private boolean imageLoaded = false;
    private SliderLayout sliderShow;

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

        String url = ServiceGenerator.API_BASE_URL + ServiceGenerator.API_PREFIX_URL + "/products/" + id;

        TextView product_brand = (TextView) findViewById(R.id.product_brand);
        TextView product_type = (TextView) findViewById(R.id.product_type);
        TextView product_price = (TextView) findViewById(R.id.product_price);
        final TextView product_description = (TextView) findViewById(R.id.product_description);
        final TextView product_shop = (TextView) findViewById(R.id.product_shop);
        final TextView product_adress = (TextView) findViewById(R.id.product_adress);
        final TextView product_phone = (TextView) findViewById(R.id.product_phone);

        sliderShow = (SliderLayout) findViewById(R.id.slider);
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

        volleyRequest(product_description, product_shop, product_adress, product_phone, url);
    }

    private void volleyRequest(final TextView product_description, final TextView product_shop, final TextView product_adress, final TextView product_phone, String url) {
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
                stopAnim();

                for (String urlImg : items.getImageUrls()) {
                    String imgUrl = ServiceGenerator.API_BASE_URL + "/" + urlImg;
                    // initialize a SliderLayout
                    sliderShow.addSlider(getSlider().image(imgUrl));
                    sliderShow.stopAutoCycle();
                    imageLoaded = true;
                }
                if(items.getImageUrls().isEmpty()) {
                    // initialize a SliderLayout
                    sliderShow.addSlider(getSlider().image(R.mipmap.no_image));
                }
                sliderShow.stopAutoCycle();
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

    private BaseSliderView getSlider() {
        TextSliderView textSliderView = new TextSliderView(ProductInfo.this);
        textSliderView
                .description("")
                .setScaleType(BaseSliderView.ScaleType.FitCenterCrop)
                .setOnSliderClickListener(ProductInfo.this);
        //add your extra information
        textSliderView.bundle(new Bundle());
        textSliderView.getBundle()
                .putString("extra", "");

        return textSliderView;
    }

    private void jsonParser(String response) {
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
        if (getSupportActionBar() != null)
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


    private void startAnim() {
        avLoadingIndicatorView.setVisibility(View.VISIBLE);
    }

    private void stopAnim() {
        avLoadingIndicatorView.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            finish();
            overridePendingTransition(R.animator.back_in, R.animator.back_out);
//        } else {
//            supportFinishAfterTransition();
//            super.onBackPressed();
//        }
    }

    @Override
    protected void onStop() {
        if (sliderShow != null) sliderShow.stopAutoCycle();
        super.onStop();
    }

    @Override
    public void onSliderClick(BaseSliderView slider) {
        Log.d("onSliderClick", "onSliderClick " + slider.getUrl());
        if (imageLoaded) {
            Intent intent = new Intent(ProductInfo.this, ImagePreview.class);
            intent.putExtra("image", slider.getUrl());
            startActivity(intent);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
