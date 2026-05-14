package com.example.distributed_gambling_platform;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import shared.Request;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilUsername;
    private TextInputEditText etUsername;
    private MaterialButton btnSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindViews();
        setupInputField();
        setupButtons();
    }

    private void bindViews() {
        tilUsername = findViewById(R.id.tilUsername);

        etUsername = findViewById(R.id.etUsername);
        btnSignIn = findViewById(R.id.btnSignIn);
    }

    private void setupInputField() {
        etUsername.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                tilUsername.setError(null);
            }
        });


    }

    private void setupButtons() {
        btnSignIn.setOnClickListener(v -> {
            String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";

            if (username.isEmpty()) {
                tilUsername.setError("Please enter a username");
                return;
            }


            Intent i = new Intent(LoginActivity.this, DashboardActivity.class);
            i.putExtra("USERNAME", username);
            startActivity(i);
        });

    }
}