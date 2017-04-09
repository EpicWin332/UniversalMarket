package com.magomed.gamzatov.universalmarket.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.magomed.gamzatov.universalmarket.R;
import com.magomed.gamzatov.universalmarket.adapter.Item;
import com.magomed.gamzatov.universalmarket.adapter.ItemClickSupport;
import com.magomed.gamzatov.universalmarket.adapter.RVAdapter;
import com.magomed.gamzatov.universalmarket.entity.Items;
import com.magomed.gamzatov.universalmarket.network.DictionaryQuery;
import com.magomed.gamzatov.universalmarket.network.RegistrationQuery;
import com.magomed.gamzatov.universalmarket.network.ServiceGenerator;
import com.magomed.gamzatov.universalmarket.network.ShopsQuery;
import com.magomed.gamzatov.universalmarket.network.VolleySingleton;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;

public class ItemsList extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, NavigationView.OnNavigationItemSelectedListener {

    private static final int MY_SOCKET_TIMEOUT_MS = 60_000;
    private List<Item> items = new ArrayList<>();
    private AVLoadingIndicatorView avLoadingIndicatorView;
    //private SwipeRefreshLayout mSwipeRefreshLayout;
    private WaveSwipeRefreshLayout mWaveSwipeRefreshLayout;
    private RecyclerView rv;
    private FloatingActionButton fab;
    private RequestQueue requestQueue;
    private RVAdapter adapter;
    Toolbar toolbar;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private int limit = 20;
    private int offset = 0;
    private String url = ServiceGenerator.API_BASE_URL+ ServiceGenerator.API_PREFIX_URL + "/products?limit="+limit+"&offset=";
    private boolean clickable = true;
    private boolean hideFab = false;
    private EditText brand;
    private EditText description;
    private EditText priceMin;
    private EditText priceMax;
    private EditText phone;
    private Spinner shop;
    private Spinner type;
    private List<Map<String, String>> shops = new ArrayList<>();
    private List<Map<String, String>> types = new ArrayList<>();

    private String brandCurrent = "";
    private String descriptionCurrent = "";
    private String priceMinCurrent = "";
    private String priceMaxCurrent = "";
    private String phoneCurrent = "";
    private String shopCurrent = "";
    private String typeCurrent = "";


