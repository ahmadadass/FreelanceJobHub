package com.ucas.freelancejobhub.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.ucas.freelancejobhub.R;
import com.ucas.freelancejobhub.data.DatabaseHelper;
import com.ucas.freelancejobhub.models.Project;
import com.ucas.freelancejobhub.utils.SessionManager;

import java.text.NumberFormat;
import java.util.Locale;

public class ProjectDetailsActivity extends AppCompatActivity {
    public static final String EXTRA_PROJECT = "extra_project_entity";

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private Project project;
    private TextView title;
    private TextView client;
    private TextView budget;
    private TextView dueDate;
    private TextView phone;
    private TextView address;
    private TextView description;
    private Chip status;
    private MaterialButton callButton;
    private MaterialButton mapButton;
    private boolean firstResume = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_details);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        project = readProjectExtra();
        if (project == null || !sessionManager.isLoggedIn()) {
            Toast.makeText(this, R.string.error_project_unavailable, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        MaterialToolbar toolbar = findViewById(R.id.toolbar_project_details);
        MaterialButton editButton = findViewById(R.id.button_details_edit);
        MaterialButton deleteButton = findViewById(R.id.button_details_delete);
        MaterialButton shareButton = findViewById(R.id.button_share_project);

        toolbar.setNavigationOnClickListener(view -> finish());
        editButton.setOnClickListener(view -> openEditScreen());
        deleteButton.setOnClickListener(view -> confirmDelete());
        callButton.setOnClickListener(view -> openDialer());
        mapButton.setOnClickListener(view -> openMap());
        shareButton.setOnClickListener(view -> shareProject());
        bindProject();
    }

    @SuppressWarnings("deprecation")
    private Project readProjectExtra() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return getIntent().getParcelableExtra(EXTRA_PROJECT, Project.class);
        }
        return getIntent().getParcelableExtra(EXTRA_PROJECT);
    }

    private void bindViews() {
        title = findViewById(R.id.text_details_title);
        client = findViewById(R.id.text_details_client);
        budget = findViewById(R.id.text_details_budget);
        dueDate = findViewById(R.id.text_details_due_date);
        phone = findViewById(R.id.text_details_phone);
        address = findViewById(R.id.text_details_address);
        description = findViewById(R.id.text_details_description);
        status = findViewById(R.id.chip_details_status);
        callButton = findViewById(R.id.button_call_client);
        mapButton = findViewById(R.id.button_open_map);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (firstResume) {
            firstResume = false;
            return;
        }
        if (project != null && sessionManager != null) {
            Project refreshed = databaseHelper.getProjectById(project.getId(), sessionManager.getUserId());
            if (refreshed == null) {
                finish();
            } else {
                project = refreshed;
                getIntent().putExtra(EXTRA_PROJECT, project);
                if (title != null) bindProject();
            }
        }
    }

    private void bindProject() {
        title.setText(project.getTitle());
        client.setText(project.getClientName());
        budget.setText(NumberFormat.getCurrencyInstance(Locale.US).format(project.getBudget()));
        status.setText(project.getStatus());
        status.setChipBackgroundColor(ColorStateList.valueOf(
                ContextCompat.getColor(this, statusColor(project.getStatus()))));
        dueDate.setText(emptyFallback(project.getDueDate(), getString(R.string.no_deadline)));
        phone.setText(emptyFallback(project.getClientPhone(), getString(R.string.not_provided)));
        address.setText(emptyFallback(project.getMeetingAddress(), getString(R.string.not_provided)));
        description.setText(emptyFallback(project.getDescription(), getString(R.string.no_description)));

        boolean hasPhone = !isEmpty(project.getClientPhone());
        boolean hasAddress = !isEmpty(project.getMeetingAddress());
        callButton.setEnabled(hasPhone);
        mapButton.setEnabled(hasAddress);
        callButton.setAlpha(hasPhone ? 1f : 0.5f);
        mapButton.setAlpha(hasAddress ? 1f : 0.5f);
    }

    private int statusColor(String value) {
        if ("Active".equals(value)) return R.color.status_active_background;
        if ("Completed".equals(value)) return R.color.status_completed_background;
        if ("Pending Invoice".equals(value)) return R.color.status_pending_background;
        if ("On Hold".equals(value)) return R.color.status_hold_background;
        return R.color.status_planning_background;
    }

    private void openEditScreen() {
        Intent edit = new Intent(this, EditProjectActivity.class);
        edit.putExtra(EXTRA_PROJECT, project);
        startActivity(edit);
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_project)
                .setMessage(getString(R.string.delete_project_confirmation, project.getTitle()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    databaseHelper.deleteProject(project.getId(), sessionManager.getUserId());
                    Toast.makeText(this, R.string.project_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .show();
    }

    private void openDialer() {
        launchExternalIntent(new Intent(Intent.ACTION_DIAL,
                Uri.parse("tel:" + Uri.encode(project.getClientPhone()))));
    }

    private void openMap() {
        Uri location = Uri.parse("geo:0,0?q=" + Uri.encode(project.getMeetingAddress()));
        launchExternalIntent(new Intent(Intent.ACTION_VIEW, location));
    }

    private void shareProject() {
        String shareText = getString(R.string.project_share_text,
                project.getTitle(), project.getClientName(), project.getStatus(),
                NumberFormat.getCurrencyInstance(Locale.US).format(project.getBudget()));
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, project.getTitle());
        share.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(share, getString(R.string.share_project)));
    }

    private void launchExternalIntent(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, R.string.error_no_compatible_app, Toast.LENGTH_SHORT).show();
        }
    }

    private String emptyFallback(String value, String fallback) {
        return isEmpty(value) ? fallback : value;
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
