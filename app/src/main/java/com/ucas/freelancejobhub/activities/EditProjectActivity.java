package com.ucas.freelancejobhub.activities;

import android.os.Build;

import com.ucas.freelancejobhub.models.Project;

public class EditProjectActivity extends BaseProjectFormActivity {
    @Override
    protected boolean isEditMode() {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    protected Project getProjectFromIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return getIntent().getParcelableExtra(ProjectDetailsActivity.EXTRA_PROJECT, Project.class);
        }
        return getIntent().getParcelableExtra(ProjectDetailsActivity.EXTRA_PROJECT);
    }
}
