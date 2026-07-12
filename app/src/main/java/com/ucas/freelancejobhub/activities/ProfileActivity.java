package com.ucas.freelancejobhub.activities;

import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ucas.freelancejobhub.R;
import com.ucas.freelancejobhub.data.DatabaseHelper;
import com.ucas.freelancejobhub.models.User;
import com.ucas.freelancejobhub.utils.ImageUtils;
import com.ucas.freelancejobhub.utils.SecurityUtils;
import com.ucas.freelancejobhub.utils.SessionManager;
import com.ucas.freelancejobhub.utils.ValidationUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {
    private static final String STATE_PENDING_IMAGE = "state_pending_profile_image";
    private static final String STATE_CAMERA_URI = "state_camera_uri";

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private User user;
    private ImageView profileImage;
    private TextInputLayout nameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout usernameLayout;
    private TextInputLayout phoneLayout;
    private TextInputLayout birthdateLayout;
    private TextInputLayout currentPasswordLayout;
    private TextInputLayout newPasswordLayout;
    private TextInputLayout confirmPasswordLayout;
    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText usernameInput;
    private TextInputEditText phoneInput;
    private TextInputEditText birthdateInput;
    private TextInputEditText currentPasswordInput;
    private TextInputEditText newPasswordInput;
    private TextInputEditText confirmPasswordInput;
    private String pendingProfileImage;
    private Uri cameraImageUri;
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        user = databaseHelper.getUserById(sessionManager.getUserId());
        if (user == null) {
            sessionManager.clear();
            finish();
            return;
        }

        bindViews();
        registerImageLaunchers();
        bindUser();

        MaterialToolbar toolbar = findViewById(R.id.toolbar_profile);
        MaterialButton changePhotoButton = findViewById(R.id.button_change_profile_photo);
        MaterialButton saveButton = findViewById(R.id.button_save_profile);
        MaterialButton dialButton = findViewById(R.id.button_dial_profile);
        MaterialButton passwordButton = findViewById(R.id.button_update_password);
        MaterialButton logoutButton = findViewById(R.id.button_profile_logout);

        toolbar.setNavigationOnClickListener(view -> finish());
        changePhotoButton.setOnClickListener(view -> showImageSourceDialog());
        profileImage.setOnClickListener(view -> showImageSourceDialog());
        birthdateInput.setOnClickListener(view -> showBirthdatePicker());
        birthdateLayout.setEndIconOnClickListener(view -> showBirthdatePicker());
        saveButton.setOnClickListener(view -> saveProfile());
        passwordButton.setOnClickListener(view -> updatePassword());
        dialButton.setOnClickListener(view -> openDialer());
        logoutButton.setOnClickListener(view -> confirmLogout());

        if (savedInstanceState != null) {
            pendingProfileImage = savedInstanceState.getString(STATE_PENDING_IMAGE, pendingProfileImage);
            String cameraUri = savedInstanceState.getString(STATE_CAMERA_URI);
            if (cameraUri != null) cameraImageUri = Uri.parse(cameraUri);
            ImageUtils.loadProfileImage(profileImage, pendingProfileImage);
        }
    }

    private void bindViews() {
        profileImage = findViewById(R.id.image_profile);
        nameLayout = findViewById(R.id.layout_profile_name);
        emailLayout = findViewById(R.id.layout_profile_email);
        usernameLayout = findViewById(R.id.layout_profile_username);
        phoneLayout = findViewById(R.id.layout_profile_phone);
        birthdateLayout = findViewById(R.id.layout_profile_birthdate);
        currentPasswordLayout = findViewById(R.id.layout_current_password);
        newPasswordLayout = findViewById(R.id.layout_new_password);
        confirmPasswordLayout = findViewById(R.id.layout_confirm_new_password);
        nameInput = findViewById(R.id.input_profile_name);
        emailInput = findViewById(R.id.input_profile_email);
        usernameInput = findViewById(R.id.input_profile_username);
        phoneInput = findViewById(R.id.input_profile_phone);
        birthdateInput = findViewById(R.id.input_profile_birthdate);
        currentPasswordInput = findViewById(R.id.input_current_password);
        newPasswordInput = findViewById(R.id.input_new_password);
        confirmPasswordInput = findViewById(R.id.input_confirm_new_password);
    }

    private void registerImageLaunchers() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) return;
                    try {
                        pendingProfileImage = ImageUtils.copyProfileImage(this, uri, user.getId());
                        ImageUtils.loadProfileImage(profileImage, pendingProfileImage);
                    } catch (IOException exception) {
                        Toast.makeText(this, R.string.error_image_read, Toast.LENGTH_LONG).show();
                    }
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && cameraImageUri != null) {
                        pendingProfileImage = cameraImageUri.toString();
                        ImageUtils.loadProfileImage(profileImage, pendingProfileImage);
                    }
                }
        );
    }

    private void bindUser() {
        pendingProfileImage = user.getProfileImage();
        nameInput.setText(user.getName());
        emailInput.setText(user.getEmail());
        usernameInput.setText(user.getUsername());
        phoneInput.setText(user.getPhone());
        birthdateInput.setText(user.getBirthdate());
        ImageUtils.loadProfileImage(profileImage, pendingProfileImage);
    }

    private void showImageSourceDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.change_profile_photo)
                .setItems(R.array.profile_image_sources, (dialog, which) -> {
                    if (which == 0) {
                        galleryLauncher.launch("image/*");
                    } else {
                        openCamera();
                    }
                })
                .show();
    }

    private void openCamera() {
        try {
            cameraImageUri = ImageUtils.createCameraImageUri(this, "profile");
            cameraLauncher.launch(cameraImageUri);
        } catch (IOException | RuntimeException exception) {
            Toast.makeText(this, R.string.error_camera_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    private void showBirthdatePicker() {
        Calendar today = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> birthdateInput.setText(
                        String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)),
                today.get(Calendar.YEAR) - 22,
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMaxDate(today.getTimeInMillis());
        dialog.show();
    }

    private void saveProfile() {
        clearProfileErrors();
        String name = textOf(nameInput);
        String email = textOf(emailInput);
        String username = textOf(usernameInput);
        String phone = textOf(phoneInput);
        String birthdate = textOf(birthdateInput);
        boolean valid = true;

        if (name.length() < 2) {
            nameLayout.setError(getString(R.string.error_name_required));
            valid = false;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            emailLayout.setError(getString(R.string.error_invalid_email));
            valid = false;
        } else if (databaseHelper.isEmailTaken(email, user.getId())) {
            emailLayout.setError(getString(R.string.error_email_taken));
            valid = false;
        }
        if (!ValidationUtils.isValidUsername(username)) {
            usernameLayout.setError(getString(R.string.error_invalid_username));
            valid = false;
        } else if (databaseHelper.isUsernameTaken(username, user.getId())) {
            usernameLayout.setError(getString(R.string.error_username_taken));
            valid = false;
        }
        if (!ValidationUtils.isValidPhone(phone)) {
            phoneLayout.setError(getString(R.string.error_invalid_phone));
            valid = false;
        }
        if (birthdate.isEmpty()) {
            birthdateLayout.setError(getString(R.string.error_birthdate_required));
            valid = false;
        }
        if (!valid) return;

        user.setName(name);
        user.setEmail(email);
        user.setUsername(username);
        user.setPhone(phone);
        user.setBirthdate(birthdate);
        user.setProfileImage(pendingProfileImage == null ? "" : pendingProfileImage);

        try {
            if (databaseHelper.updateUser(user) > 0) {
                Toast.makeText(this, R.string.profile_updated, Toast.LENGTH_SHORT).show();
            }
        } catch (SQLiteConstraintException exception) {
            Toast.makeText(this, R.string.error_account_exists, Toast.LENGTH_LONG).show();
        }
    }

    private void updatePassword() {
        currentPasswordLayout.setError(null);
        newPasswordLayout.setError(null);
        confirmPasswordLayout.setError(null);
        String currentPassword = textOf(currentPasswordInput);
        String newPassword = textOf(newPasswordInput);
        String confirmPassword = textOf(confirmPasswordInput);

        if (currentPassword.isEmpty()) {
            currentPasswordLayout.setError(getString(R.string.error_password_required));
            return;
        }
        if (!databaseHelper.verifyPassword(user.getId(), SecurityUtils.hashPassword(currentPassword))) {
            currentPasswordLayout.setError(getString(R.string.error_incorrect_password));
            return;
        }
        if (!ValidationUtils.isStrongPassword(newPassword)) {
            newPasswordLayout.setError(getString(R.string.error_weak_password));
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordLayout.setError(getString(R.string.error_passwords_mismatch));
            return;
        }

        databaseHelper.updatePassword(user.getId(), SecurityUtils.hashPassword(newPassword));
        currentPasswordInput.setText("");
        newPasswordInput.setText("");
        confirmPasswordInput.setText("");
        Toast.makeText(this, R.string.password_updated, Toast.LENGTH_SHORT).show();
    }

    private void openDialer() {
        String phone = textOf(phoneInput);
        if (!ValidationUtils.isValidPhone(phone)) {
            phoneLayout.setError(getString(R.string.error_invalid_phone));
            return;
        }
        try {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(phone))));
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, R.string.error_no_compatible_app, Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_confirmation)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.logout, (dialog, which) -> {
                    sessionManager.clear();
                    Intent login = new Intent(this, LoginActivity.class);
                    login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(login);
                })
                .show();
    }

    private void clearProfileErrors() {
        nameLayout.setError(null);
        emailLayout.setError(null);
        usernameLayout.setError(null);
        phoneLayout.setError(null);
        birthdateLayout.setError(null);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(STATE_PENDING_IMAGE, pendingProfileImage);
        if (cameraImageUri != null) outState.putString(STATE_CAMERA_URI, cameraImageUri.toString());
        super.onSaveInstanceState(outState);
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
