package com.example.chatapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.model.ChatRecycleItem;
import com.example.chatapp.model.TextMessage;
import com.example.chatapp.model.User;

import java.util.ArrayList;
import java.util.Comparator;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatRecycleViewAdapter  extends RecyclerView.Adapter<ChatRecycleViewAdapter.ChatRecycleViewHolder> {
    ChatRecycleViewListener listener;
    ArrayList<Pair<User,TextMessage>> items;
    Context context;



    public ChatRecycleViewAdapter(ChatRecycleViewListener listener, ArrayList<Pair<User,TextMessage>> items, Context c) {
        this.listener = listener;
        this.items = items;
        context=c;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)


    @NonNull
    @Override
    public ChatRecycleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChatRecycleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item, parent, false), listener,items);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ChatRecycleViewHolder holder, int position) {

        holder.name.setText(items.get(position).first.getName());
        if(items.get(position).second!=null){
            holder.lastMessage.setText(items.get(position).second.getText());
            holder.time.setText(DateFormat.format("hh:mm a",items.get(position).second.getDate()).toString());
        }else{
            holder.lastMessage.setText("Attachment");
        }
        Glide.with(context).load(items.get(position).first.getProfileImage()).into(holder.profileImage);

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ChatRecycleViewHolder extends RecyclerView.ViewHolder {
        TextView name,time,lastMessage;
        CircleImageView profileImage;
        CheckBox checkBox;
        public ChatRecycleViewHolder(@NonNull View itemView, ChatRecycleViewListener listener, ArrayList<Pair<User, TextMessage>> items) {
            super(itemView);
            name = itemView.findViewById(R.id.item_name_textview);
            time = itemView.findViewById(R.id.item_time_textview);
            lastMessage = itemView.findViewById(R.id.item_last_message_textview);
            profileImage = itemView.findViewById(R.id.item_circle_image_view);
            profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.setOnImageClickListener(items.get(getAdapterPosition()).first);
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.setOnClickListener(items.get(getAdapterPosition()).first);
                }
            });
        }
    }
}
