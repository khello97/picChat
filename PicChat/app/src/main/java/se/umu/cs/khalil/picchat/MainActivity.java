package se.umu.cs.khalil.picchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText mEmail, mPassword;
    private TextView mRegister, mForgotPassword;
    private Button mLoginButton;
    private FirebaseAuth mAuth;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Disables darkmode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        initializeVariables();

        //begränsar input för Email så att användaren håller sig till "email-form"
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .showSoftInput(mEmail, InputMethodManager.SHOW_FORCED);

        mForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
            }
        });

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    private void initializeVariables() {
        mForgotPassword = (TextView) findViewById(R.id.forgot_password_Login);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar_login);
        mEmail = (TextInputEditText) findViewById(R.id.email_login_edit_text);
        mPassword = (TextInputEditText) findViewById(R.id.password_login_edit_text);
        mRegister = (TextView) findViewById(R.id.register_Login);
        mLoginButton = (Button) findViewById(R.id.login_button);
        mAuth = FirebaseAuth.getInstance();
    }

    private void loginUser() {
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();


        //Kontrollerar så all input är korrekt
        if (email.isEmpty()) {
            mEmail.setError("Email is required");
            mEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmail.setError("Please provide a valid Email");
            mEmail.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            mPassword.setError("Min password length should be 6 characters");
            mPassword.requestFocus();
            return;
        }

        mLoginButton.setEnabled(false);

        mProgressBar.setVisibility(View.VISIBLE);

        //Om inloggningen lyckas skickas användaren till loggedIn-activity, annars får användaren
        //ett felmeddelande
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if (user.isEmailVerified()) {

                        startActivity(new Intent(MainActivity.this, loggedIn.class));
                    } else {
                        user.sendEmailVerification();
                        Toast.makeText(MainActivity.this, "Check your email to verify your account", Toast.LENGTH_LONG).show();
                        mProgressBar.setVisibility(View.GONE);
                        mLoginButton.setEnabled(true);
                    }

                } else {
                    Toast.makeText(MainActivity.this, "Failed to login", Toast.LENGTH_LONG).show();
                    mProgressBar.setVisibility(View.GONE);
                    mLoginButton.setEnabled(true);
                }
            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}