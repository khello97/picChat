package se.umu.cs.khalil.picchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputEditText mEmail;
    private Button mResetButton;
    private FirebaseAuth mAuth;
    private ProgressBar mProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        initializeVariables();

        //ett mejl skickas till användaren där hen bes att återställa lösenord

        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString().trim();

                //kontrollerar att all input är korrekt
                if(email.isEmpty()){
                    mEmail.setError("Email is required");
                    mEmail.requestFocus();
                    return;
                }

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    mEmail.setError("Please provide a valid Email");
                    mEmail.requestFocus();
                    return;
                }

                mProgressBar.setVisibility(View.VISIBLE);
                //om något går fel, exempelvis att email-adressen inmatad existerar inte i databasen
                //kommer ett fel meddelande att visas
                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            Toast.makeText(ResetPasswordActivity.this, "Check your email to reset password", Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(ResetPasswordActivity.this, "Email provided is not found", Toast.LENGTH_LONG).show();
                        }

                        mProgressBar.setVisibility(View.GONE);
                    }
                });
            }
        });

    }

    private void initializeVariables() {
        mAuth = FirebaseAuth.getInstance();
        mEmail = (TextInputEditText) findViewById(R.id.email_reset_edit_text);
        mResetButton = (Button) findViewById(R.id.reset_Pass_Button);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar_reset);
    }
}