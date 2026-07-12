package com.ucas.freelancejobhub;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.ucas.freelancejobhub.activities.DashboardActivity;
import com.ucas.freelancejobhub.activities.LoginActivity;
import com.ucas.freelancejobhub.utils.SessionManager;

/** Lightweight launcher that restores a remembered session or opens the login screen. */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager sessionManager = new SessionManager(this);
        Intent nextScreen;

        if (sessionManager.isLoggedIn() && sessionManager.isRemembered()) {
            nextScreen = new Intent(this, DashboardActivity.class);
        } else {
            sessionManager.clear();
            nextScreen = new Intent(this, LoginActivity.class);
        }

        startActivity(nextScreen);
        finish();
    }
}
