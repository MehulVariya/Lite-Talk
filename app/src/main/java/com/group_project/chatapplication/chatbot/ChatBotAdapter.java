package com.group_project.chatapplication.chatbot;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.group_project.chatapplication.R;
import com.group_project.chatapplication.singleChat.single_chat_messages.Chatmodel;
import com.group_project.chatapplication.singleChat.single_chat_messages.Single_Chat_Doc_WebView_Activity;
import com.group_project.chatapplication.singleChat.single_chat_messages.Single_Chat_full_screen_photo_Activity;
import com.makeramen.roundedimageview.RoundedImageView;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChatBotAdapter extends RecyclerView.Adapter {

    public ArrayList<Map<String,String>> chatmodels;
    Context context;
    int SENDER_VIEW_TYPE = 1;
    int RECEIVER_VIEW_TYPE = 2;

    public ChatBotAdapter(Context context, ArrayList<Map<String,String>>  chatmodels){
        this.context = context;
        this.chatmodels = chatmodels;
    }

   public void addChatMode(Map<String,String> chatMode) {
        chatmodels.add(chatMode);
       notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER_VIEW_TYPE) {
            View view = LayoutInflater.from(context).inflate(R.layout.sender_message_ui, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.reciver_message_ui, parent, false);
            return new RecieverViewHolder(view);
        }
    }
    @Override
    public int getItemViewType(int position) {
        if (Objects.requireNonNull(chatmodels.get(position).get("Type")).equals("Person")) {
            return SENDER_VIEW_TYPE;
        } else {
            return RECEIVER_VIEW_TYPE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Map<String,String> data = chatmodels.get(position);
        String message = data.get("Message");
        String sender = data.get("Type");
        switch (holder.getItemViewType()) {
            case 1:
                SenderViewHolder holderSender = (SenderViewHolder)holder;
                holderSender.senderMsg.setText(message);
                holderSender.senderImage.setVisibility(View.GONE);
                holderSender.senderFile.setVisibility(View.GONE);
                holderSender.senderTime.setVisibility(View.GONE);
                holderSender.feeling.setVisibility(View.GONE);
                break;
            case 2:
                RecieverViewHolder holderReceiver= (RecieverViewHolder)holder;
                holderReceiver.receiverMsg.setText(message);
                holderReceiver.receiverFile.setVisibility(View.GONE);
                holderReceiver.receiverImage.setVisibility(View.GONE);
                holderReceiver.receiverTime.setVisibility(View.GONE);
                holderReceiver.feeling.setVisibility(View.GONE);
                break;
        }
    }


    @Override
    public int getItemCount() {
        return chatmodels.size();
    }


    public class RecieverViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout user_img_msg_layout, user_doc_msg_layout;
        TextView receiverMsg, receiverTime;
        RoundedImageView receiverImage, receiverFile;
        LinearLayout single_outer_message_layout,rec_message_layout;
        ImageView feeling;

        public RecieverViewHolder(@NonNull View itemView) {
            super(itemView);
            single_outer_message_layout = itemView.findViewById(R.id.single_outer_message_layout);
            receiverMsg = itemView.findViewById(R.id.single_user_txt_msg);
            receiverTime = itemView.findViewById(R.id.single_msg_time);
            user_img_msg_layout = itemView.findViewById(R.id.single_user_img_msg_layout);
            user_doc_msg_layout = itemView.findViewById(R.id.single_user_doc_msg_layout);
            receiverImage = itemView.findViewById(R.id.single_user_img_msg);
            receiverFile = itemView.findViewById(R.id.single_user_doc_msg);
            feeling=itemView.findViewById(R.id.feeling);
            rec_message_layout=itemView.findViewById(R.id.rec_message_layout);
        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout user_img_msg_layout, user_doc_msg_layout;
        TextView senderMsg, senderTime;
        RoundedImageView senderImage, senderFile;
        LinearLayout single_outer_message_layout,sen_message_layout;
        ImageView feeling;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            single_outer_message_layout = itemView.findViewById(R.id.single_outer_message_layout);
            senderMsg = itemView.findViewById(R.id.single_user_txt_msg);
            senderTime = itemView.findViewById(R.id.single_msg_time);
            user_img_msg_layout = itemView.findViewById(R.id.single_user_img_msg_layout);
            user_doc_msg_layout = itemView.findViewById(R.id.single_user_doc_msg_layout);
            senderImage = itemView.findViewById(R.id.single_user_img_msg);
            senderFile = itemView.findViewById(R.id.single_user_doc_msg);
            feeling=itemView.findViewById(R.id.feeling);
            sen_message_layout=itemView.findViewById(R.id.sen_message_layout);
        }
    }

    public static String longToDateString(long timestamp, String format) {
        return DateFormat.format(format, new Date(timestamp)).toString();
    }

}
