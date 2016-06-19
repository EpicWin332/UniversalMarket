package com.magomed.gamzatov.universalmarket.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.magomed.gamzatov.universalmarket.network.VolleySingleton;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

public class ItemsList extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

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
    private int limit = 20;
    private int offset = 0;
    private String url = "http://e455.azurewebsites.net/TestTomcat-1.0-SNAPSHOT/getProducts?limit="+limit+"&offset=";
    private boolean clickable = true;

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
            mWaveSwipeRefreshLayout.setWaveColor(Color.argb(255,63,81,181));
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
        adapter = new RVAdapter(items);
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        View textViewName = v.findViewById(R.id.item_name);
                        View textViewPrice = v.findViewById(R.id.item_description);
                        View view = v.findViewById(R.id.cv);
                        View image = v.findViewById(R.id.item_photo);
                        View toolbar = findViewById(R.id.toolbar);

                        Pair<View, String> pair1 = Pair.create(textViewName, textViewName.getTransitionName());
                        Pair<View, String> pair2 = Pair.create(textViewPrice, textViewPrice.getTransitionName());
                        Pair<View, String> pair3 = Pair.create(view, view.getTransitionName());
                        Pair<View, String> pair4 = Pair.create(image, image.getTransitionName());
                        Pair<View, String> pair5 = Pair.create(toolbar, toolbar.getTransitionName());

                        ActivityOptionsCompat options = ActivityOptionsCompat.
                                makeSceneTransitionAnimation(ItemsList.this, pair2, pair1, pair3, pair4, pair5,
                                        Pair.create(findViewById(android.R.id.statusBarBackground), Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME),
                                        Pair.create(findViewById(android.R.id.navigationBarBackground), Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME));
                        startActivity(intent, options.toBundle());
                    } else {
                        startActivity(intent);
                        overridePendingTransition(R.animator.push_down_in, R.animator.push_down_out);
                    }
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

                if(fab != null) {
                    if (dy > 0 && fab.isShown()) {
                        fab.hide();
                    }
                    else if (dy < 0 && !fab.isShown()) {
                        fab.show();
                    }
                }
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
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url+offset, new Response.Listener<String>() {
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
        if(fab!=null){
            fab.hide();
        }
        finish();
        overridePendingTransition(R.animator.back_in, R.animator.back_out);
    }

}
