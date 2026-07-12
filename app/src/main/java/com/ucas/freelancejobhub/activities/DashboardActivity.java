package com.ucas.freelancejobhub.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.ucas.freelancejobhub.R;
import com.ucas.freelancejobhub.data.DatabaseHelper;
import com.ucas.freelancejobhub.models.User;
import com.ucas.freelancejobhub.utils.ImageUtils;
import com.ucas.freelancejobhub.utils.SessionManager;

import java.text.NumberFormat;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private TextView welcomeText;
    private TextView subtitleText;
    private TextView activeCountText;
    private TextView totalBudgetText;
    private TextView pendingCountText;
    private TextView totalProjectsText;
    private ImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            openLogin();
            return;
        }

        bindViews();
        MaterialToolbar toolbar = findViewById(R.id.toolbar_dashboard);
        MaterialButton projectsButton = findViewById(R.id.button_view_projects);
        MaterialButton addButton = findViewById(R.id.button_quick_add);
        MaterialButton profileButton = findViewById(R.id.button_open_profile);

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            if (item.getItemId() == R.id.action_logout) {
                confirmLogout();
                return true;
            }
            return false;
        });

        projectsButton.setOnClickListener(view ->
                startActivity(new Intent(this, ProjectsActivity.class)));
        addButton.setOnClickListener(view ->
                startActivity(new Intent(this, AddProjectActivity.class)));
        profileButton.setOnClickListener(view ->
                startActivity(new Intent(this, ProfileActivity.class)));
        profileImage.setOnClickListener(view ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void bindViews() {
        welcomeText = findViewById(R.id.text_dashboard_welcome);
        subtitleText = findViewById(R.id.text_dashboard_subtitle);
        activeCountText = findViewById(R.id.text_active_count);
        totalBudgetText = findViewById(R.id.text_total_budget);
        pendingCountText = findViewById(R.id.text_pending_count);
        totalProjectsText = findViewById(R.id.text_total_projects);
        profileImage = findViewById(R.id.image_dashboard_profile);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager != null && sessionManager.isLoggedIn()) {
            refreshDashboard();
        }
    }

    private void refreshDashboard() {
        long userId = sessionManager.getUserId();
        User user = databaseHelper.getUserById(userId);
        if (user == null) {
            sessionManager.clear();
            openLogin();
            return;
        }

        DatabaseHelper.DashboardStats stats = databaseHelper.getDashboardStats(userId);
        welcomeText.setText(getString(R.string.welcome_user, user.getName()));
        subtitleText.setText(stats.getTotalProjects() == 0
                ? R.string.dashboard_empty_subtitle
                : R.string.dashboard_ready_subtitle);
        activeCountText.setText(String.valueOf(stats.getActiveProjects()));
        pendingCountText.setText(String.valueOf(stats.getPendingInvoices()));
        totalProjectsText.setText(getResources().getQuantityString(
                R.plurals.project_count, stats.getTotalProjects(), stats.getTotalProjects()));
        totalBudgetText.setText(NumberFormat.getCurrencyInstance(Locale.US)
                .format(stats.getTotalBudget()));
        ImageUtils.loadProfileImage(profileImage, user.getProfileImage());
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_confirmation)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.logout, (dialog, which) -> {
                    sessionManager.clear();
                    openLogin();
                })
                .show();
    }

    private void openLogin() {
        Intent login = new Intent(this, LoginActivity.class);
        login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(login);
    }
}
