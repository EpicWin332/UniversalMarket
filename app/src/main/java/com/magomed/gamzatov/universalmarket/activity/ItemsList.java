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
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.magomed.gamzatov.universalmarket.R;
import com.magomed.gamzatov.universalmarket.adapter.Item;
import com.magomed.gamzatov.universalmarket.adapter.ItemClickSupport;
import com.magomed.gamzatov.universalmarket.adapter.RVAdapter;
import com.magomed.gamzatov.universalmarket.entity.Items;
import com.magomed.gamzatov.universalmarket.network.ServiceGenerator;
import com.magomed.gamzatov.universalmarket.network.VolleySingleton;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

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
    private String url = ServiceGenerator.API_BASE_URL+ ServiceGenerator.API_PREFIX_URL + "/getProductsWithFilter?limit="+limit+"&offset=";
    private boolean clickable = true;
    private boolean hideFab = false;

    private boolean loading = true;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items_list);
        initToolbar("Все");
        initFab();

//        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
//        if (mSwipeRefreshLayout != null) {
//            mSwipeRefreshLayout.setOnRefreshListener(this);
//        }

        mWaveSwipeRefreshLayout = (WaveSwipeRefreshLayout) findViewById(R.id.main_swipe);
        if (mWaveSwipeRefreshLayout != null) {
            mWaveSwipeRefreshLayout.setColorSchemeColors(Color.WHITE, Color.WHITE);
            mWaveSwipeRefreshLayout.setWaveColor(Color.argb(255,49,67,91));
//            mWaveSwipeRefreshLayout.setWaveARGBColor(255,63,81,181);
//            mWaveSwipeRefreshLayout.setColorSchemeColors(R.color.colorWhite);
//            mWaveSwipeRefreshLayout.setColorSchemeResources(R.id.toolbar);
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
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url+offset, new Response.Listener<String>() {
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

        setFilerButtonsClickListener();

    }

    private void setFilerButtonsClickListener() {
        Button resetAll = (Button) findViewById(R.id.buttonReset);
        Button accept = (Button) findViewById(R.id.buttonAccept);

        resetAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(drawer.isDrawerOpen(GravityCompat.END)){
                    drawer.closeDrawer(GravityCompat.END);
                }
                moreData(false);
            }
        });

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(drawer.isDrawerOpen(GravityCompat.END)){
                    drawer.closeDrawer(GravityCompat.END);
                }
                moreData(false);
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
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
//            @Override
//            public boolean onOptionsItemSelected(MenuItem item) {
//                if (item != null && item.getItemId() == android.R.id.home) {
//                    if (drawer.isDrawerOpen(Gravity.RIGHT)) {
//                        drawer.closeDrawer(Gravity.RIGHT);
//                    } else {
//                        drawer.openDrawer(Gravity.RIGHT);
//                    }
//                }
//                return false;
//            }
//        };
//
//        if (drawer != null) {
//            drawer.setDrawerListener(toggle);
//        }
//        toggle.syncState();

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
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url+offset, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response", response);
                if(pagination) {
                    items.remove(items.size() - 1);
                    adapter.notifyItemRemoved(items.size());
                }
                jsonParser(response, pagination);
                Log.d("refresh", "end");
                //mSwipeRefreshLayout.setRefreshing(false);
                mWaveSwipeRefreshLayout.setRefreshing(false);
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
                //mSwipeRefreshLayout.setRefreshing(false);
                mWaveSwipeRefreshLayout.setRefreshing(false);
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
