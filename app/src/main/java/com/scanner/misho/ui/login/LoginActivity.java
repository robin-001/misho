package com.scanner.misho.ui.login;

import android.app.Activity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.scanner.misho.App;
import com.scanner.misho.MainActivity;
import com.scanner.misho.R;
import com.scanner.misho.SplashScreenActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private LoginViewModel loginViewModel;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory()).get(LoginViewModel.class);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                    startActivity(new Intent(LoginActivity.this, LoginActivity.class));
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                }
                setResult(Activity.RESULT_OK);

                //Complete and destroy login activity once successful
                finish();
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.makeLoginRequest(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setTitle(R.string.login_title);
                progressDialog.setProgress(10);
                progressDialog.setMax(100);
                progressDialog.setMessage(getString(R.string.login_message));
                new LoginTask().execute(new String[]{usernameEditText.getText().toString(),
                        passwordEditText.getText().toString()});
            }
        });
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
    }

    public void getClientID(){
        HttpURLConnection conn = null;
        try {
            URL url = new URL(getString(R.string.serverUrl)+"api");
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");


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

            JSONObject obj = new JSONObject(response.toString());
            if (!obj.getBoolean("error")) {
                Log.d(TAG,obj.getString("CLIENT_ID"));

                SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).edit();
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
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }
    }

    public JSONObject login(String username,String password){
        JSONObject obj = null;
        Uri.Builder builder = new Uri.Builder();
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        final Map<String,String> params = new HashMap<String, String>();
        params.put("grant_type","password");
        params.put("client_id",sharedPreferences.getString("CLIENT_ID",""));
        params.put("client_secret",sharedPreferences.getString("CLIENT_SECRET",""));
        params.put("username",username);
        params.put("password",password);
        Log.d(TAG,"Login Request: "+params.toString());
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
            SharedPreferences prefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
            String endpoint = getString(R.string.serverUrl)+prefs.getString("login","");
            Log.d(TAG,endpoint);
            URL url = new URL(endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            OutputStream output = new BufferedOutputStream(conn.getOutputStream());
            output.write(requestBody.getBytes());
            output.flush();

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
            if (obj.has("access_token")) {


                editor.putString("access_token", obj.getString("access_token"));
                editor.putString("refresh_token", obj.getString("refresh_token"));
                editor.putString("token_type", obj.getString("token_type"));
                editor.putString("expires_in", obj.getString("expires_in"));

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



    public class LoginTask extends AsyncTask<String, Void, Boolean> {
            private static final String TAG = "LoginTask";
            AlertDialog alertDialog;
            public void onPreExecute() {
                alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                progressDialog.show();
            }

            public Boolean doInBackground(String... params) {
                getClientID();
                JSONObject login_response = login(params[0], params[1]);
                Boolean login_result=false;
                if(login_response.has("error")) {
                    login_result = false;
                } else {
                    login_result = true;
                }
                return login_result;
            }

        @Override
        protected void onPostExecute(Boolean executed){
            progressDialog.dismiss();
            SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
            //check if login was successful
            if(sharedPreferences.contains("error")){

                alertDialog.setTitle("Login Error");
                alertDialog.setMessage(sharedPreferences.getString("error_description","Login failed"));
               // alertDialog.setIcon(R.drawable.);
                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.cancel();
                    } });

                //clear prefs
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
                alertDialog.show();
            }
            else{
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }
        }
    }



}