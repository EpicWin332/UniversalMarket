package com.magomed.gamzatov.universalmarket.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.magomed.gamzatov.universalmarket.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initToolbar();
        initFab();
        initListViewCategory();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("Категории");
        }
        setSupportActionBar(toolbar);
    }

    private void initFab() {
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

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

//                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//                        // previously visible view
//                        final View myView = findViewById(R.id.fab);
//
//                        int cx = (myView.getLeft() + myView.getRight()) / 2;
//                        int cy = (myView.getTop() + myView.getBottom()) / 2;
//
//                        int initialRadius = myView.getWidth();
//
//                        Animator anim =
//                                ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius, 0);
//
//                        anim.addListener(new AnimatorListenerAdapter() {
//                            @Override
//                            public void onAnimationEnd(Animator animation) {
//                                super.onAnimationEnd(animation);
//                                myView.setVisibility(View.INVISIBLE);
//                            }
//                        });
//
//                        anim.start();
//                    }

                    Intent intent = new Intent(MainActivity.this, ItemsList.class);
                    startActivity(intent);
                    overridePendingTransition(R.animator.push_down_in, R.animator.push_down_out);
                }
            });
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
