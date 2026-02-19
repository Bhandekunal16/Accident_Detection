package com.example.accidentdetection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

public class LoginActivity extends AppCompatActivity {

    private EditText Email_editText, Password_editText;
    private TextView SignUp, ForgotPass, buttonText;
    private FirebaseAuth mAuth;
    private ProgressBar ProgressBar;
    private boolean passwordVisible;
    private View LoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Initialization();

        ForgotPass.setOnClickListener(view -> intentNow(ResetPassword.class));

        LoginButton.setOnClickListener(view -> {
            if (isConnected())
                Login();
        });

        SignUp.setOnClickListener(view -> intentNow(Register.class));

        // Show / Hide password
        Password_editText.setOnTouchListener((v, event) -> {
            final int RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (Password_editText.getCompoundDrawables()[RIGHT] != null &&
                        event.getRawX() >= (Password_editText.getRight()
                                - Password_editText.getCompoundDrawables()[RIGHT]
                                        .getBounds().width())) {

                    int selection = Password_editText.getSelectionEnd();

                    if (passwordVisible) {
                        Password_editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                0, 0, R.drawable.ic_visibility_off, 0);
                        Password_editText.setTransformationMethod(
                                PasswordTransformationMethod.getInstance());
                        passwordVisible = false;
                    } else {
                        Password_editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                0, 0, R.drawable.ic_visibility, 0);
                        Password_editText.setTransformationMethod(
                                HideReturnsTransformationMethod.getInstance());
                        passwordVisible = true;
                    }

                    Password_editText.setSelection(selection);
                    return true;
                }
            }
            return false;
        });
    }

    private void intentNow(Class<?> targetActivity) {
        Intent intent = new Intent(getApplicationContext(), targetActivity);
        startActivity(intent);
    }

    private void Login() {

        String email = Email_editText.getText().toString().trim();
        String pass = Password_editText.getText().toString().trim();

        if (email.isEmpty()) {
            Email_editText.setError("Field can't be empty");
            Email_editText.requestFocus();
            return;
        }

        if (pass.isEmpty()) {
            Password_editText.setError("Field can't be empty");
            Password_editText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Email_editText.setError("Please enter a valid email address");
            Email_editText.requestFocus();
            return;
        }

        if (pass.length() < 8) {
            Password_editText.setError("Password must be at least 8 characters");
            Password_editText.requestFocus();
            return;
        }

        ProgressBar.setVisibility(View.VISIBLE);
        buttonText.setVisibility(View.GONE);

        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {

                    ProgressBar.setVisibility(View.GONE);
                    buttonText.setVisibility(View.VISIBLE);

                    if (task.isSuccessful()) {
                        // Login success → go to main screen regardless of email verification
                        Intent intent = new Intent(LoginActivity.this, MainScreen.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        String message = "Failed to login";
                        if (task.getException() != null) {
                            message = task.getException().getMessage();
                        }
                        DynamicToast.makeError(this, message).show();
                    }
                });
    }

    private boolean isConnected() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }

        return false;
    }

    private void Initialization() {

        Email_editText = findViewById(R.id.email_box);
        Password_editText = findViewById(R.id.pass_box);
        SignUp = findViewById(R.id.SignUp_tv);
        ForgotPass = findViewById(R.id.forgotpass_tv);
        LoginButton = findViewById(R.id.login_button);
        ProgressBar = findViewById(R.id.buttonProgress);
        buttonText = findViewById(R.id.buttonText);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser) {

        if (currentUser != null) {

            Intent i = new Intent(LoginActivity.this, MainScreen.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
    }
}