package com.ucas.freelancejobhub.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ucas.freelancejobhub.R;
import com.ucas.freelancejobhub.data.DatabaseHelper;
import com.ucas.freelancejobhub.models.User;
import com.ucas.freelancejobhub.utils.SecurityUtils;
import com.ucas.freelancejobhub.utils.ValidationUtils;

import java.util.Calendar;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {
    private TextInputLayout nameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout usernameLayout;
    private TextInputLayout phoneLayout;
    private TextInputLayout birthdateLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmLayout;
    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText usernameInput;
    private TextInputEditText phoneInput;
    private TextInputEditText birthdateInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmInput;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        databaseHelper = new DatabaseHelper(this);
        bindViews();

        MaterialToolbar toolbar = findViewById(R.id.toolbar_register);
        MaterialButton registerButton = findViewById(R.id.button_register);
        toolbar.setNavigationOnClickListener(view -> finish());
        birthdateInput.setOnClickListener(view -> showBirthdatePicker());
        birthdateLayout.setEndIconOnClickListener(view -> showBirthdatePicker());
        registerButton.setOnClickListener(view -> createAccount());
    }

    private void bindViews() {
        nameLayout = findViewById(R.id.layout_register_name);
        emailLayout = findViewById(R.id.layout_register_email);
        usernameLayout = findViewById(R.id.layout_register_username);
        phoneLayout = findViewById(R.id.layout_register_phone);
        birthdateLayout = findViewById(R.id.layout_register_birthdate);
        passwordLayout = findViewById(R.id.layout_register_password);
        confirmLayout = findViewById(R.id.layout_register_confirm_password);
        nameInput = findViewById(R.id.input_register_name);
        emailInput = findViewById(R.id.input_register_email);
        usernameInput = findViewById(R.id.input_register_username);
        phoneInput = findViewById(R.id.input_register_phone);
        birthdateInput = findViewById(R.id.input_register_birthdate);
        passwordInput = findViewById(R.id.input_register_password);
        confirmInput = findViewById(R.id.input_register_confirm_password);
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

    private void createAccount() {
        clearErrors();
        String name = textOf(nameInput);
        String email = textOf(emailInput);
        String username = textOf(usernameInput);
        String phone = textOf(phoneInput);
        String birthdate = textOf(birthdateInput);
        String password = textOf(passwordInput);
        String confirmPassword = textOf(confirmInput);
        boolean valid = true;

        if (name.length() < 2) {
            nameLayout.setError(getString(R.string.error_name_required));
            valid = false;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            emailLayout.setError(getString(R.string.error_invalid_email));
            valid = false;
        } else if (databaseHelper.isEmailTaken(email, -1)) {
            emailLayout.setError(getString(R.string.error_email_taken));
            valid = false;
        }
        if (!ValidationUtils.isValidUsername(username)) {
            usernameLayout.setError(getString(R.string.error_invalid_username));
            valid = false;
        } else if (databaseHelper.isUsernameTaken(username, -1)) {
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
        if (!ValidationUtils.isStrongPassword(password)) {
            passwordLayout.setError(getString(R.string.error_weak_password));
            valid = false;
        }
        if (!password.equals(confirmPassword)) {
            confirmLayout.setError(getString(R.string.error_passwords_mismatch));
            valid = false;
        }

        if (!valid) {
            return;
        }

        User user = new User(0, name, email, username, phone, birthdate, "");
        try {
            long userId = databaseHelper.registerUser(user, SecurityUtils.hashPassword(password));
            if (userId > 0) {
                Toast.makeText(this, R.string.account_created, Toast.LENGTH_SHORT).show();
                Intent result = new Intent();
                result.putExtra(LoginActivity.EXTRA_IDENTIFIER, username);
                setResult(RESULT_OK, result);
                finish();
            }
        } catch (SQLiteConstraintException exception) {
            Toast.makeText(this, R.string.error_account_exists, Toast.LENGTH_LONG).show();
        }
    }

    private void clearErrors() {
        nameLayout.setError(null);
        emailLayout.setError(null);
        usernameLayout.setError(null);
        phoneLayout.setError(null);
        birthdateLayout.setError(null);
        passwordLayout.setError(null);
        confirmLayout.setError(null);
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
