package com.polarscoop.driver.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.polarscoop.driver.R;

/**
 * LoginActivity – the app's entry point.
 *
 * Authentication is intentionally hard-coded for this PoC:
 *   Driver ID : DRIVER01   Password : password123
 *   Driver ID : DRIVER02   Password : password123
 *
 * In a production app this would call a REST API with proper credential handling.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etDriverId;
    private EditText etPassword;
    private TextView tvErrorMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Bind views
        etDriverId = findViewById(R.id.etDriverId);
        etPassword = findViewById(R.id.etPassword);
        tvErrorMsg = findViewById(R.id.tvErrorMsg);
        Button btnLogin = findViewById(R.id.btnLogin);

        // Allow "Done" on the keyboard to trigger login
        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    /**
     * Validates the entered credentials against the hard-coded driver accounts.
     * On success navigates to DeliveryListActivity; on failure shows an error.
     */
    private void attemptLogin() {
        String driverId = etDriverId.getText().toString().trim().toUpperCase();
        String password = etPassword.getText().toString().trim();

        // Hide any previous error
        tvErrorMsg.setVisibility(View.GONE);

        if (driverId.isEmpty() || password.isEmpty()) {
            tvErrorMsg.setText("Please enter both Driver ID and Password.");
            tvErrorMsg.setVisibility(View.VISIBLE);
            return;
        }

        if (isValidCredentials(driverId, password)) {
            // Pass the driver ID to the next screen so it can display the name
            Intent intent = new Intent(this, DeliveryListActivity.class);
            intent.putExtra(DeliveryListActivity.EXTRA_DRIVER_ID, driverId);
            startActivity(intent);
            finish();   // Remove login from back stack
        } else {
            tvErrorMsg.setText(getString(R.string.invalid_credentials));
            tvErrorMsg.setVisibility(View.VISIBLE);
            etPassword.setText("");   // Clear password field for retry
            etPassword.requestFocus();
        }
    }

    /**
     * Hard-coded credential check.
     * Accepted logins:
     *   DRIVER01 / password123
     *   DRIVER02 / password123
     */
    private boolean isValidCredentials(String driverId, String password) {
        boolean validId = driverId.equals("DRIVER01") || driverId.equals("DRIVER02");
        boolean validPw = password.equals("password123");
        return validId && validPw;
    }
}
