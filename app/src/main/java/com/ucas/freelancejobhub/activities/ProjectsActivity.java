package com.ucas.freelancejobhub.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ucas.freelancejobhub.R;
import com.ucas.freelancejobhub.adapters.ProjectAdapter;
import com.ucas.freelancejobhub.data.DatabaseHelper;
import com.ucas.freelancejobhub.models.Project;
import com.ucas.freelancejobhub.utils.SessionManager;

import java.util.List;

public class ProjectsActivity extends AppCompatActivity implements ProjectAdapter.ProjectListener {
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private ProjectAdapter adapter;
    private View emptyState;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar_projects);
        RecyclerView recyclerView = findViewById(R.id.recycler_projects);
        FloatingActionButton addButton = findViewById(R.id.fab_add_project);
        emptyState = findViewById(R.id.text_projects_empty);
        searchView = findViewById(R.id.search_projects);

        toolbar.setNavigationOnClickListener(view -> finish());
        adapter = new ProjectAdapter(this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, calculateSpanCount()));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                applyFilter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                applyFilter(newText);
                return true;
            }
        });

        addButton.setOnClickListener(view ->
                startActivity(new Intent(this, AddProjectActivity.class)));
    }

    private int calculateSpanCount() {
        int widthDp = getResources().getConfiguration().screenWidthDp;
        if (widthDp >= 840) return 3;
        if (widthDp >= 600) return 2;
        return 1;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            loadProjects();
        }
    }

    private void loadProjects() {
        List<Project> projects = databaseHelper.getProjects(sessionManager.getUserId());
        adapter.submitList(projects);
        updateEmptyState();
    }

    private void applyFilter(String query) {
        adapter.filter(query);
        updateEmptyState();
    }

    private void updateEmptyState() {
        boolean empty = adapter.getVisibleItemCount() == 0;
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onProjectClicked(Project project) {
        Intent details = new Intent(this, ProjectDetailsActivity.class);
        details.putExtra(ProjectDetailsActivity.EXTRA_PROJECT, project);
        startActivity(details);
    }

    @Override
    public void onEditClicked(Project project) {
        Intent edit = new Intent(this, EditProjectActivity.class);
        edit.putExtra(ProjectDetailsActivity.EXTRA_PROJECT, project);
        startActivity(edit);
    }

    @Override
    public void onDeleteClicked(Project project) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_project)
                .setMessage(getString(R.string.delete_project_confirmation, project.getTitle()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    databaseHelper.deleteProject(project.getId(), sessionManager.getUserId());
                    loadProjects();
                })
                .show();
    }
}
