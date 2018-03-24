package com.example.ravindra.aawaztranslator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class home_screen extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_screen, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {

        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        String language[]={"US","Hindi","Spanish"};

        final int i=item.getItemId();

        if(i==R.id.select_dialog_listview){

            AlertDialog.Builder builder = new AlertDialog.Builder(home_screen.this);
            builder.setTitle("Choose Language:");


            builder.setItems(language, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            item.setIcon(R.drawable.us);
                            break;
                        case 1:
                            item.setIcon(R.drawable.india);break;
                        case 2:
                            item.setIcon(R.drawable.spain);
                            break;
                        default:break;

                    }
                }
            });


            AlertDialog dialog = builder.create();
            dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }


    public void add_delete_gesture(View view) {
        Log.d("asd", "add_delete_gesture: ");
        String activity[]={"Add Gesture","Edit Gesture","Delete Gesture"};
        AlertDialog.Builder builder = new AlertDialog.Builder(home_screen.this);
        builder.setTitle("Choose Action:");


        builder.setItems(activity, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        startActivity(new Intent(home_screen.this,add_gesture.class));
                        break;
                    case 1:
                        startActivity(new Intent(home_screen.this,edit_gesture.class));
                        break;
                    case 2:
                        startActivity(new Intent(home_screen.this,delete_gesture.class));
                        break;
                    default:break;

                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void tutorial_activity(View view) {
        startActivity(new Intent(home_screen.this,tutorial_activity.class));
    }
}
