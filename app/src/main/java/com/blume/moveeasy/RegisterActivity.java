package com.blume.moveeasy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.blume.moveeasy.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    EditText email, pword, uname, phone ;
    Button signup;
    ProgressBar progressBar;

    FirebaseAuth fbAuth;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mDatabase = database.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fbAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.regEmail);
        pword = findViewById(R.id.regPassword);
        progressBar = findViewById(R.id.progressBar3);
        uname = findViewById(R.id.username);
        phone = findViewById(R.id.phone);
        signup = findViewById(R.id.regButton);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                final String e_mail = email.getText().toString();
                String p_word = pword.getText().toString();

                 final String u_name = uname.getText().toString();
                 String uphone = phone.getText().toString();
                 final int u_phone = Integer.parseInt(uphone);

                if(e_mail.isEmpty()){
                    email.setError("Fill in email field");
                    email.requestFocus();
                }
                else if(p_word.isEmpty()){
                    pword.setError("Password field is empty");
                    pword.requestFocus();
                }
                else if(p_word.isEmpty() && e_mail.isEmpty()){
                    Toast.makeText(RegisterActivity.this, "Fields are empty!!", Toast.LENGTH_SHORT).show();
                }
                else if (!(p_word.isEmpty() && e_mail.isEmpty())){
                    progressBar.setVisibility(View.VISIBLE);
                    fbAuth.createUserWithEmailAndPassword(e_mail, p_word).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressBar.setVisibility(View.GONE);
                            if(!task.isSuccessful()){
                                Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            else {

                                User a_user = new User(fbAuth.getUid(),e_mail, u_name, u_phone);
                                mDatabase.child("Users").child(a_user.get_uid()).child("Email").setValue(a_user.get_email());
                                mDatabase.child("Users").child(a_user.get_uid()).child("Username").setValue(a_user.get_uname());
                                mDatabase.child("Users").child(a_user.get_uid()).child("Phone").setValue(a_user.get_phone());


                                FirebaseUser user = fbAuth.getCurrentUser();

                                user.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(RegisterActivity.this, "Verification sent via email", Toast.LENGTH_LONG).show();

                                                    Intent toLogin = new Intent(RegisterActivity.this, MainActivity.class);
                                                    startActivity(toLogin);

                                                }
                                            }
                                        });
                            }
                        }
                    });

                }
                else {
                    Toast.makeText(RegisterActivity.this, "An error occurred, Please try again in a few...", Toast.LENGTH_LONG).show();
                }

            }
        });
    }
}
