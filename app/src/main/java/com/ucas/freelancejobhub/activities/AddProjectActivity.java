package com.ucas.freelancejobhub.activities;

import com.ucas.freelancejobhub.models.Project;

public class AddProjectActivity extends BaseProjectFormActivity {
    @Override
    protected boolean isEditMode() {
        return false;
    }

    @Override
    protected Project getProjectFromIntent() {
        return null;
    }
}
