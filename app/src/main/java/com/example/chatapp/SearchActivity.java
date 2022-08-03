package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;

import com.example.chatapp.adapters.ChatRecycleViewAdapter;
import com.example.chatapp.adapters.ChatRecycleViewListener;
import com.example.chatapp.model.TextMessage;
import com.example.chatapp.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class SearchActivity extends AppCompatActivity implements ChatRecycleViewListener  {
    Toolbar toolbar;
    FirebaseFirestore db;
    RecyclerView rvSearch;
    ArrayList<Pair<User,TextMessage>> chatPeople;
    ChatRecycleViewAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        toolbar = findViewById(R.id.toolbar_search);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        rvSearch = findViewById(R.id.recycleView_search);
        chatPeople = new ArrayList<>();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rvSearch.setLayoutManager(layoutManager);
        adapter = new ChatRecycleViewAdapter(this, chatPeople,this);
        rvSearch.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu,menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView s =((SearchView) menu.findItem(R.id.actionSearch).getActionView());
        s.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        s.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.isEmpty()){
                    return false;
                }
                Query query = db.collection("users")
                        .orderBy("name")
                        .startAt(newText.trim())
                        .endAt(newText.trim() + "\uf8ff");
                showResultSearch(query);
                return true;
            }
        });
        return  true;
    }

    private void showResultSearch(Query query) {
        final User[] user = {new User()};
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                chatPeople.clear();
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    user[0] = document.toObject(User.class);
                    user[0].setId(document.getId());
                    if(!chatPeople.contains(new Pair<>(user[0],null)))
                        chatPeople.add(new Pair<>(user[0],null));
                }
                adapter.notifyDataSetChanged();
            }
        });
    }


    @Override
    public void setOnClickListener(User user) {
        Intent n = new Intent(this, ChatActivity.class);
        n.putExtra("user", user);
        startActivity(n);

    }

    @Override
    public void setOnImageClickListener(User user) {

    }
}