    private boolean loading = true;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items_list);
        initToolbar("Все");
        initFab();

        brand = (EditText) findViewById(R.id.editBrand);
        description = (EditText) findViewById(R.id.editDescription);
        priceMin = (EditText) findViewById(R.id.editMinPrice);
        priceMax = (EditText) findViewById(R.id.editMaxPrice);
        phone = (EditText) findViewById(R.id.editPhone);
        shop = (Spinner) findViewById(R.id.editShop);
        type = (Spinner) findViewById(R.id.editType);

        Map<String, String> empty =  new HashMap<>();
        empty.put("id", "-1");
        empty.put("name", "");
        shops.add(empty);
        types.add(empty);

        mWaveSwipeRefreshLayout = (WaveSwipeRefreshLayout) findViewById(R.id.main_swipe);
        if (mWaveSwipeRefreshLayout != null) {
            mWaveSwipeRefreshLayout.setColorSchemeColors(Color.WHITE, Color.WHITE);
            mWaveSwipeRefreshLayout.setWaveColor(Color.argb(255,49,67,91));
        }
        mWaveSwipeRefreshLayout.setOnRefreshListener(new WaveSwipeRefreshLayout.OnRefreshListener() {
            @Override public void onRefresh() {
                // Do work to refresh the list here.
                moreData(false);
            }
        });

        rv = (RecyclerView) findViewById(R.id.rv);
        if (rv != null) {
            rv.setHasFixedSize(true);
        }
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rv.setLayoutManager(gridLayoutManager);
        adapter = new RVAdapter(items, this);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch(adapter.getItemViewType(position)){
                    case RVAdapter.VIEW_ITEM:
                        return 1;
                    case RVAdapter.VIEW_PROG:
                        return 2; //number of columns of the grid
                    default:
                        return -1;
                }
            }
        });

        rv.setAdapter(adapter);
        avLoadingIndicatorView = (AVLoadingIndicatorView) findViewById(R.id.avloadingIndicatorView);

        requestQueue = VolleySingleton.getsInstance().getRequestQueue();
        startAnim();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url+offset, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response", response);
                jsonParser(response, false);
                stopAnim();
                adapter.notifyItemInserted(items.size());

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                stopAnim();
                Toast.makeText(getApplicationContext(), "Error " + error, Toast.LENGTH_LONG).show();
            }
        });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);

        ItemClickSupport.addTo(rv).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                if(clickable) {
                    clickable = false;
                    Intent intent = new Intent(ItemsList.this, ProductInfo.class);
                    intent.putExtra("brand", items.get(position).getName());
                    intent.putExtra("type", items.get(position).getType());
                    intent.putExtra("price", items.get(position).getDescription());
                    intent.putExtra("id", items.get(position).getId());
                    startActivity(intent);
                    overridePendingTransition(R.animator.push_down_in, R.animator.push_down_out);
                }
            }
        });

        rv.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if(dy > 0) //check for scroll down
                {
                    visibleItemCount = gridLayoutManager.getChildCount();
                    totalItemCount = gridLayoutManager.getItemCount();
                    pastVisiblesItems = gridLayoutManager.findFirstVisibleItemPosition();

                    if (loading)
                    {
                        if ( (visibleItemCount + pastVisiblesItems) >= totalItemCount)
                        {
                            loading = false;
                            Log.v("...", "Last Item Wow !");
                            moreData(true);
                        }
                    }
                }

                if(
                        !hideFab &&
                                fab != null) {
                    if (dy > 0 && fab.isShown()) {
                        fab.hide();
                    }
                    else if (dy < 0 && !fab.isShown()) {
                        fab.show();
                    }
                }
            }
        });

        setFilerListener();

    }

    private void setFilerListener() {
        Button resetAll = (Button) findViewById(R.id.buttonReset);
        Button accept = (Button) findViewById(R.id.buttonAccept);

        resetAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                brand.setText("");
                description.setText("");
                priceMin.setText("");
                priceMax.setText("");
                phone.setText("");
                if(shop.getSelectedItem() !=null)
                    shop.setSelection(0);
                if(type.getSelectedItem() !=null)
                    type.setSelection(0);
                brandCurrent = "";
                descriptionCurrent = "";
                priceMinCurrent = "";
                priceMaxCurrent = "";
                phoneCurrent = "";
                shopCurrent = "";
                typeCurrent = "";
                if(drawer.isDrawerOpen(GravityCompat.END)){
                    drawer.closeDrawer(GravityCompat.END);
                }
                startAnim();
                moreData(false);
            }
        });

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                brandCurrent = brand.getText().toString();
                descriptionCurrent = description.getText().toString();
                priceMinCurrent = priceMin.getText().toString();
                priceMaxCurrent = priceMax.getText().toString();
                phoneCurrent = phone.getText().toString();
                if(shop.getSelectedItem() != null)
                    shopCurrent = shops.get(((int) shop.getSelectedItemId())).get("name");
                if(type.getSelectedItem() != null)
                    typeCurrent = types.get(((int) type.getSelectedItemId())).get("name");

                if(drawer.isDrawerOpen(GravityCompat.END)){
                    drawer.closeDrawer(GravityCompat.END);
                }
                startAnim();
                moreData(false);
            }
        });

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View view, float v) {

            }

            @Override
            public void onDrawerOpened(View view) {

            }

            @Override
            public void onDrawerClosed(View view) {
                brand.setText(brandCurrent);
                description.setText(descriptionCurrent);
                priceMin.setText(priceMinCurrent);
                priceMax.setText(priceMaxCurrent);
                phone.setText(phoneCurrent);
            }

            @Override
            public void onDrawerStateChanged(int i) {

            }
        });

        ShopsQuery shopsQuery = ServiceGenerator.createService(ShopsQuery.class);

        shopsQuery.getShops().enqueue(new Callback<List<Map<String, String>>>() {
            @Override
            public void onResponse(Call<List<Map<String, String>>> call, retrofit2.Response<List<Map<String, String>>> response) {
                if(response.code()==200) {
                    Log.d("onResponse shops", response.body().toString());
                    shops.addAll(response.body());
                    List<String> shopNames = new ArrayList<>();
                    for (Map<String, String> item: shops) {
                        shopNames.add(item.get("name"));
                    }
                    shop.setAdapter(new ArrayAdapter<>(ItemsList.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            shopNames));
                }
                else {
                    Log.e("onResponse error shops:", response.code() + " " + response.message()+ " " + response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, String>>> call, Throwable t) {
                Log.d("onFailure shops", t.getMessage());
            }
        });


        DictionaryQuery dictionaryQuery = ServiceGenerator.createService(DictionaryQuery.class);

        dictionaryQuery.getDictionaries("types").enqueue(new Callback<List<Map<String, String>>>() {
            @Override
            public void onResponse(Call<List<Map<String, String>>> call, retrofit2.Response<List<Map<String, String>>> response) {
                if(response.code()==200) {
                    Log.d("onResponse types", response.body().toString());
                    types.addAll(response.body());
                    List<String> typeNames = new ArrayList<>();
                    for (Map<String, String> item: types) {
                        typeNames.add(item.get("name"));
                    }
                    type.setAdapter(new ArrayAdapter<>(ItemsList.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            typeNames));
                }
                else {
                    Log.e("onResponse error types:", response.code() + " " + response.message()+ " " + response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, String>>> call, Throwable t) {
                Log.d("onFailure types", t.getMessage());
            }
        });

    }

    @Override
    protected void onResume(){
        super.onResume();

        clickable = true;
    }

    private void initToolbar(String title) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(title);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_filter) {
            if (drawer.isDrawerOpen(GravityCompat.END)) {
                drawer.closeDrawer(GravityCompat.END);
            } else {
                drawer.openDrawer(GravityCompat.END);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initFab() {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.hide();
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ItemsList.this, AddProduct.class);
                    startActivity(intent);
                }
            });
            SharedPreferences sPref = getSharedPreferences("cookies", MODE_PRIVATE);
            if(!sPref.getString("cookie", "").equals("")) {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        fab.show();
                    }
                }, 300);
            } else {
                hideFab = true;
            }
        }
    }

    private void jsonParser(String response, boolean pagination){
        Gson gson = new Gson();
        ArrayList<Items> itemsLists = gson.fromJson(response, new TypeToken<ArrayList<Items>>(){}.getType());
        Log.d("json", gson.toJson(itemsLists));
        loading = true;
        if(!pagination) {
            items.clear();
        } else if(itemsLists.isEmpty()) {
            offset-=limit;
            loading = false;
        }
        for(Items itemsList:itemsLists){
            String imgUrl = itemsList.getImageUrls().isEmpty()? "" : "http://e455.azurewebsites.net/"+itemsList.getImageUrls().get(0);
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
    public void onRefresh() {
        moreData(false);
    }

    private void moreData(final boolean pagination) {
        Log.d("refresh", "start");
        if(pagination){
            offset+=limit;
            items.add(null);
            adapter.notifyItemInserted(items.size() - 1);
        } else {
            offset = 0;
            // начинаем показывать прогресс
            //mSwipeRefreshLayout.setRefreshing(true);
        }
        StringBuilder queryParams = new StringBuilder();
        if(!brandCurrent.isEmpty())
            queryParams.append("&brand=").append(brandCurrent);
        if(!descriptionCurrent.isEmpty())
            queryParams.append("&description=").append(descriptionCurrent);
        if(!priceMaxCurrent.isEmpty())
            queryParams.append("&priceMax=").append(priceMaxCurrent);
        if(!priceMinCurrent.isEmpty())
            queryParams.append("&priceMin=").append(priceMinCurrent);
        if(!phoneCurrent.isEmpty())
            queryParams.append("&shopPhone=").append(phoneCurrent);
        if(!shopCurrent.isEmpty())
            queryParams.append("&shopName=").append(shopCurrent);
        if(!typeCurrent.isEmpty())
            queryParams.append("&typeName=").append(typeCurrent);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url+offset+queryParams.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response", response);
                if(pagination) {
                    items.remove(items.size() - 1);
                    adapter.notifyItemRemoved(items.size());
                }
                jsonParser(response, pagination);
                Log.d("refresh", "end");
                mWaveSwipeRefreshLayout.setRefreshing(false);
                stopAnim();
                if(pagination) {
                    adapter.notifyItemInserted(items.size());
                } else {
                    adapter.notifyDataSetChanged();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("refresh", "end");
                mWaveSwipeRefreshLayout.setRefreshing(false);
                stopAnim();
                Toast.makeText(getApplicationContext(), "Error " + error, Toast.LENGTH_LONG).show();
                loading = true;
                if(pagination){
                    offset-=limit;
                    items.remove(items.size() - 1);
                    adapter.notifyItemRemoved(items.size());
                }
            }
        });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.END)){
            drawer.closeDrawer(GravityCompat.END);
        } else {
            if (fab != null) {
                fab.hide();
            }
            finish();
            overridePendingTransition(R.animator.back_in, R.animator.back_out);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return false;
    }
}
