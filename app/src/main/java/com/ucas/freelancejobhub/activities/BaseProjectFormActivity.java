package com.ucas.freelancejobhub.activities;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.ucas.freelancejobhub.R;
import com.ucas.freelancejobhub.data.DatabaseHelper;
import com.ucas.freelancejobhub.models.Project;
import com.ucas.freelancejobhub.utils.ImageUtils;
import com.ucas.freelancejobhub.utils.SessionManager;
import com.ucas.freelancejobhub.utils.ValidationUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

/** Shared implementation for the distinct Add and Edit form screens. */
public abstract class BaseProjectFormActivity extends AppCompatActivity {
    private static final String STATE_TITLE = "state_title";
    private static final String STATE_CLIENT = "state_client";
    private static final String STATE_PHONE = "state_phone";
    private static final String STATE_ADDRESS = "state_address";
    private static final String STATE_BUDGET = "state_budget";
    private static final String STATE_DUE_DATE = "state_due_date";
    private static final String STATE_STATUS_POSITION = "state_status_position";
    private static final String STATE_DESCRIPTION = "state_description";
    private static final String STATE_OCR_URI = "state_ocr_uri";

    private TextInputLayout titleLayout;
    private TextInputLayout clientLayout;
    private TextInputLayout phoneLayout;
    private TextInputLayout budgetLayout;
    private TextInputLayout dueDateLayout;
    private TextInputEditText titleInput;
    private TextInputEditText clientInput;
    private TextInputEditText phoneInput;
    private TextInputEditText addressInput;
    private TextInputEditText budgetInput;
    private TextInputEditText dueDateInput;
    private TextInputEditText descriptionInput;
    private Spinner statusSpinner;
    private MaterialButton scanButton;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private Project currentProject;
    private Uri currentOcrImageUri;
    private ActivityResultLauncher<Uri> ocrCameraLauncher;

    protected abstract boolean isEditMode();

