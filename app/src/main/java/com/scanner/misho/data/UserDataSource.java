package com.scanner.misho.data;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.scanner.misho.App;
import com.scanner.misho.R;
import com.scanner.misho.data.model.LoggedInUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
import static com.scanner.misho.VolleySingleton.getInstance;


public class UserDataSource {

    private static String TAG ="UserDataSource";
    private Context mContext;
    private SharedPreferences prefs;
    private String access_token;
    private String refresh_token;
    private int expires_in;
    private String client_id,client_secret;
    public int iRequests=0;
    public boolean volleyError=false;

    public UserDataSource(){
        this.mContext= App.getContext();
        SharedPreferences prefs = mContext.getSharedPreferences(mContext.getString(R.string.app_name), MODE_PRIVATE);
        this.client_id =  prefs.getString("CLIENT_ID", "");
        this.client_secret =  prefs.getString("CLIENT_SECRET", "");
        if(client_id.length()==0 || client_secret.length()==0){
            initialize();
        }

        this.access_token =  prefs.getString("access_token", "");
        this.refresh_token =  prefs.getString("refresh_token", "");
        this.expires_in =  Integer.valueOf(prefs.getString("expires_in", "3600"));
    }

    public void initialize(){
        Log.d(TAG,mContext.getResources().getString(R.string.serverUrl));
        StringRequest stringRequest = new StringRequest(Request.Method.GET, mContext.getResources().getString(R.string.serverUrl)+"api",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        iRequests--;
                        volleyError=false;
                        try {
                            JSONObject obj = new JSONObject(response);
                            Log.d(TAG,"init RESPONSE"+obj.toString());
                            if (!obj.getBoolean("error")) {
                                SharedPreferences.Editor editor = mContext.getSharedPreferences(mContext.getString(R.string.app_name), MODE_PRIVATE).edit();
                                editor.putString("CLIENT_ID", obj.getString("CLIENT_ID"));
                                editor.putString("CLIENT_SECRET", obj.getString("CLIENT_SECRET"));

                                JSONObject urls = new JSONObject(obj.getString("urls"));
                                editor.putString("login", urls.getString("login"));
                                editor.putString("token", urls.getString("access_token"));
                                editor.putString("refresh", urls.getString("refresh_token"));
                                editor.putString("documents", urls.getString("upload_document"));
                                editor.putString("logout", urls.getString("logout"));
                                editor.putString("profile", urls.getString("profile"));
                                editor.commit();
                                // Log.d(TAG,response);
                            } else {
                                //error in response
                                Log.d(TAG,"Errro 23"+response.toString());
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
                        iRequests--;
                        volleyError=true;
                        VolleyLog.d(TAG, "Error: " + error.getMessage());
                    }
                }
                ) {
        };
        iRequests++;
        getInstance(mContext).addToRequestQueue(stringRequest);
    }


    public Result.Success<LoggedInUser> login(String username, String password) throws JSONException, IOException {
        final Map<String,String> params = new HashMap<String, String>();
        params.put("grant_type","password");
        params.put("client_id",this.client_id);
        params.put("client_secret",this.client_secret);
        params.put("username",username);
        params.put("password",password);
        Log.d(TAG,"Login Request: "+params.toString());

        JSONObject response=null;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, mContext.getResources().getString(R.string.serverUrl)+"oauth/token",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        iRequests--;
                        volleyError=false;
                        try {
                            Log.d(TAG,"Login Response: "+response);
                            JSONObject obj = new JSONObject(response);
                            if (obj.getString("access_token")!=null) {
                                //add access-token to shared prefs
                                Log.d(TAG,"Storing Shared Prefs");
                                SharedPreferences.Editor editor = mContext.getSharedPreferences(mContext.getString(R.string.app_name), MODE_PRIVATE).edit();
                                editor.putString("access_token", obj.getString("access_token"));
                                editor.putString("refresh_token", obj.getString("refresh_token"));
                                editor.putString("token_type", obj.getString("token_type"));
                                editor.putString("expires_in", obj.getString("expires_in"));
                                editor.apply();
                                Log.d(TAG,"Prefs stored");
                                getUser();

                            } else {
                                //error in response
                                Log.d(TAG,"Login Error1: "+response);
                            }
                        } catch (JSONException e) {
                            Log.d(TAG,"JSON Error: "+e.getMessage());
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        iRequests--;
                        volleyError=true;
                        Log.d(TAG,"Error Response code: " + error.networkResponse.statusCode);
                        Log.d(TAG, "Error: " + error.toString());
                    }
                }) {
            @Override
            protected Map<String,String> getParams() {
                return params;
            }
        };
        iRequests++;
        getInstance(mContext).addToRequestQueue(stringRequest);

        LoggedInUser fakeUser =
                new LoggedInUser(
                        java.util.UUID.randomUUID().toString(),
                        "Jane Doe");
        return new Result.Success<>(fakeUser);
    }

    public Result<LoggedInUser> login_new(String username, String password) {

        try {
            // TODO: handle loggedInUser authentication

            LoggedInUser fakeUser =
                    new LoggedInUser(
                            java.util.UUID.randomUUID().toString(),
                            "Jane Doe");
            return new Result.Success<>(fakeUser);
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }



    public void getUser() throws JSONException {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(mContext.getString(R.string.app_name), MODE_PRIVATE);
        final String access_token = sharedPreferences.getString("access_token","");

        StringRequest stringRequest = new StringRequest(Request.Method.GET, mContext.getResources().getString(R.string.serverUrl)+"api/user",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        iRequests--;
                        volleyError=false;
                        try {
                            Log.d(TAG,"Get User: "+response);
                            JSONObject obj = new JSONObject(response);
                            if (obj.getString("id")!=null) {
                                //add access-token to shared prefs
                                Log.d(TAG,"Response with User Profile");
                                SharedPreferences.Editor editor = mContext.getSharedPreferences(mContext.getString(R.string.app_name), MODE_PRIVATE).edit();
                                editor.putString("id", obj.getString("id"));
                                editor.putString("name", obj.getString("name"));
                                editor.putString("email", obj.getString("email"));
                                editor.putString("phone", obj.getString("phone"));
                                editor.apply();

                            } else {
                                //error in response
                                Log.d(TAG,"Get User Error1: "+response);
                            }
                        } catch (JSONException e) {
                            Log.d(TAG,"Get User JSON Error: "+e.getMessage());
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        iRequests--;
                        volleyError=true;
                        Log.d(TAG,"Error Response code: " + error.networkResponse.statusCode);
                        VolleyLog.d(TAG, "Error: " + error.getMessage());
                    }
                }) {
            //This is for Headers If You Needed
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                final Map<String,String> headers = new HashMap<String, String>();
                headers.put("Authorization","Bearer "+access_token);
                Log.d(TAG,"User Profile headers: "+headers.toString());
                return headers;
            }
        };
        iRequests++;
        getInstance(mContext).addToRequestQueue(stringRequest);


    }


    public JSONObject post(final String url, final Map<String,String> params){
        JSONObject response = new JSONObject();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, mContext.getResources().getString(R.string.serverUrl)+url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {

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
            protected Map<String,String> getParams(){
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {

                /*
                * Authorization: Bearer Token
                * Content-Type application/json
                * */
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer "+access_token);
                return headers;
            }
        };
        getInstance(mContext).addToRequestQueue(stringRequest);
        return response;
    }


}
