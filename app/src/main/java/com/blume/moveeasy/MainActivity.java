package com.blume.moveeasy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    EditText email, pword;
    Button signin, signup;

    FirebaseAuth mfbAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mfbAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.Email);
        pword = findViewById(R.id.password);
        signin = findViewById(R.id.login);
        signup = findViewById(R.id.register);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mfbUser = mfbAuth.getCurrentUser();
                if(mfbUser != null){
                    Toast.makeText(MainActivity.this, "You are logged in", Toast.LENGTH_SHORT).show();
                    Intent ToHome = new Intent(MainActivity.this, Main2Activity.class);
                    startActivity(ToHome);

                }
                else{
                    Toast.makeText(MainActivity.this, "Please log in...", Toast.LENGTH_SHORT).show();
                }
            }                                                                                                                            ;
        };



        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String e_mail = email.getText().toString();
                String p_word = pword.getText().toString();

                if(e_mail.isEmpty()){
                    email.setError("Fill in email field");
                    email.requestFocus();
                }
                else if(p_word.isEmpty()){
                    pword.setError("Password field is empty");
                    pword.requestFocus();
                }
                else if(p_word.isEmpty() && e_mail.isEmpty()){
                    Toast.makeText(MainActivity.this, "Fields are empty!!", Toast.LENGTH_SHORT).show();
                }
                else if (!(p_word.isEmpty() && e_mail.isEmpty())){
                    mfbAuth.signInWithEmailAndPassword(e_mail, p_word).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                               if(!task.isSuccessful()){
                                   Toast.makeText(MainActivity.this, "Authentication Failed :( , Please try again...", Toast.LENGTH_LONG).show();

                               }
                               else{
                                    Toast.makeText(MainActivity.this, "Sign-in Approved!! ;) ", Toast.LENGTH_LONG).show();

                                    Intent intToHome = new Intent(MainActivity.this, Main2Activity.class);
                                    startActivity(intToHome);
                               }
                        }
                    });
                }
                else {
                    Toast.makeText(MainActivity.this, "An error Occured, Please try again in a few moments", Toast.LENGTH_LONG).show();
                }

            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(i);

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mfbAuth.addAuthStateListener(mAuthStateListener);
    }
}
