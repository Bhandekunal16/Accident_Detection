package com.example.accidentdetection;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity {

    private EditText name, email, pass, repass, phone;
    private TextView login, buttonText;
    private View registerButton;
    private ProgressBar buttonProgress;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private boolean passwordVisible = false;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        phone = findViewById(R.id.phone);
        pass = findViewById(R.id.password);
        repass = findViewById(R.id.repassword);
        login = findViewById(R.id.login);
        registerButton = findViewById(R.id.login_button);
        buttonText = findViewById(R.id.buttonText);
        buttonProgress = findViewById(R.id.buttonProgress);

        buttonText.setText("Create Account");

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        registerButton.setOnClickListener(v -> validateAndRegister());

        login.setOnClickListener(v -> {
            startActivity(new Intent(Register.this, LoginActivity.class));
            finish();
        });

        setupPasswordToggle(pass);
        setupPasswordToggle(repass);
    }

    private void setupPasswordToggle(EditText editText) {
        editText.setOnTouchListener((v, event) -> {
            final int RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (editText.getCompoundDrawables()[RIGHT] != null &&
                        event.getRawX() >= (editText.getRight() -
                                editText.getCompoundDrawables()[RIGHT]
                                        .getBounds().width())) {

                    int selection = editText.getSelectionEnd();

                    if (passwordVisible) {
                        editText.setTransformationMethod(
                                PasswordTransformationMethod.getInstance());
                        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                0, 0, R.drawable.ic_visibility_off, 0);
                        passwordVisible = false;
                    } else {
                        editText.setTransformationMethod(
                                HideReturnsTransformationMethod.getInstance());
                        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                0, 0, R.drawable.ic_visibility, 0);
                        passwordVisible = true;
                    }

                    editText.setSelection(selection);
                    return true;
                }
            }
            return false;
        });
    }

    private void validateAndRegister() {

        String fullName = name.getText().toString().trim();
        String userEmail = email.getText().toString().trim();
        String password = pass.getText().toString().trim();
        String rePassword = repass.getText().toString().trim();
        String mobile = phone.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            name.setError("Enter full name");
            return;
        }

        if (TextUtils.isEmpty(userEmail)) {
            email.setError("Enter email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            pass.setError("Enter password");
            return;
        }

        if (password.length() < 6) {
            pass.setError("Password must be at least 6 characters");
            return;
        }

        if (!password.equals(rePassword)) {
            repass.setError("Passwords do not match");
            return;
        }

        if (TextUtils.isEmpty(mobile) || mobile.length() != 10) {
            phone.setError("Enter valid 10 digit number");
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        buttonProgress.setVisibility(View.VISIBLE);
        buttonText.setVisibility(View.GONE);

        firebaseAuth.createUserWithEmailAndPassword(userEmail, password)
                .addOnCompleteListener(task -> {

                    progressDialog.dismiss();
                    buttonProgress.setVisibility(View.GONE);
                    buttonText.setVisibility(View.VISIBLE);

                    if (task.isSuccessful()) {

                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                        if (firebaseUser != null) {

                            Model user = new Model();
                            user.setName(fullName);
                            user.setEmail(userEmail);
                            user.setMno(mobile);
                            user.setImageurl("default");

                            databaseReference.child(firebaseUser.getUid())
                                    .setValue(user);

                            Toast.makeText(Register.this,
                                    "Registration Successful",
                                    Toast.LENGTH_LONG).show();

                            startActivity(new Intent(Register.this,
                                    LoginActivity.class));
                            finish();
                        }

                    } else {
                        String errorMessage = task.getException() != null
                                ? task.getException().getMessage()
                                : "Registration Failed";

                        Toast.makeText(Register.this,
                                errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
