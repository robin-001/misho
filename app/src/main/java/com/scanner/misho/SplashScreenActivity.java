package com.scanner.misho;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.scanner.misho.ui.login.LoginActivity;

import static android.app.PendingIntent.getActivity;

public class SplashScreenActivity extends AppCompatActivity {
    private static String TAG ="SplashScreenActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        final String id = sharedPreferences.getString("name",null);
        if(id==null){
            startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
        }
        else{
            startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
        }

        finish();
    }
}