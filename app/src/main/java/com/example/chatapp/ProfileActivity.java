package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
//import com.example.chatapp.glide.GlideApp;
import com.example.chatapp.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    Toolbar profileActivity_toolbar;
    CircleImageView profileImage;
    TextView username;
    private  int BACKUPIMAGE=2;
    FirebaseStorage storageInstance;
    StorageReference currentUserStorageRef;
    private FirebaseFirestore firestoreInstance;
    private DocumentReference currentUserDocRef;
    Map<String,Object> map ,map2;
    Context c;
    User user;
    ProgressBar progressBar;
    Button signOut;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        init();
        //user = (User) getIntent().getSerializableExtra("user");

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                MainActivity.x=0;
                Intent n =new Intent(ProfileActivity.this,SignInActivity.class);

                MainActivity.signOut=1;
                n.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(n);
                finish();
            }
        });
        setImage();


        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent n = new Intent();
                Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent,BACKUPIMAGE);
            }
        });
    }

    private void setImage() {
        username.setText(MainActivity.user.getName());
        Glide.with(c).load(MainActivity.user.getProfileImage()).placeholder(R.drawable.ic_account).into(profileImage);
    }

    private void init(){
        profileActivity_toolbar = findViewById(R.id.profile_toolbar);
        setSupportActionBar(profileActivity_toolbar);
        getSupportActionBar().setTitle(R.string.me);
        c=this;
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        map2 = new HashMap<>();
        signOut = findViewById(R.id.btn_signOut);
        progressBar = findViewById(R.id.progress_profile);
        storageInstance = FirebaseStorage.getInstance();
        currentUserStorageRef = storageInstance.getReference().child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
        firestoreInstance = FirebaseFirestore.getInstance();
        currentUserDocRef = firestoreInstance.document("users/"+FirebaseAuth.getInstance().getUid());
        profileImage = findViewById(R.id.circle_profile_image);
        username = findViewById(R.id.txt_username);

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==BACKUPIMAGE&&resultCode== Activity.RESULT_OK&&data!=null&&data.getData()!=null){
            profileImage.setImageURI(data.getData());
            //to compress the image
            Uri selectedImagePath = data.getData();
            Bitmap selectedImageBitmap = null;
            try {
                progressBar.setVisibility(View.VISIBLE);
                selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImagePath);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG,50,outputStream);
                byte[] selectedImageBytes = outputStream.toByteArray(); // now image is in this byte array
                uploadProfileImage(selectedImageBytes);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void uploadProfileImage(byte[] selectedImageBytes) {
        StorageReference ref = currentUserStorageRef.child("profilePictures/" + FirebaseAuth.getInstance().getUid());
        ref.putBytes(selectedImageBytes).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    onSuccess(selectedImageBytes);
                }else{
                    Toast.makeText(ProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void onSuccess(byte[] selectedImageBytes) {
        map = new HashMap<>();
        map.put("name", MainActivity.user.getName());
        map.put("token",MainActivity.user.getToken());
        currentUserStorageRef.child("profilePictures/" + FirebaseAuth.getInstance().getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if (uri!=null) {
                    //Toast.makeText(ProfileActivity.this, task.toString(), Toast.LENGTH_SHORT).show();
                    map.put("profileImage", uri.toString());
                    MainActivity.user.setProfileImage( uri.toString());
                    //Toast.makeText(getBaseContext(), map.get("profileImage").toString(), Toast.LENGTH_SHORT).show();
                    currentUserDocRef.update(map);
                    //GetProfileInfo.setUser(getApplicationContext());
                    progressBar.setVisibility(View.GONE);
                    setImage();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //startActivity(new Intent(this,MainActivity.class));
            finish();
            return true;
        }
        return false;
    }
}

