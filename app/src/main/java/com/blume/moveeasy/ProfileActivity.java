package com.blume.moveeasy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    TextView nameView, phoneView, emailView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //finding components
        nameView = findViewById(R.id.profileUsername);
        phoneView = findViewById(R.id.profilePhone);
        emailView = findViewById(R.id.profileEmail);

        String userId = FirebaseAuth.getInstance().getUid();
        getProfileInfo(userId);
    }

    private void getProfileInfo(String userId) {
        DatabaseReference profileInfoRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        profileInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    if(dataSnapshot.child("Username").exists()){
                        nameView.setText(dataSnapshot.child("Username").getValue().toString());
                    }
                    if(dataSnapshot.child("Phone").exists()){
                        phoneView.setText("0"+dataSnapshot.child("Phone").getValue().toString());
                    }
                    if(dataSnapshot.child("Email").exists()){
                        emailView.setText(dataSnapshot.child("Email").getValue().toString());
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
