package com.example.chatapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.MainActivity;
import com.example.chatapp.R;
import com.example.chatapp.model.AudioMessage;
import com.example.chatapp.model.ImageMessage;
import com.example.chatapp.model.TextMessage;
import com.example.chatapp.model.User;
import com.keenfin.audioview.AudioService;
import com.keenfin.audioview.AudioView;
import com.keenfin.audioview.AudioView2;

import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.messageAdapterViewHolder> {
    ArrayList<Pair<Pair<TextMessage,AudioMessage>,Pair<ImageMessage, AudioMessage>>>messagePair;
    Context context;
    public static int i=0,j=0;
    MessageAdapterListener listener;
    String date="";

    public MessageAdapter(ArrayList<Pair<Pair<TextMessage, AudioMessage>, Pair<ImageMessage, AudioMessage>>> messagePair, Context context, MessageAdapterListener listener) {
        this.listener=listener;
        this.messagePair=messagePair;
        this.context = context;
    }

    @NonNull
    @Override
    public messageAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new messageAdapterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.message_layout, parent, false),listener,messagePair);

    }

    @Override
    public void onBindViewHolder(@NonNull messageAdapterViewHolder holder, int position) {
        if(messagePair.get(position).second==null&&messagePair.get(position).first.second==null) {
            holder.recceiverImage.setVisibility(View.GONE);
            holder.senderImage.setVisibility(View.GONE);
            holder.receiverAudio.setVisibility(View.GONE);
            holder.senderAudio.setVisibility(View.GONE);
            holder.senderVideo.setVisibility(View.GONE);
            holder.receiverVideo.setVisibility(View.GONE);
            holder.dateDay.setText(DateFormat.format("dd MMMM yyyy",messagePair.get(position).first.first.getDate()));

            if (messagePair.get(position).first.first.getUser() == 0) {
                holder.sender.setVisibility(View.VISIBLE);
                holder.receiver.setVisibility(View.GONE);
                holder.message.setText(messagePair.get(position).first.first.getText());

                holder.time.setText(DateFormat.format("hh:mm a", messagePair.get(position).first.first.getDate()).toString());
            } else {
                holder.receiver.setVisibility(View.VISIBLE);
                holder.sender.setVisibility(View.GONE);
                //holder.uImage.setVisibility(View.GONE);
                holder.message2.setText(messagePair.get(position).first.first.getText());
                holder.time2.setText(DateFormat.format("hh:mm a", messagePair.get(position).first.first.getDate()).toString());
            }
        }else if(messagePair.get(position).second==null&&messagePair.get(position).first.first==null) {
            holder.recceiverImage.setVisibility(View.GONE);
            holder.senderImage.setVisibility(View.GONE);
            holder.receiverAudio.setVisibility(View.GONE);
            holder.senderAudio.setVisibility(View.GONE);
            holder.sender.setVisibility(View.GONE);
            holder.receiver.setVisibility(View.GONE);
            holder.dateDay.setText(DateFormat.format("dd MMMM yyyy",messagePair.get(position).first.second.getDate()));
            if (messagePair.get(position).first.second.getUser() == 0) {
                holder.senderVideo.setVisibility(View.VISIBLE);
                holder.receiverVideo.setVisibility(View.GONE);
                holder.senderV.setVideoURI(Uri.parse(messagePair.get(position).first.second.getAudioUri()));
                MediaController mediaController=new MediaController(context);
                holder.senderV.setMediaController(mediaController);
                mediaController.setAnchorView(holder.senderV);
                holder.timeVideo.setText(DateFormat.format("hh:mm a", messagePair.get(position).first.second.getDate()).toString());
            } else {
                holder.receiverVideo.setVisibility(View.VISIBLE);
                holder.senderVideo.setVisibility(View.GONE);
                //holder.uImage.setVisibility(View.GONE);
                holder.receiverV.setVideoURI(Uri.parse(messagePair.get(position).first.second.getAudioUri()));

                MediaController mediaController=new MediaController(context);
                holder.receiverV.setMediaController(mediaController);
                mediaController.setAnchorView(holder.receiverV);
                holder.timeVideo.setText(DateFormat.format("hh:mm a", messagePair.get(position).first.second.getDate()).toString());
            }
        }

        else if(messagePair.get(position).first==null&&messagePair.get(position).second.second==null)
        {
            holder.sender.setVisibility(View.GONE);
            holder.receiver.setVisibility(View.GONE);
            holder.receiverAudio.setVisibility(View.GONE);
            holder.senderAudio.setVisibility(View.GONE);
            holder.receiverVideo.setVisibility(View.GONE);
            holder.senderVideo.setVisibility(View.GONE);
           /* if(date.equals(DateFormat.format("dd MMMM yyyy",messagePair.get(position).second.first.getDate()).toString())){
                holder.dateDay.setVisibility(View.GONE);
            }else{
                holder.dateDay.setVisibility(View.VISIBLE);
                holder.dateDay.setText(DateFormat.format("dd MMMM yyyy",messagePair.get(position).second.first.getDate()));
                date=DateFormat.format("dd MMMM yyyy",messagePair.get(position).second.first.getDate()).toString();
            }*/

            holder.dateDay.setText(DateFormat.format("dd MMMM yyyy",messagePair.get(position).second.first.getDate()));

            if(messagePair.get(position).second.first.getUser()==0)
            {
                holder.senderImage.setVisibility(View.VISIBLE);
                holder.recceiverImage.setVisibility(View.GONE);
                Glide.with(context).load(messagePair.get(position).second.first.getImageUri()).into(holder.senderM);
                holder.timeImage.setText(DateFormat.format("hh:mm a",messagePair.get(position).second.first.getDate()).toString());
            }else
            {
                //holder.uImage.setVisibility(View.VISIBLE);
                //Glide.with(context).load(MainActivity.user.getProfileImage()).into(holder.uImage);
                holder.recceiverImage.setVisibility(View.VISIBLE);
                holder.senderImage.setVisibility(View.GONE);
                Glide.with(context).load(messagePair.get(position).second.first.getImageUri()).into(holder.receiverM);
                holder.timeImage2.setText(DateFormat.format("hh:mm a",messagePair.get(position).second.first.getDate()).toString());
            }

        }else if(messagePair.get(position).first==null&&messagePair.get(position).second.first==null&&messagePair.get(position).second.second!=null)
        {
           // holder.uImage.setVisibility(View.GONE);
            holder.recceiverImage.setVisibility(View.GONE);
            holder.senderImage.setVisibility(View.GONE);
            holder.sender.setVisibility(View.GONE);
            holder.receiver.setVisibility(View.GONE);
            holder.receiverVideo.setVisibility(View.GONE);
            holder.senderVideo.setVisibility(View.GONE);
            /*if(date.equals(DateFormat.format("dd MMMM yyyy",messagePair.get(position).second.second.getDate()).toString())){
                holder.dateDay.setVisibility(View.GONE);
            }else{
                holder.dateDay.setVisibility(View.VISIBLE);
                holder.dateDay.setText(DateFormat.format("dd MMMM yyyy",messagePair.get(position).second.second.getDate()));
                date=DateFormat.format("dd MMMM yyyy",messagePair.get(position).second.second.getDate()).toString();
            }*/
            holder.dateDay.setText(DateFormat.format("dd MMMM yyyy",messagePair.get(position).second.second.getDate()));
            if(messagePair.get(position).second.second.getUser()==0)
            {
                holder.senderAudio.setVisibility(View.VISIBLE);
                holder.receiverAudio.setVisibility(View.GONE);

                holder.senderA.setTag(position);
                if (!holder.senderA.attached())
                    holder.senderA.setUpControls();
                try {
                    holder.senderA.setDataSource(messagePair.get(position).second.second.getAudioUri());
                } catch (IOException ignored) {
                }
                holder.timeAudio.setText(DateFormat.format("hh:mm a",messagePair.get(position).second.second.getDate()).toString());
            }else
            {
                holder.receiverAudio.setVisibility(View.VISIBLE);
                holder.senderAudio.setVisibility(View.GONE);
                holder.receiverA.setTag(position);
                if (!holder.receiverA.attached())
                    holder.receiverA.setUpControls();

                try {
                    holder.receiverA.setDataSource(messagePair.get(position).second.second.getAudioUri());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                holder.timeAudio2.setText(DateFormat.format("hh:mm a",messagePair.get(position).second.second.getDate()).toString());
            }

        }

    }

    @Override
    public int getItemCount() {
        return messagePair.size();


    }

    public static class messageAdapterViewHolder extends RecyclerView.ViewHolder {
        TextView message,time,message2,time2,timeImage,timeImage2,timeAudio,timeAudio2,dateDay,timeVideo,timeVideo2;
        LinearLayout senderImage,recceiverImage,senderAudio,receiverAudio,senderVideo,receiverVideo;
        FrameLayout sender,receiver;
        ImageView senderM,receiverM;
        AudioView2 senderA,receiverA;
        VideoView senderV,receiverV;
        CircleImageView uImage;
        @SuppressLint("CutPasteId")
        public messageAdapterViewHolder(@NonNull View itemView, MessageAdapterListener listener, ArrayList<Pair<Pair<TextMessage, AudioMessage>, Pair<ImageMessage, AudioMessage>>> messagePair) {
            super(itemView);
            senderV=itemView.findViewById(R.id.video);
            receiverV=itemView.findViewById(R.id.video2);
            message = itemView.findViewById(R.id.message_text);
            time = itemView.findViewById(R.id.messageTime);
            dateDay=itemView.findViewById(R.id.dayDate);
            message2 = itemView.findViewById(R.id.message_text2);
            time2 = itemView.findViewById(R.id.messageTime2);
            timeImage = itemView.findViewById(R.id.messageTime_image);
            timeImage2 = itemView.findViewById(R.id.messageTime_image2);
            timeAudio=itemView.findViewById(R.id.messageTime_audio);
            timeAudio2=itemView.findViewById(R.id.messageTime_audio2);
            timeVideo=itemView.findViewById(R.id.messageTime_video);
            timeVideo2=itemView.findViewById(R.id.messageTime_video2);
            sender = itemView.findViewById(R.id.linear_sender);
            receiver = itemView.findViewById(R.id.linear_receiver);
            senderImage = itemView.findViewById(R.id.linear_sender_image);
            recceiverImage = itemView.findViewById(R.id.linear_receiver_image);
            senderAudio=itemView.findViewById(R.id.linear_sender_audio);
            receiverAudio=itemView.findViewById(R.id.linear_receiver_audio);
            senderVideo=itemView.findViewById(R.id.linear_sender_video);
            receiverVideo=itemView.findViewById(R.id.linear_receiver_video);
            senderM = itemView.findViewById(R.id.image_sender);
            receiverM = itemView.findViewById(R.id.image_receiver);
            senderA=itemView.findViewById(R.id.audio);
            receiverA=itemView.findViewById(R.id.audio2);




            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return false;
                }
            });

            senderM.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onImageClicked(messagePair.get(getAdapterPosition()).second.first);
                }
            });

            receiverM.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onImageClicked(messagePair.get(getAdapterPosition()).second.first);
                }
            });


        }
    }
}
