package com.example.alesha.percomsg;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class UserProfileData extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String TAG = " DATABASE";
    private Button saveB, loadB;
    private EditText first_name, last_name, bio;
    private int cnt=0;
    private DatabaseReference mDatabase;
    private String id, hobbiesChecked;
    private ArrayList<String> hobbyArr = new ArrayList<String>();
    private Button btnChoose, btnUpload;
    private ImageView imageView;
    private Uri filePath;
    Bitmap bitmap;

    private final int PICK_IMAGE_REQUEST = 71;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_data);

        saveB = (Button) findViewById(R.id.saveBtn);
        first_name = (EditText) findViewById(R.id.field_fname);
        last_name = (EditText) findViewById(R.id.field_lname);
        bio = (EditText) findViewById(R.id.field_bio);


        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        // Write a message to the database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        id = user.getUid().toString();

        saveB.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick (View v){
                if (user != null) {

                    UserData data = new UserData(first_name.getText().toString(), last_name.getText().toString(), bio.getText().toString(), hobbyArr);

                    mDatabase.child("data").child(user.getUid()).setValue(data, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Toast.makeText(UserProfileData.this, "Data could not be saved " + databaseError.getMessage(),
                                        Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(UserProfileData.this, "Data saved successfully.",
                                        Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                    ValueEventListener postListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get Post object and use the values to update the UI
                            UserData data = dataSnapshot.getValue(UserData.class);

                            if (data != null) {
                                first_name.setText(data.getFirst_name());
                                last_name.setText(data.getLast_name());
                                bio.setText(data.getBio());
                                hobbyArr = (data.getHobbyArr());

                            }
                            Intent i = new Intent(UserProfileData.this, UserProfile.class);
                            startActivity(i);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Getting Post failed, log a message
                            Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                        }
                    };
                    mDatabase.child("data").child(user.getUid().toString()).addListenerForSingleValueEvent(postListener);
                }
            }
        });
    }

    public void onCheckBoxClick(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        // Check which checkbox was clicked
        switch (view.getId()) {
            case R.id.readBox:
                if (checked) {
                    hobbyArr.add(" Read");
                    cnt++;
                }
                break;
            case R.id.hangoutBox:
                if (checked) {
                    hobbyArr.add(" Hangout with friends");
                    cnt++;

                }
                break;
            case R.id.videogameBox:
                if (checked) {
                    hobbyArr.add(" Play video games");
                    cnt++;

                }
                break;
            case R.id.workoutBox:
                if (checked) {
                    hobbyArr.add(" Workout");
                    cnt++;

                }
                break;
            case R.id.sleepBox:
                if (checked) {
                    hobbyArr.add(" Sleep");
                    cnt++;

                }
                break;
            case R.id.danceBox:
                if (checked) {
                    hobbyArr.add(" Dance");
                    cnt++;

                }
                break;
            case R.id.huntingBox:
                if (checked) {
                    hobbyArr.add(" Read,");
                    cnt++;

                }
                break;
            case R.id.cookingBox:
                if (checked) {
                    hobbyArr.add(" Cook");
                    cnt++;

                }
                break;
            case R.id.musicBox:
                if (checked) {
                    hobbyArr.add(" Listen to music");
                    cnt++;
                }
                break;
        }
    }
}



