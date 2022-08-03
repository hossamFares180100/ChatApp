package com.example.chatapp.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.ChatActivity;
import com.example.chatapp.MainActivity;
import com.example.chatapp.ProfileActivity;
import com.example.chatapp.R;
import com.example.chatapp.SearchActivity;
import com.example.chatapp.ShowImageActivity;
import com.example.chatapp.adapters.ChatRecycleViewAdapter;
import com.example.chatapp.adapters.ChatRecycleViewListener;
import com.example.chatapp.model.ImageMessage;
import com.example.chatapp.model.TextMessage;
import com.example.chatapp.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatFragment extends Fragment implements ChatRecycleViewListener {

    CircleImageView profileImage;
    FirebaseFirestore firestoreInstance;
    ArrayList<Pair<User,TextMessage>> chatPeople;
    ChatRecycleViewAdapter adapter;
    RecyclerView chats;
    Map<String,Object>map;
    DocumentReference currentUserDocumentRef;
    EditText searchEditText;
    ImageView imageViewSearch;
    CollectionReference chatChannelCollectionRef ;
    boolean flag = false;

    public ChatFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestoreInstance = FirebaseFirestore.getInstance();
        chatPeople = new ArrayList<>();
        map=new HashMap<>();
        chatChannelCollectionRef = firestoreInstance.collection("chatChannels");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        TextView titleToolbar = getActivity().findViewById(R.id.title_toolbar_textview);
        titleToolbar.setText("Chats");
        profileImage = getActivity().findViewById(R.id.circle_image_view_profile_image);

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("here","");
                Intent n = new Intent(getActivity(), ProfileActivity.class);
                n.putExtra("user", MainActivity.user);
                getActivity().startActivity(n);
            }
        });

        adapter = new ChatRecycleViewAdapter(this, chatPeople, getActivity());

        addChatListener();
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chat, container, false);
        chats = v.findViewById(R.id.recycle_chats);
        searchEditText = v.findViewById(R.id.editText_search);
        imageViewSearch = v.findViewById(R.id.imageViewSearch);
        imageViewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(new Intent(getActivity(), SearchActivity.class));
            }
        });
        chats.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        chats.setAdapter(adapter);
        return v;
    }
    private void addChatListener() {
        firestoreInstance.collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("sharedChat")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            return;
                        }
                        for (DocumentSnapshot document : Objects.requireNonNull(value).getDocuments()) {
                            chatPeople.clear();
                            if (document.exists()) {
                                currentUserDocumentRef = firestoreInstance.document("users/" + document.getId());
                                currentUserDocumentRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        map = documentSnapshot.getData();
                                        User user = new User();
                                        assert user != null;
                                        user.setId(document.getId());
                                        user.setProfileImage(map.get("profileImage").toString());
                                        user.setName(map.get("name").toString());
                                        user.setToken(map.get("token").toString());
                                        Query query = chatChannelCollectionRef.document(document.get("channelId").toString()).collection("messages").
                                                orderBy("date", Query.Direction.DESCENDING);
                                        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                if(error!=null){
                                                    return;
                                                }
                                                if (!value.getDocuments().isEmpty()) {
                                                    if (value.getDocuments() != null) {
                                                        for (int i = 0; i < chatPeople.size(); i++) {
                                                            if (chatPeople.get(i).first == user) {
                                                                chatPeople.remove(i);
                                                                adapter.notifyItemRemoved(i);
                                                                adapter.notifyDataSetChanged();
                                                                flag = true;
                                                                Log.e("result2:",chatPeople.size()+"");
                                                            }
                                                        }
                                                        if (flag) {
                                                            chatPeople.add(0, new Pair<>(user, value.getDocuments().get(0).toObject(TextMessage.class)));
                                                        } else {
                                                            chatPeople.add(new Pair<>(user, value.getDocuments().get(0).toObject(TextMessage.class)));
                                                        }
                                                        Log.e("result:",chatPeople.size()+"");
                                                        adapter.notifyDataSetChanged();
                                                        flag = false;

                                                    }
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    }
                });
    }

    @Override
    public void setOnClickListener(User user) {
        Intent n = new Intent(getActivity(), ChatActivity.class);
        n.putExtra("user", user);
        if(MainActivity.sharedText!=null){
            n.putExtra("sharedText",MainActivity.sharedText);
        }
        if(MainActivity.imageUri!=null){
            n.putExtra("sharedImage",MainActivity.imageUri);
        }
        getActivity().startActivity(n);

    }

    @Override
    public void setOnImageClickListener(User user) {
        Intent n = new Intent(getActivity(), ShowImageActivity.class);

        ImageMessage imageMessage=new ImageMessage();
        imageMessage.setImageUri(user.getProfileImage());
        n.putExtra("imageMessage",imageMessage);
        getActivity().startActivity(n);
    }
}