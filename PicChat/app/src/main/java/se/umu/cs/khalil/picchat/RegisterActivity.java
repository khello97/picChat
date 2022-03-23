package se.umu.cs.khalil.picchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mUserNamesDatabaseReference;
    private TextInputEditText mUserName, mEmail, mFullName, mPassword, mRePassword;
    private Button mRegisterButton;
    private ProgressBar mProgressBar;
    private boolean runRegistration = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeVariables();

        //begränsar input för Email så att användaren håller sig till "email-form"
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                .showSoftInput(mEmail, InputMethodManager.SHOW_FORCED);

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
        

    }

    private void initializeVariables() {
        mUserNamesDatabaseReference = FirebaseDatabase.getInstance().getReference().child("userNames");
        mAuth = FirebaseAuth.getInstance();

        mProgressBar = (ProgressBar) findViewById(R.id.progressbar_register);
        mRegisterButton = (Button) findViewById(R.id.registerButton);
        mUserName = (TextInputEditText) findViewById(R.id.userName_register_edit_text);
        mEmail = (TextInputEditText) findViewById(R.id.Email_register_edit_text);
        mPassword = (TextInputEditText) findViewById(R.id.password_register_edit_text);
        mRePassword = (TextInputEditText) findViewById(R.id.re_password_register_edit_text);
        mFullName = (TextInputEditText) findViewById(R.id.full_name_register_edit_text);
    }

    private void registerUser() {

        String userName = mUserName.getText().toString().toLowerCase().trim();
        String email = mEmail.getText().toString().trim();
        String fullName = mFullName.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        String rePassword = mRePassword.getText().toString().trim();

        //Kontrollerar så all input är korrekt
        if (userName.isEmpty()) {
            mUserName.setError("Username is required");
            mUserName.requestFocus();
            return;
        }

        if (fullName.isEmpty()) {
            mFullName.setError("Full Name is required");
            mFullName.requestFocus();
            return;
        }

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

        if (!rePassword.equals(password)) {
            mRePassword.setError("The passwords does not match");
            mRePassword.requestFocus();
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);

        runRegistration = true;

        //Kontrollerar att username inte är upptagen
        mUserNamesDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (runRegistration) {
                    boolean userNameTaken = false;

                    for (DataSnapshot shot : snapshot.getChildren()) {


                        String user = shot.getValue(String.class);

                        if (user.toLowerCase().equals(userName)) {
                            userNameTaken = true;
                            mUserName.setError("Username is taken, try again");
                            mUserName.requestFocus();
                            mProgressBar.setVisibility(View.GONE);
                            break;
                        }
                    }

                    if (!userNameTaken) {

                        createUserWithEmailAndPassword(email, password, fullName, userName);
                    }
                    runRegistration = false;
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Funktionen nedan lägger till användarens email i autentisering-listan (FireBase) samt
    //skapar en användare i realtime-database, där den innehåller email, username och fullname
    //om något går fel kommer ett felmeddelande att skickas ut
    private void createUserWithEmailAndPassword(String email, String password, String fullName, String userName){

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){
                    User user = new User(userName, fullName);

                    FirebaseDatabase.getInstance().getReference("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                Toast.makeText(RegisterActivity.this, "Registration completed successfully", Toast.LENGTH_LONG).show();
                                mUserNamesDatabaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(userName);
                                user.sendEmailVerification();
                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));

                            } else{
                                    Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_LONG).show();
                            }
                            mProgressBar.setVisibility(View.GONE);
                        }
                    });

                }else{
                        Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_LONG).show();
                        mProgressBar.setVisibility(View.GONE);
                }
            }
        });

    }







}