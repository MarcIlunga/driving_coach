package starthack.drivingcoach;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.here.sdk.analytics.internal.HttpClient;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        DataAnalysis.init();
        String toPrint = DataAnalysis.totalScore + "";
        ((TextView)findViewById(R.id.MAIN_ScoreText)).setText(toPrint);


        String distPrint = DataAnalysis.totalDistance + "m";
        ((TextView)findViewById(R.id.DAILY_distance)).setText(distPrint);

        String ecolo = (FileWriterReader.readFile("ecolo.score", MyApplication.getContext()));
        ((TextView)findViewById(R.id.DAILY_ecology)).setText(ecolo);
    }

    void onFabClick (View view) {
        Intent mapActivity = new Intent(this, MapActivity.class);
        startActivity(mapActivity);
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
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void login(View view) {
        final CardView cardView = ((CardView) findViewById(R.id.MAIN_Login));
        String pseudo = ((TextView)findViewById(R.id.LOGIN_Username)).getText().toString();
        String mail = ((TextView)findViewById(R.id.LOGIN_Email)).getText().toString();
        Log.d("<<<<", pseudo + " aaand " + mail);
        if (isValidData(pseudo, mail)) {
            Log.d("<<<<", "The slide animation should happen");
            // TODO: Envoyer à Marc les infos et récupérer l'id pour l'écrire dans un fichier ensuite.
            AnimationSet s = new AnimationSet(false);
            Animation slideRight = AnimationUtils.loadAnimation(MyApplication.getContext(), R.anim.slide_right);
            s.addAnimation(slideRight);

            s.setFillAfter(true);
            cardView.startAnimation(s);

            cardView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    cardView.setVisibility(View.GONE);
                }
            }, 800);

            try {
                String toSend = pseudo + "|" + mail;
                byte[] postData = toSend.getBytes();
                int postDataLength = postData.length;
                // TODO !!!
                String request = "http://172.27.3.7";

                URL url = new URL(request);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setInstanceFollowRedirects(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("charset", "utf-8");
                conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
                conn.setUseCaches(false);
                try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                    wr.write(postData);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Log.d("<<<<<", "Data given is not valid !");
        }

    }

    private boolean isValidData(String pseudo, String mail) {
        return (pseudo!= null && !pseudo.equals("")) && (mail!= null && !mail.equals("") && mail.contains("@") && mail.contains("."));
    }

}
