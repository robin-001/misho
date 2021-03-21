package com.scanner.misho;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.scanner.misho.VolleySingleton.getInstance;

public class NetworkStateChecker extends BroadcastReceiver {

    //context and database helper object
    private Context context;
    private SQLiteDocumentDatabaseHandler db;
    private static String TAG = "NetworkStateChangeChecker";


    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;

        db = new SQLiteDocumentDatabaseHandler(context);

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        //if there is a network
        if (activeNetwork != null) {
            //if connected to wifi or mobile data plan
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {

                //getting all the unsynced names
                ArrayList<Document> documents=null;
                try {
                     documents = db.getUnsyncedDocuments();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (documents.size()>0) {
                   for(int i=0;i<documents.size();i++){
                       saveToServer(documents.get(i));
                   }
                }
            }
        }
    }

    private void saveToServer(final Document document) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, context.getResources().getString(R.string.serverUrl)+"api/documents",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                document.setSynced(true);
                                db =  new SQLiteDocumentDatabaseHandler(context);
                                db.updateDocument(document);
                               // Log.d(TAG,response);
                            } else {
                                //error in response
                                Log.d(TAG,response.toString());
                            }
                        } catch (JSONException e) {
                           Log.d(TAG,e.getMessage());
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
                //Log.d(TAG,document.toString());
                return document.toJSON().getBytes();
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer: "+MainActivity.API_TOKEN);
                return headers;
            }
        };
        getInstance(context).addToRequestQueue(stringRequest);
    }

}