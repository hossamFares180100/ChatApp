package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.example.chatapp.fragments.ChatFragment;
import com.example.chatapp.fragments.DiscoverFragment;
import com.example.chatapp.fragments.PeopleFragment;
import com.example.chatapp.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    CircleImageView profileImg;
    ChatFragment chatFragment;
    PeopleFragment peopleFragment;
    DiscoverFragment discoverFragment;
    BottomNavigationView navigationView;
    Toolbar toolbar;
    static int x = 0;
    public static int signOut=0;
    DocumentReference currentUserDocRef ;
    public static User user;
    public static String sharedText;
    public static Uri imageUri;
    Map<String,Object> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        user = new User();
        map = new HashMap<>();
        currentUserDocRef  = FirebaseFirestore.getInstance().document("users/" + FirebaseAuth.getInstance().getUid());
        chatFragment = new ChatFragment();
        peopleFragment = new PeopleFragment();
        discoverFragment = new DiscoverFragment();
        profileImg=findViewById(R.id.circle_image_view_profile_image);
        navigationView = findViewById(R.id.bottomNavigationView);
        navigationView.setOnNavigationItemSelectedListener(this);
        setFragment(chatFragment);
        setImage();

        Intent intent = getIntent();
        String action = intent.getAction();
        String type=intent.getType();
        if(Intent.ACTION_SEND.equals(action)&&type!=null){
            if("text/plain".equals(type)){
                 sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            }else if(type.startsWith("image/")){
                 imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        }
        }

    }
    

    private void setImage() {

        currentUserDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    map = task.getResult().getData();
                    //assert map != null;
                    if(map!=null){
                    user.setName(Objects.requireNonNull(map.get("name")).toString());
                    user.setProfileImage(Objects.requireNonNull(map.get("profileImage")).toString());
                    user.setToken(map.get("token").toString());
                    Glide.with(getApplicationContext()).load(user.getProfileImage()).placeholder(R.drawable.ic_account).into(profileImg);
                    }
                }

            }
        });



    }



    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.navigation_chat_item:
                setFragment(chatFragment);
                return true;
            case R.id.navigation_people_item:
                setFragment(peopleFragment);
                return true;
            case R.id.navigation_more_item:
                setFragment(discoverFragment);
                return true;
            default:
                return false;
        }

    }

    private void setFragment(Fragment fragment) {

        FragmentTransaction fr = getSupportFragmentManager().beginTransaction();
        fr.replace(R.id.coordinatorLayout_main_content,fragment);
        fr.commit();

    }

    @Override
    protected void onStart() {
        setImage();
        super.onStart();
    }
}