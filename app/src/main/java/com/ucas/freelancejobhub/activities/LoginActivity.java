package com.ucas.freelancejobhub.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ucas.freelancejobhub.R;
import com.ucas.freelancejobhub.data.DatabaseHelper;
import com.ucas.freelancejobhub.models.User;
import com.ucas.freelancejobhub.utils.SecurityUtils;
import com.ucas.freelancejobhub.utils.SessionManager;
import com.ucas.freelancejobhub.utils.ValidationUtils;

public class LoginActivity extends AppCompatActivity {
    public static final String EXTRA_IDENTIFIER = "extra_identifier";

    private TextInputLayout identifierLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText identifierInput;
    private TextInputEditText passwordInput;
    private CheckBox rememberCheckBox;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private ActivityResultLauncher<Intent> registerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        identifierLayout = findViewById(R.id.layout_login_identifier);
        passwordLayout = findViewById(R.id.layout_login_password);
        identifierInput = findViewById(R.id.input_login_identifier);
        passwordInput = findViewById(R.id.input_login_password);
        rememberCheckBox = findViewById(R.id.checkbox_remember_me);
        MaterialButton loginButton = findViewById(R.id.button_login);
        TextView registerLink = findViewById(R.id.link_create_account);

        registerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String identifier = result.getData().getStringExtra(EXTRA_IDENTIFIER);
                        if (!TextUtils.isEmpty(identifier)) {
                            identifierInput.setText(identifier);
                            passwordInput.requestFocus();
                        }
                    }
                }
        );

        String suggestedIdentifier = getIntent().getStringExtra(EXTRA_IDENTIFIER);
        if (!TextUtils.isEmpty(suggestedIdentifier)) {
            identifierInput.setText(suggestedIdentifier);
            passwordInput.requestFocus();
        }

        loginButton.setOnClickListener(view -> attemptLogin());
        registerLink.setOnClickListener(view ->
                registerLauncher.launch(new Intent(this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        identifierLayout.setError(null);
        passwordLayout.setError(null);

        String identifier = textOf(identifierInput);
        String password = textOf(passwordInput);
        boolean valid = true;

        if (identifier.isEmpty()) {
            identifierLayout.setError(getString(R.string.error_identifier_required));
            valid = false;
        } else if (identifier.contains("@") && !ValidationUtils.isValidEmail(identifier)) {
            identifierLayout.setError(getString(R.string.error_invalid_email));
            valid = false;
        }

        if (password.isEmpty()) {
            passwordLayout.setError(getString(R.string.error_password_required));
            valid = false;
        }

        if (!valid) {
            return;
        }

        User user = databaseHelper.getUserByIdentifier(identifier);
        if (user == null) {
            identifierLayout.setError(getString(R.string.error_user_not_found));
            return;
        }

        if (!databaseHelper.verifyPassword(user.getId(), SecurityUtils.hashPassword(password))) {
            passwordLayout.setError(getString(R.string.error_incorrect_password));
            return;
        }

        sessionManager.startSession(user.getId(), rememberCheckBox.isChecked());
        Intent dashboard = new Intent(this, DashboardActivity.class);
        dashboard.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(dashboard);
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
