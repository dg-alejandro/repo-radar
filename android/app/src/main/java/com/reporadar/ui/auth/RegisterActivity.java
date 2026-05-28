package com.reporadar.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.reporadar.R;
import com.reporadar.RetrofitClient;
import com.reporadar.api.ApiService;
import com.reporadar.model.AuthRequest;
import com.reporadar.model.AuthResponse;
import com.reporadar.session.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Pantalla de registro. Envia name + email + password al backend.
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private Button btnRegister;
    private ProgressBar progress;

    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Crear cuenta");
        }

        apiService = RetrofitClient.getInstance().create(ApiService.class);
        sessionManager = SessionManager.getInstance(this);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progress = findViewById(R.id.progress);

        btnRegister.setOnClickListener(v -> doRegister());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void doRegister() {
        final String name = etName.getText().toString().trim();
        final String email = etEmail.getText().toString().trim();
        final String password = etPassword.getText().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "La contrasena debe tener al menos 6 caracteres",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        setBusy(true);
        apiService.register(new AuthRequest(name, email, password)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call,
                                   @NonNull Response<AuthResponse> response) {
                setBusy(false);
                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().getToken() != null) {
                    sessionManager.saveSession(response.body().getToken(), email);
                    Toast.makeText(RegisterActivity.this, "Cuenta creada",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this,
                            "No se pudo completar el registro (HTTP " + response.code() + ")",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                setBusy(false);
                Toast.makeText(RegisterActivity.this, "Error de conexion: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setBusy(boolean busy) {
        progress.setVisibility(busy ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!busy);
    }
}