    protected abstract Project getProjectFromIntent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_form);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        bindViews();
        setupStatusSpinner();
        registerOcrCamera();

        currentProject = isEditMode() ? getProjectFromIntent() : null;
        if (isEditMode() && currentProject == null) {
            Toast.makeText(this, R.string.error_project_unavailable, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar_project_form);
        MaterialButton saveButton = findViewById(R.id.button_save_project);
        MaterialButton cancelButton = findViewById(R.id.button_cancel_project);
        toolbar.setTitle(isEditMode() ? R.string.edit_project : R.string.add_project);
        saveButton.setText(isEditMode() ? R.string.save_changes : R.string.create_project);
        toolbar.setNavigationOnClickListener(view -> finish());
        cancelButton.setOnClickListener(view -> finish());
        saveButton.setOnClickListener(view -> saveProject());
        dueDateInput.setOnClickListener(view -> showDueDatePicker());
        dueDateLayout.setEndIconOnClickListener(view -> showDueDatePicker());
        scanButton.setOnClickListener(view -> captureContractText());

        if (savedInstanceState == null && currentProject != null) {
            populateProject(currentProject);
        }
    }

    private void bindViews() {
        titleLayout = findViewById(R.id.layout_project_title);
        clientLayout = findViewById(R.id.layout_project_client);
        phoneLayout = findViewById(R.id.layout_project_phone);
        budgetLayout = findViewById(R.id.layout_project_budget);
        dueDateLayout = findViewById(R.id.layout_project_due_date);
        titleInput = findViewById(R.id.input_project_title);
        clientInput = findViewById(R.id.input_project_client);
        phoneInput = findViewById(R.id.input_project_phone);
        addressInput = findViewById(R.id.input_project_address);
        budgetInput = findViewById(R.id.input_project_budget);
        dueDateInput = findViewById(R.id.input_project_due_date);
        descriptionInput = findViewById(R.id.input_project_description);
        statusSpinner = findViewById(R.id.spinner_project_status);
        scanButton = findViewById(R.id.button_scan_contract);
    }

    private void setupStatusSpinner() {
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.project_statuses,
                R.layout.item_spinner_selected
        );
        statusAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        statusSpinner.setAdapter(statusAdapter);
    }

    private void registerOcrCamera() {
        ocrCameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && currentOcrImageUri != null) {
                        recognizeContractText(currentOcrImageUri);
                    } else {
                        Toast.makeText(this, R.string.scan_cancelled, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void captureContractText() {
        try {
            currentOcrImageUri = ImageUtils.createCameraImageUri(this, "contract");
            ocrCameraLauncher.launch(currentOcrImageUri);
        } catch (IOException | RuntimeException exception) {
            Toast.makeText(this, R.string.error_camera_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    private void recognizeContractText(Uri imageUri) {
        scanButton.setEnabled(false);
        scanButton.setText(R.string.scanning_contract);
        try {
            InputImage image = InputImage.fromFilePath(this, imageUri);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            recognizer.process(image)
                    .addOnSuccessListener(result -> {
                        String recognized = result.getText().trim();
                        if (recognized.isEmpty()) {
                            Toast.makeText(this, R.string.no_text_detected, Toast.LENGTH_SHORT).show();
                        } else {
                            descriptionInput.setText(recognized);
                            descriptionInput.setSelection(descriptionInput.length());
                            Toast.makeText(this, R.string.text_scanned, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(error -> Toast.makeText(
                            this, R.string.error_text_recognition, Toast.LENGTH_LONG).show())
                    .addOnCompleteListener(task -> {
                        recognizer.close();
                        scanButton.setEnabled(true);
                        scanButton.setText(R.string.scan_contract_text);
                    });
        } catch (IOException exception) {
            scanButton.setEnabled(true);
            scanButton.setText(R.string.scan_contract_text);
            Toast.makeText(this, R.string.error_image_read, Toast.LENGTH_LONG).show();
        }
    }

    private void showDueDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> dueDateInput.setText(
                        String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000L);
        dialog.show();
    }

    private void populateProject(Project project) {
        titleInput.setText(project.getTitle());
        clientInput.setText(project.getClientName());
        phoneInput.setText(project.getClientPhone());
        addressInput.setText(project.getMeetingAddress());
        budgetInput.setText(String.format(Locale.US, "%.2f", project.getBudget()));
        dueDateInput.setText(project.getDueDate());
        descriptionInput.setText(project.getDescription());

        String[] statuses = getResources().getStringArray(R.array.project_statuses);
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equals(project.getStatus())) {
                statusSpinner.setSelection(i);
                break;
            }
        }
    }

    private void saveProject() {
        clearErrors();
        String title = textOf(titleInput);
        String client = textOf(clientInput);
        String phone = textOf(phoneInput);
        String address = textOf(addressInput);
        String budgetValue = textOf(budgetInput);
        String dueDate = textOf(dueDateInput);
        String description = textOf(descriptionInput);
        boolean valid = true;
        double budget = 0;

        if (title.length() < 3) {
            titleLayout.setError(getString(R.string.error_project_title));
            valid = false;
        }
        if (client.length() < 2) {
            clientLayout.setError(getString(R.string.error_client_name));
            valid = false;
        }
        if (!phone.isEmpty() && !ValidationUtils.isValidPhone(phone)) {
            phoneLayout.setError(getString(R.string.error_invalid_phone));
            valid = false;
        }
        try {
            budget = Double.parseDouble(budgetValue);
            if (budget < 0) throw new NumberFormatException();
        } catch (NumberFormatException exception) {
            budgetLayout.setError(getString(R.string.error_invalid_budget));
            valid = false;
        }
        if (dueDate.isEmpty()) {
            dueDateLayout.setError(getString(R.string.error_due_date));
            valid = false;
        }

        if (!valid) {
            return;
        }

        long id = currentProject == null ? 0 : currentProject.getId();
        Project project = new Project(
                id,
                sessionManager.getUserId(),
                title,
                client,
                phone,
                address,
                budget,
                String.valueOf(statusSpinner.getSelectedItem()),
                dueDate,
                description
        );

        if (isEditMode()) {
            int updated = databaseHelper.updateProject(project);
            if (updated > 0) {
                Toast.makeText(this, R.string.project_updated, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, R.string.error_save_project, Toast.LENGTH_LONG).show();
            }
        } else {
            long projectId = databaseHelper.addProject(project);
            if (projectId > 0) {
                Toast.makeText(this, R.string.project_created, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, R.string.error_save_project, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void clearErrors() {
        titleLayout.setError(null);
        clientLayout.setError(null);
        phoneLayout.setError(null);
        budgetLayout.setError(null);
        dueDateLayout.setError(null);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(STATE_TITLE, textOf(titleInput));
        outState.putString(STATE_CLIENT, textOf(clientInput));
        outState.putString(STATE_PHONE, textOf(phoneInput));
        outState.putString(STATE_ADDRESS, textOf(addressInput));
        outState.putString(STATE_BUDGET, textOf(budgetInput));
        outState.putString(STATE_DUE_DATE, textOf(dueDateInput));
        outState.putInt(STATE_STATUS_POSITION, statusSpinner.getSelectedItemPosition());
        outState.putString(STATE_DESCRIPTION, textOf(descriptionInput));
        if (currentOcrImageUri != null) {
            outState.putString(STATE_OCR_URI, currentOcrImageUri.toString());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        titleInput.setText(savedInstanceState.getString(STATE_TITLE, ""));
        clientInput.setText(savedInstanceState.getString(STATE_CLIENT, ""));
        phoneInput.setText(savedInstanceState.getString(STATE_PHONE, ""));
        addressInput.setText(savedInstanceState.getString(STATE_ADDRESS, ""));
        budgetInput.setText(savedInstanceState.getString(STATE_BUDGET, ""));
        dueDateInput.setText(savedInstanceState.getString(STATE_DUE_DATE, ""));
        statusSpinner.setSelection(savedInstanceState.getInt(STATE_STATUS_POSITION, 0));
        descriptionInput.setText(savedInstanceState.getString(STATE_DESCRIPTION, ""));
        String uriValue = savedInstanceState.getString(STATE_OCR_URI);
        if (uriValue != null) {
            currentOcrImageUri = Uri.parse(uriValue);
        }
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
