package com.magomed.gamzatov.universalmarket.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.magomed.gamzatov.universalmarket.R;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FloatingActionButton fab;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private TextView navHeaderText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initToolbar();
        initFab();
        initListViewCategory();
        initNavHeader();
    }

    @Override
    protected void onStart(){
        super.onStart();

        SharedPreferences sPref = getSharedPreferences("cookies", MODE_PRIVATE);
        if(sPref.getString("cookie", "").equals("")){
            fab.hide();
        } else {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (fab != null) {
                        fab.show();
                    }
                }
            }, 300);
        }
        String email = sPref.getString("email", "");
        if(navHeaderText!=null && !email.equals("")) {
            navHeaderText.setText(email);
        }
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Категории");
        }
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (drawer != null) {
            drawer.setDrawerListener(toggle);
        }
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }
    }

    private void initFab() {
        fab = (FloatingActionButton) findViewById(R.id.fab);

        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, AddProduct.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void initListViewCategory() {
        ListView listViewCategory = (ListView) findViewById(R.id.listViewCategory);
        String[] list = {"Все", "Шорты", "Футболки", "Джинсы"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        if (listViewCategory != null) {
            listViewCategory.setAdapter(arrayAdapter);
            listViewCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(fab!=null){
                        fab.hide();
                    }
                    Intent intent = new Intent(MainActivity.this, ItemsList.class);
                    startActivity(intent);
                    overridePendingTransition(R.animator.push_down_in, R.animator.push_down_out);
                }
            });
        }
    }

    private void initNavHeader() {
        if (navigationView != null) {
            View headerView = navigationView.getHeaderView(0);
            LinearLayout navHeader = (LinearLayout) headerView.findViewById(R.id.navHeader);
            if (navHeader != null) {
                navHeader.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, Login.class);
                        startActivity(intent);
                        drawer.closeDrawer(GravityCompat.START);
                    }
                });
            }
            navHeaderText = (TextView) headerView.findViewById(R.id.navHeaderText);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            SharedPreferences sPref = getSharedPreferences("cookies", MODE_PRIVATE);
            SharedPreferences.Editor ed = sPref.edit();
            ed.putString("cookie", "");
            ed.apply();
            fab.hide();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void onBackPressed() {

        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }
}
