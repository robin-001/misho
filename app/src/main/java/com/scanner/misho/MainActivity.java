package com.scanner.misho;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jmrtd.lds.icao.MRZInfo;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.scanner.misho.VolleySingleton.getInstance;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener{

    public static final String API_TOKEN = "";
    private static final int PERM_REQUEST_CODE = 9001;
    public static RecyclerView documentList;
    public static LinearLayoutManager layoutManager;
    public static DocumentListAdapter adapter;
    public static ArrayList<Document> documents;
    private AppBarConfiguration mAppBarConfiguration;
    private static final int PASSPORT_MRZ_CAPTURE = 9002;
    private static final int NID_BARCODE_CAPTURE = 9003;
    private static final int DRIVER_BARCODE_CAPTURE = 9004;
    private String token="";
    private static final String TAG = "MainActivity";
    private SQLiteDocumentDatabaseHandler db;
    boolean isFABVisible=false;

    //RelativeLayout nidFAB,driverPermitFAB,passportFAB;
    FloatingActionButton mainFAB,nidFAB,driverPermitFAB,passportFAB;
    Animation nid_show_fab,nid_hide_fab,passport_show_fab,passport_hide_fab,driver_permit_show_fab,driver_permit_hide_fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setNavigationViewListener();

        new GetProfileTask().execute();

        requestLocationPermission();

        registerReceiver(new NetworkStateChecker(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        db = new SQLiteDocumentDatabaseHandler(this);
        // Log.d(TAG, db.getTableAsString("Documents"));
        try {
            documents = db.allDocuments();
        } catch (ParseException e) {
            handleException(e);
            Log.e(TAG, e.getMessage());
        }

        adapter = new DocumentListAdapter(documents);
        adapter.setOnItemClickListener(new DocumentListAdapter.ListItemClickListener() {
            @Override
            public void onListItemClick(int position) {
                Log.d(TAG,"Clicked"+position);
            }
        });

        findViewById(R.id.read_barcode).setOnClickListener(this);//start scanning id

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_settings, R.id.nav_logout)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        //set floating buttons
        mainFAB = findViewById(R.id.read_barcode);
        passportFAB = findViewById(R.id.passportFAB);
        nidFAB = findViewById(R.id.nidFAB);
        driverPermitFAB = findViewById(R.id.driverPermitFAB);

        //Animations
        passport_show_fab = AnimationUtils.loadAnimation(getApplication(), R.anim.passport_fab_show);
        passport_hide_fab = AnimationUtils.loadAnimation(getApplication(), R.anim.passport_fab_hide);

        nid_show_fab = AnimationUtils.loadAnimation(getApplication(), R.anim.nid_fab_show);
        nid_hide_fab = AnimationUtils.loadAnimation(getApplication(), R.anim.nid_fab_hide);

        driver_permit_show_fab = AnimationUtils.loadAnimation(getApplication(), R.anim.driver_permit_fab_show);
        driver_permit_hide_fab = AnimationUtils.loadAnimation(getApplication(), R.anim.driver_permit_fab_hide);

        //onclick listeners
        passportFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Scan Passport", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                Intent intent = new Intent(getApplicationContext(), CaptureActivity.class);//change to pasportcapture activity
                startActivityForResult(intent, PASSPORT_MRZ_CAPTURE);
                Log.d("Passport FAB","Clicked");
            }
        });


        nidFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
                intent.putExtra(BarcodeCaptureActivity.UseFlash, false);
                startActivityForResult(intent, NID_BARCODE_CAPTURE);
                Snackbar.make(v, "Scan National ID", Snackbar.LENGTH_LONG).setAction("Action", null).show();

            }
        });

        driverPermitFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Scan Driver's License", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                Intent intent = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
                intent.putExtra(BarcodeCaptureActivity.UseFlash, false);
                startActivityForResult(intent, DRIVER_BARCODE_CAPTURE);
                Snackbar.make(v, "Scan National ID", Snackbar.LENGTH_LONG).setAction("Action", null).show();

            }
        });


    }

    private void handleUncaughtException(Thread thread, final Throwable e) {
        new Thread(){
            public void run(){
                Looper.prepare();
                handleException((Exception) e);
                Looper.loop();
            }
        }.start();

        try {

            Thread.sleep(4000);
        }
        catch(InterruptedException ie){
            handleException((Exception) e);
        }
        System.exit(2);
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    PERM_REQUEST_CODE);
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.read_barcode){
            toggleFAB(isFABVisible);
        }
    }

    @SuppressLint("StringFormatInvalid")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        Document mDocument = null;
        String rawData =null;
        if (requestCode == PASSPORT_MRZ_CAPTURE) {
            Log.d(TAG,"PASSPORT CAPTURE ---");
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    rawData = data.getStringExtra(CaptureActivity.MRZ_RESULT);
                    try {
                        mDocument = new Document(Document.PASSPORT,rawData);
                    } catch (Exception e) {
                        handleException(e);
                        e.printStackTrace();
                    }

                } else {
                    Snackbar.make(getWindow().getDecorView().getRootView(),R.string.barcode_failure,Snackbar.LENGTH_LONG).show();
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                Snackbar.make(getWindow().getDecorView().getRootView(),String.format(getString(R.string.mrz_failure),
                        CommonStatusCodes.getStatusCodeString(resultCode)),Snackbar.LENGTH_LONG).show();
            }
        }

        else if (requestCode == NID_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    rawData =barcode.displayValue;
                    try {
                        mDocument = new Document(Document.NATIONAL_ID, rawData);
                    } catch (Exception e) {
                        handleException(e);
                        e.printStackTrace();
                    }

                } else {
                    Snackbar.make(getWindow().getDecorView().getRootView(),R.string.barcode_failure,Snackbar.LENGTH_LONG).show();
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                Snackbar.make(getWindow().getDecorView().getRootView(),String.format(getString(R.string.barcode_failure),
                        CommonStatusCodes.getStatusCodeString(resultCode)),Snackbar.LENGTH_LONG).show();
            }
        }

        else   if (requestCode == DRIVER_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    String raw_data = barcode.displayValue;
                    Log.d(TAG,barcode.displayValue);
                    //send raw data to server
                    Snackbar.make(getWindow().getDecorView().getRootView(),R.string.sending,Snackbar.LENGTH_LONG).show();
                    saveToServer("DRIVER DATA >> "+raw_data);

                } else {
                    Snackbar.make(getWindow().getDecorView().getRootView(),R.string.barcode_failure,Snackbar.LENGTH_LONG).show();
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                Snackbar.make(getWindow().getDecorView().getRootView(),String.format(getString(R.string.barcode_failure),
                        CommonStatusCodes.getStatusCodeString(resultCode)),Snackbar.LENGTH_LONG).show();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        //process document
        if (mDocument == null) {
            Snackbar.make(getWindow().getDecorView().getRootView(), getString(R.string.unknown, rawData), Snackbar.LENGTH_LONG).show();
            return;
        }
        //add imei,lat,lng,user_id
        mDocument.setImei(Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID));

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location==null){
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if(location==null){
                mDocument.setLatitude("0.00");
                mDocument.setLongitude("0.0");
            }
            else{
                mDocument.setLatitude(String.valueOf(location.getLatitude()));
                mDocument.setLongitude(String.valueOf(location.getLongitude()));
            }
        }else{
            mDocument.setLatitude(String.valueOf(location.getLatitude()));
            mDocument.setLongitude(String.valueOf(location.getLongitude()));
        }

        int documentId = db.addDocument(mDocument);
        if(documentId>0){
            documents.add(mDocument);
            adapter.notifyDataSetChanged();
            documentList.scrollToPosition(0);
            try {
                saveToServer(db.getDocument(documentId));
            } catch (ParseException e) {
                handleException(e);
                e.printStackTrace();
            }
            Snackbar.make(getWindow().getDecorView().getRootView(),R.string.document_saved,Snackbar.LENGTH_LONG).show();

            //got to Document activity
            Intent intent = new Intent(this,DocumentActivity.class);
            intent.putExtra("documentId",documentId);
            startActivity(intent);
        }
        else{
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.app_name)
                    .setMessage(R.string.duplicate_id)
                    .setPositiveButton(R.string.ok, listener)
                    .show();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERM_REQUEST_CODE: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission denied to get your location", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void saveToServer(final Document document) {
        final SharedPreferences prefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        String endpoint = getString(R.string.serverUrl)+"api/documents";
        Log.d(TAG,endpoint);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, endpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            Log.d(TAG,response);
                            if (!obj.getBoolean("error")) {
                                document.setSynced(true);
                                db =  new SQLiteDocumentDatabaseHandler(getApplicationContext());
                                db.updateDocument(document);

                            } else {
                                //error in response
                                Log.d(TAG,response.toString());
                            }
                        } catch (JSONException e) {
                            handleException(e);
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG,error.toString());
                    }
                }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return document.toJSON().getBytes();
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer "+prefs.getString("access_token",""));
                return headers;
            }
        };
        getInstance(this).addToRequestQueue(stringRequest);
    }

    private void saveToServer(final String raw_data) {
        final SharedPreferences prefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        String endpoint = getString(R.string.serverUrl)+"api/logger";
        Log.d(TAG,endpoint);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, endpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            Log.d(TAG,response);
                            if (!obj.getBoolean("error")) {
                                //there was no error. process server response

                            } else {
                                //error in response
                                Log.d(TAG,response.toString());
                            }
                        } catch (JSONException e) {
                            handleException(e);
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG,error.toString());
                    }
                }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return raw_data.getBytes();
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer "+prefs.getString("access_token",""));
                return headers;
            }
        };
        getInstance(this).addToRequestQueue(stringRequest);
    }

    public void toggleFAB(boolean isVisible){
        if(isVisible){
            hideFAB();
            isFABVisible=false;
        }
        else{
            showFAB();
            isFABVisible=true;
        }
    }

    public void hideFAB(){

        FrameLayout.LayoutParams layoutParams = null;
        //PASSPORT
        layoutParams = (FrameLayout.LayoutParams) passportFAB.getLayoutParams();
        layoutParams.rightMargin -= (int) (passportFAB.getWidth() * 0.25);
        layoutParams.bottomMargin -= (int) (passportFAB.getHeight() * 2);
        passportFAB.setLayoutParams(layoutParams);
        passportFAB.startAnimation(passport_hide_fab);
        passportFAB.setClickable(false);

        //NATIONAL ID
        layoutParams = (FrameLayout.LayoutParams) nidFAB.getLayoutParams();
        layoutParams.rightMargin -= (int) (nidFAB.getWidth() * 0.25);
        layoutParams.bottomMargin -= (int) (nidFAB.getHeight() * 3.5);
        nidFAB.setLayoutParams(layoutParams);
        nidFAB.startAnimation(nid_hide_fab);
        nidFAB.setClickable(false);

        //Driver
        layoutParams = (FrameLayout.LayoutParams) driverPermitFAB.getLayoutParams();
        layoutParams.rightMargin -= (int) (driverPermitFAB.getWidth() * 0.25);
        layoutParams.bottomMargin -= (int) (driverPermitFAB.getHeight() * 5);
        driverPermitFAB.setLayoutParams(layoutParams);
        driverPermitFAB.startAnimation(driver_permit_hide_fab);
        driverPermitFAB.setClickable(false);

    }

    public void showFAB(){

        FrameLayout.LayoutParams layoutParams = null;

        //PASSPORT
        layoutParams = (FrameLayout.LayoutParams) passportFAB.getLayoutParams();
        layoutParams.rightMargin += (int) (passportFAB.getWidth() * 0.25);
        layoutParams.bottomMargin += (int) (passportFAB.getHeight() * 2);
        passportFAB.setLayoutParams(layoutParams);
        passportFAB.startAnimation(passport_show_fab);
        passportFAB.setClickable(true);

        //NATIONAL ID
        layoutParams = (FrameLayout.LayoutParams) nidFAB.getLayoutParams();
        layoutParams.rightMargin += (int) (nidFAB.getWidth() * 0.25);
        layoutParams.bottomMargin += (int) (nidFAB.getHeight() * 3.5);
        nidFAB.setLayoutParams(layoutParams);
        nidFAB.startAnimation(nid_show_fab);
        nidFAB.setClickable(true);

        //Driver
        layoutParams = (FrameLayout.LayoutParams) driverPermitFAB.getLayoutParams();
        layoutParams.rightMargin += (int) (driverPermitFAB.getWidth() * 0.25);
        layoutParams.bottomMargin += (int) (driverPermitFAB.getHeight() * 5);
        driverPermitFAB.setLayoutParams(layoutParams);
        driverPermitFAB.startAnimation(driver_permit_show_fab);
        driverPermitFAB.setClickable(true);
    }

    public  void handleException(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        final String sStackTrace = sw.toString();
        saveToServer(sStackTrace);
    }

    public JSONObject getProfile(){
        JSONObject obj = null;
        Uri.Builder builder = new Uri.Builder();
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        final Map<String,String> params = new HashMap<String, String>();
        params.put("param1","testparam");
        Log.d(TAG,"Get Profile Request: "+params.toString());
        // encode parameters
        Iterator entries = params.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            builder.appendQueryParameter(entry.getKey().toString(), entry.getValue().toString());
            entries.remove();
        }
        String requestBody = builder.build().getEncodedQuery();

        HttpURLConnection conn = null;
        try {
            Log.d(TAG,getString(R.string.serverUrl)+sharedPreferences.getString("profile",""));
            URL url = new URL(getString(R.string.serverUrl)+sharedPreferences.getString("profile",""));
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestProperty ("Authorization", "Bearer "+sharedPreferences.getString("access_token",""));
            conn.setRequestMethod("GET");
            //conn.setDoInput(true);
            //conn.setDoOutput(true);
            //InputStream input = new BufferedInputStream(conn.getInputStream());

            // handle error response code it occurs
            int responseCode = conn.getResponseCode();
            InputStream inputStream;
            if (200 <= responseCode && responseCode <= 299) {
                inputStream = conn.getInputStream();
            } else {
                inputStream = conn.getErrorStream();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder response = new StringBuilder();
            String currentLine;

            while ((currentLine = in.readLine()) != null) {
                response.append(currentLine);
            }
            in.close();
            Log.d(TAG, response.toString());

            obj = new JSONObject(response.toString());
            SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).edit();
            if (obj.has("id")) {
                editor.putString("name", obj.getString("name"));
                editor.putString("email", obj.getString("email"));
                editor.putString("phone", obj.getString("phone"));
                editor.putString("sub_class", obj.getString("sub_class"));

            }
            else{
                editor.putString("error", obj.getString("error"));
                editor.putString("error_description", obj.getString("error_description"));
            }
            editor.commit();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException ex) {
            ex.printStackTrace();
        } finally {
            conn.disconnect();
        }

        return obj;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        Log.d(TAG, String.valueOf(item.getItemId()));
        switch (item.getItemId()) {

            case R.id.nav_logout: {
                Toast.makeText(MainActivity.this,"Logout",Toast.LENGTH_LONG).show();
                break;
            }
        }

        return true;
    }

    private void setNavigationViewListener() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public class GetProfileTask extends AsyncTask<String, Void, JSONObject> {
        private static final String TAG = "GetProfile";
        AlertDialog alertDialog;
        public void onPreExecute() {

        }

        public JSONObject doInBackground(String... params) {
            return getProfile();

        }

        @Override
        protected void onPostExecute(JSONObject profile){
            SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
            //check if login was successful
            if(profile.has("error")){
                //display error message or retry for x times
            }
            else{
                Log.i(TAG,"Fetched profile successfully");

                //user details
                TextView nameTextView = findViewById(R.id.profile_name);
                TextView emailTextView = findViewById(R.id.profile_email);

                try {
                    nameTextView.setText(profile.getString("name"));
                    emailTextView.setText(profile.getString("email"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}