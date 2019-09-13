package com.blume.moveeasy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    EditText email, pword;
    ImageButton signup;

    FirebaseAuth fbAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fbAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.regEmail);
        pword = findViewById(R.id.regPassword);
        signup = findViewById(R.id.regButton);
        signup.setOnClickListener(new View.OnClickListener() {
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
                    Toast.makeText(RegisterActivity.this, "Fields are empty!!", Toast.LENGTH_SHORT).show();
                }
                else if (!(p_word.isEmpty() && e_mail.isEmpty())){
                    fbAuth.createUserWithEmailAndPassword(e_mail, p_word).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(RegisterActivity.this, "Sign up unsuccessful :(", Toast.LENGTH_SHORT).show();
                            }
                            else {

                            }
                        }
                    });

                }
                else {
                    Toast.makeText(RegisterActivity.this, "An error Occured, Please try again in a few moments", Toast.LENGTH_LONG).show();
                }



            }
        });
    }
}
