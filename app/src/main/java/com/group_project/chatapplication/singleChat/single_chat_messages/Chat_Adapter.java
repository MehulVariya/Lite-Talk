package com.group_project.chatapplication.singleChat.single_chat_messages;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableResource;
import com.bumptech.glide.load.resource.drawable.ResourceDrawableDecoder;
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
import com.makeramen.roundedimageview.RoundedImageView;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class Chat_Adapter extends RecyclerView.Adapter {

    ArrayList<Chatmodel> chatmodels;
    String senderID, receiverId, receiver, encoded_deleted_already_msg = "VGhpcyBtZXNzYWdlIHdhcyBkZWxldGVk"; // This message was deleted
    Context context;
    FirebaseAuth auth;
    int SENDER_VIEW_TYPE = 1;
    int RECEIVER_VIEW_TYPE = 2;

    public Chat_Adapter(Context context, ArrayList<Chatmodel> chatmodels, String senderID, String receiverId, String receiver) {
        this.chatmodels = chatmodels;
        this.context = context;
        this.senderID = senderID;
        this.receiverId = receiverId;
        this.receiver = receiver;
        auth = FirebaseAuth.getInstance();
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

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String myMobileNo;

    {
        assert user != null;
        myMobileNo = Objects.requireNonNull(user.getPhoneNumber()).replace("+91", "");
    }

    @Override
    public int getItemViewType(int position) {
        if (chatmodels.get(position).getSender().equals(myMobileNo)) {
            return SENDER_VIEW_TYPE;
        } else {
            return RECEIVER_VIEW_TYPE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Chatmodel chatmodel = chatmodels.get(position);
        String timestamp = chatmodel.getTimestamp();
        String message = chatmodel.getMessage();//text,image
        String sender = chatmodel.getSender();
        String messageType = chatmodel.getType();


        int reactions[] = new int[]{
                R.drawable.bapu,
                R.drawable.thumb,
                R.drawable.sweat,
                R.drawable.smiling_face,
                R.drawable.thinking,
                R.drawable.jibh_bar,

        };
        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();
        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {
            try {
                if (holder.getClass() == SenderViewHolder.class){
                    SenderViewHolder viewHolder=(SenderViewHolder) holder;
                    viewHolder.feeling.setImageResource(reactions[pos]);
                }else {
                    RecieverViewHolder viewHolder=(RecieverViewHolder) holder;
                    viewHolder.feeling.setImageResource(reactions[pos]);
                }
            }catch (Exception e){
                e.printStackTrace();
            }


            chatmodel.setFeeling(pos);
            DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Chat");
            reference.child(myMobileNo).child(senderID).child("Messages").child(chatmodel.getTimestamp()).setValue(chatmodel);

            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Chat");
            ref.child(receiver).child(receiverId).child("Messages").child(chatmodel.getTimestamp()).setValue(chatmodel);
            return true;

        });



        // Text display code...
        if (messageType.equals("text")) {
            if (holder.getClass() == SenderViewHolder.class) {
                byte[] data = Base64.decode(chatmodel.getMessage(), Base64.DEFAULT);
                String text = new String(data, StandardCharsets.UTF_8);
                ((SenderViewHolder) holder).senderMsg.setText(text);
                String senderMsgTime = longToDateString(Long.parseLong(chatmodel.getTimestamp()), "dd-MM-yy hh:mm");
                ((SenderViewHolder) holder).senderTime.setText(senderMsgTime);

                ((SenderViewHolder) holder).senderMsg.setVisibility(View.VISIBLE);
                ((SenderViewHolder) holder).user_doc_msg_layout.setVisibility(View.GONE);
                ((SenderViewHolder) holder).user_img_msg_layout.setVisibility(View.GONE);

            } else {
                byte[] data = Base64.decode(chatmodel.getMessage(), Base64.DEFAULT);
                String text = new String(data, StandardCharsets.UTF_8);
                ((RecieverViewHolder) holder).receiverMsg.setText(text);
                String reciverMsgTime = longToDateString(Long.parseLong(chatmodel.getTimestamp()), "dd-MM-yy hh:mm");
                ((RecieverViewHolder) holder).receiverTime.setText(reciverMsgTime);

                ((RecieverViewHolder) holder).receiverMsg.setVisibility(View.VISIBLE);
                ((RecieverViewHolder) holder).user_doc_msg_layout.setVisibility(View.GONE);
                ((RecieverViewHolder) holder).user_img_msg_layout.setVisibility(View.GONE);
            }

        }

        // Image display and image-open code...
        if (messageType.equals("image")) {
            if (holder.getClass() == SenderViewHolder.class) {
                byte[] data = Base64.decode(message, Base64.DEFAULT);
                String text = new String(data, StandardCharsets.UTF_8);
                try {
                    Glide.with(((SenderViewHolder) holder).senderImage).load(text).placeholder(R.drawable.default_image_for_chat).into(((SenderViewHolder) holder).senderImage);
                } catch (Exception e) {
                    ((SenderViewHolder) holder).senderImage.setImageResource(R.drawable.default_image_for_chat);
                }
                String senderMsgTime = longToDateString(Long.parseLong(chatmodel.getTimestamp()), "dd-MM-yy hh:mm");
                ((SenderViewHolder) holder).senderTime.setText(senderMsgTime);

                ((SenderViewHolder) holder).senderMsg.setVisibility(View.GONE);
                ((SenderViewHolder) holder).user_doc_msg_layout.setVisibility(View.GONE);


                ((SenderViewHolder) holder).senderImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, Single_Chat_full_screen_photo_Activity.class);
                        intent.putExtra("image", text);
                        intent.putExtra("sender", sender);
                        context.startActivity(intent);
                    }
                });
            } else {
                byte[] data = Base64.decode(message, Base64.DEFAULT);
                String text = new String(data, StandardCharsets.UTF_8);
                try {
                    Glide.with(((RecieverViewHolder) holder).receiverImage).load(text).placeholder(R.drawable.default_image_for_chat).into(((RecieverViewHolder) holder).receiverImage);
                } catch (Exception e) {
                    ((RecieverViewHolder) holder).receiverImage.setImageResource(R.drawable.default_image_for_chat);
                }
                String receiverMsgTime = longToDateString(Long.parseLong(chatmodel.getTimestamp()), "dd-MM-yy hh:mm");
                ((RecieverViewHolder) holder).receiverTime.setText(receiverMsgTime);

                ((RecieverViewHolder) holder).receiverMsg.setVisibility(View.GONE);
                ((RecieverViewHolder) holder).user_doc_msg_layout.setVisibility(View.GONE);

                ((RecieverViewHolder) holder).receiverImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, Single_Chat_full_screen_photo_Activity.class);
                        intent.putExtra("image", text);
                        intent.putExtra("sender", sender);
                        context.startActivity(intent);
                    }
                });
            }
        }

        //file display and click code
        if (messageType.equals("file")) {
            //document message
            if (holder.getClass() == SenderViewHolder.class) {
                String senderMsgTime = longToDateString(Long.parseLong(chatmodel.getTimestamp()), "dd-MM-yy hh:mm");
                ((SenderViewHolder) holder).senderTime.setText(senderMsgTime);

                ((SenderViewHolder) holder).user_doc_msg_layout.setVisibility(View.VISIBLE);
                ((SenderViewHolder) holder).senderMsg.setVisibility(View.GONE);
                ((SenderViewHolder) holder).senderImage.setVisibility(View.GONE);
                byte[] data = Base64.decode(message, Base64.DEFAULT);
                String text = new String(data, StandardCharsets.UTF_8);
                ((SenderViewHolder) holder).senderFile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, Single_Chat_Doc_WebView_Activity.class);
                        intent.putExtra("pass_pdf_url", text);
                        intent.putExtra("sender", sender);
                        context.startActivity(intent);
                    }
                });
            } else {
                String receiverMsgTime = longToDateString(Long.parseLong(chatmodel.getTimestamp()), "dd-MM-yy hh:mm");
                ((RecieverViewHolder) holder).receiverTime.setText(receiverMsgTime);

                ((RecieverViewHolder) holder).user_doc_msg_layout.setVisibility(View.VISIBLE);
                ((RecieverViewHolder) holder).receiverMsg.setVisibility(View.GONE);
                ((RecieverViewHolder) holder).receiverImage.setVisibility(View.GONE);
                byte[] data = Base64.decode(message, Base64.DEFAULT);
                String text = new String(data, StandardCharsets.UTF_8);
                ((RecieverViewHolder) holder).receiverFile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, Single_Chat_Doc_WebView_Activity.class);
                        intent.putExtra("pass_pdf_url", text);
                        intent.putExtra("sender", sender);
                        context.startActivity(intent);
                    }
                });
            }
        }


        //SENDER side delete code for sender text,image & file..
        if (holder.getClass() == SenderViewHolder.class) {

            if (chatmodel.getFeeling()>=0){
                //chatmodel.setFeeling(reactions[(int) chatmodel.getFeeling()]);
                ((SenderViewHolder)holder).feeling.setImageResource(reactions[chatmodel.getFeeling()]);
                ((SenderViewHolder)holder).feeling.setVisibility(View.VISIBLE);
            }else {
                ((SenderViewHolder)holder).feeling.setVisibility(View.GONE);
            }

            ((SenderViewHolder)holder).sen_message_layout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });
//            ((SenderViewHolder)holder).sen_message_layout.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    showPopupMenu(context,v);
//                }
//
//            });


            // This message was deleted
            if (message.equals(encoded_deleted_already_msg)) {
                byte[] data = Base64.decode(message, Base64.DEFAULT);
                String text = new String(data, StandardCharsets.UTF_8);
                ((SenderViewHolder) holder).senderMsg.setText(text);
                ((SenderViewHolder) holder).senderMsg.setVisibility(View.VISIBLE);
                ((SenderViewHolder) holder).senderImage.setVisibility(View.GONE);
                ((SenderViewHolder) holder).user_img_msg_layout.setVisibility(View.GONE);
                ((SenderViewHolder) holder).senderFile.setVisibility(View.GONE);
                ((SenderViewHolder) holder).user_doc_msg_layout.setVisibility(View.GONE);
            }

            //delete sender text message
            ((SenderViewHolder) holder).single_outer_message_layout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure to Delete this message?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String msgTimestamp = chatmodels.get(position).getTimestamp();
                            DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("Chat");
                            Query query = dbref.child(myMobileNo).child(senderID).child("Messages")
                                    .orderByChild("timestamp").equalTo(msgTimestamp);
                            Query query1 = dbref.child(receiver).child(receiverId).child("Messages")
                                    .orderByChild("timestamp").equalTo(msgTimestamp);
                            //first entry delete code
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        String msg = ds.child("message").getValue().toString();
                                        if (msg.equals(encoded_deleted_already_msg)) { // This message was deleted
                                            ds.getRef().removeValue();
                                        } else {
                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            hashMap.put("message", encoded_deleted_already_msg); // This message was deleted
                                            ds.getRef().updateChildren(hashMap);
                                        }
                                    }
                                    //second entry delete code
                                    query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds : snapshot.getChildren()) {
                                                String msg = ds.child("message").getValue().toString();
                                                if (msg.equals(encoded_deleted_already_msg)) { // This message was deleted
                                                    ds.getRef().removeValue();
                                                } else {
                                                    HashMap<String, Object> hashMap = new HashMap<>();
                                                    hashMap.put("message", encoded_deleted_already_msg); // This message was deleted
                                                    ds.getRef().updateChildren(hashMap);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                        }
                                    });//second entry end

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                }
                            });//first entry end
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                    return false;
                }
            });

            //delete sender Image code
            ((SenderViewHolder) holder).senderImage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure to Delete this message?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String msgTimestamp = chatmodels.get(position).getTimestamp();
                            DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("Chat");
                            Query query = dbref.child(myMobileNo).child(senderID).child("Messages")
                                    .orderByChild("timestamp").equalTo(msgTimestamp);
                            Query query1 = dbref.child(receiver).child(receiverId).child("Messages")
                                    .orderByChild("timestamp").equalTo(msgTimestamp);
                            //first entry delete code
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        String msg = ds.child("message").getValue().toString();
                                        if (msg.equals(encoded_deleted_already_msg)) { // This message was deleted
                                            ds.getRef().removeValue();
                                        } else {
                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            hashMap.put("message", encoded_deleted_already_msg); // This message was deleted
                                            ds.getRef().updateChildren(hashMap);
                                            //image delete from the firebase storage
                                            byte[] data = Base64.decode(msg, Base64.DEFAULT);
                                            String text = new String(data, StandardCharsets.UTF_8);
                                            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                                            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(text);
                                            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    //image deleted from the firebase storage
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception exception) {
                                                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                    }
                                    //second entry delete code
                                    query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds : snapshot.getChildren()) {
                                                String msg = ds.child("message").getValue().toString();
                                                if (msg.equals(encoded_deleted_already_msg)) { // This message was deleted
                                                    ds.getRef().removeValue();
                                                } else {
                                                    HashMap<String, Object> hashMap = new HashMap<>();
                                                    hashMap.put("message", encoded_deleted_already_msg); // This message was deleted
                                                    ds.getRef().updateChildren(hashMap);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                        }
                                    });//end

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                }
                            });//end
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                    return false;
                }
            });
            ((SenderViewHolder) holder).user_img_msg_layout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure to Delete this message?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String msgTimestamp = chatmodels.get(position).getTimestamp();
                            DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("Chat");
                            Query query = dbref.child(myMobileNo).child(senderID).child("Messages")
                                    .orderByChild("timestamp").equalTo(msgTimestamp);
                            Query query1 = dbref.child(receiver).child(receiverId).child("Messages")
                                    .orderByChild("timestamp").equalTo(msgTimestamp);
                            //first entry delete code
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        String msg = ds.child("message").getValue().toString();
                                        if (msg.equals(encoded_deleted_already_msg)) { // This message was deleted
                                            ds.getRef().removeValue();
                                        } else {
                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            hashMap.put("message", encoded_deleted_already_msg); // This message was deleted
                                            ds.getRef().updateChildren(hashMap);
                                            //image delete from the firebase storage
                                            byte[] data = Base64.decode(msg, Base64.DEFAULT);
                                            String text = new String(data, StandardCharsets.UTF_8);
                                            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                                            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(text);
                                            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    //image deleted from the firebase storage
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception exception) {
                                                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                    }
                                    //second entry delete code
                                    query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds : snapshot.getChildren()) {
                                                String msg = ds.child("message").getValue().toString();
                                                if (msg.equals(encoded_deleted_already_msg)) { // This message was deleted
                                                    ds.getRef().removeValue();
                                                } else {
                                                    HashMap<String, Object> hashMap = new HashMap<>();
                                                    hashMap.put("message", encoded_deleted_already_msg); // This message was deleted
                                                    ds.getRef().updateChildren(hashMap);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                        }
                                    });//end

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                }
                            });//end
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                    return false;
                }
            });

            //delete sender file code
            ((SenderViewHolder) holder).senderFile.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure to Delete this message?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String msgTimestamp = chatmodels.get(position).getTimestamp();
                            DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("Chat");
                            Query query = dbref.child(myMobileNo).child(senderID).child("Messages")
                                    .orderByChild("timestamp").equalTo(msgTimestamp);
                            Query query1 = dbref.child(receiver).child(receiverId).child("Messages")
                                    .orderByChild("timestamp").equalTo(msgTimestamp);
                            //first entry delete code
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        String msg = ds.child("message").getValue().toString();
                                        if (msg.equals(encoded_deleted_already_msg)) { // This message was deleted
                                            ds.getRef().removeValue();
                                        } else {
                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            hashMap.put("message", encoded_deleted_already_msg); // This message was deleted
                                            ds.getRef().updateChildren(hashMap);
                                            //file delete from the firebase storage
                                            byte[] data = Base64.decode(msg, Base64.DEFAULT);
                                            String text = new String(data, StandardCharsets.UTF_8);
                                            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                                            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(text);
                                            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    //file deleted from the firebase storage
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception exception) {
                                                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                    }
                                    //second entry delete code
                                    query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds : snapshot.getChildren()) {
                                                String msg = ds.child("message").getValue().toString();
                                                if (msg.equals(encoded_deleted_already_msg)) { // This message was deleted
                                                    ds.getRef().removeValue();
                                                } else {
                                                    HashMap<String, Object> hashMap = new HashMap<>();
                                                    hashMap.put("message", encoded_deleted_already_msg); // This message was deleted
                                                    ds.getRef().updateChildren(hashMap);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                        }
                                    });//end

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                }
                            });//end
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                    return false;
                }
            });
            ((SenderViewHolder) holder).user_doc_msg_layout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure to Delete this message?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String msgTimestamp = chatmodels.get(position).getTimestamp();
                            DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("Chat");
                            Query query = dbref.child(myMobileNo).child(senderID).child("Messages")
                                    .orderByChild("timestamp").equalTo(msgTimestamp);
                            Query query1 = dbref.child(receiver).child(receiverId).child("Messages")
                                    .orderByChild("timestamp").equalTo(msgTimestamp);
                            //first entry delete code
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        String msg = ds.child("message").getValue().toString();
                                        if (msg.equals(encoded_deleted_already_msg)) { // This message was deleted
                                            ds.getRef().removeValue();
                                        } else {
                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            hashMap.put("message", encoded_deleted_already_msg); // This message was deleted
                                            ds.getRef().updateChildren(hashMap);
                                            //file delete from the firebase storage
                                            byte[] data = Base64.decode(msg, Base64.DEFAULT);
                                            String text = new String(data, StandardCharsets.UTF_8);
                                            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                                            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(text);
                                            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    //file deleted from the firebase storage
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception exception) {
                                                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                    }
                                    //second entry delete code
                                    query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds : snapshot.getChildren()) {
                                                String msg = ds.child("message").getValue().toString();
                                                if (msg.equals(encoded_deleted_already_msg)) { // This message was deleted
                                                    ds.getRef().removeValue();
                                                } else {
                                                    HashMap<String, Object> hashMap = new HashMap<>();
                                                    hashMap.put("message", encoded_deleted_already_msg); // This message was deleted
                                                    ds.getRef().updateChildren(hashMap);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                        }
                                    });//end

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                }
                            });//end
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                    return false;
                }
            });

        }

        //delete code for receiver text,image & file..
        if (holder.getClass() == RecieverViewHolder.class) {

            if (chatmodel.getFeeling()>=0){
                // chatmodel.setFeeling(reactions[(int) chatmodel.getFeeling()]);
                ((RecieverViewHolder)holder).feeling.setImageResource(reactions[chatmodel.getFeeling()]);
                ((RecieverViewHolder)holder).feeling.setVisibility(View.VISIBLE);
            }else {
                ((RecieverViewHolder)holder).feeling.setVisibility(View.GONE);
            }

            ((RecieverViewHolder)holder).rec_message_layout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });


            //delete receiver text message
            ((RecieverViewHolder) holder).single_outer_message_layout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure to Delete this message?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String msgTimestamp = chatmodels.get(position).getTimestamp();
                            DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("Chat");
                            Query query = dbref.child(myMobileNo).child(senderID).child("Messages")
                                    .orderByChild("timestamp").equalTo(msgTimestamp);
                            Query query1 = dbref.child(receiver).child(receiverId).child("Messages")
                                    .orderByChild("timestamp").equalTo(msgTimestamp);
                            //first entry delete code
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        String msg = ds.child("message").getValue().toString();
                                        if (msg.equals(encoded_deleted_already_msg)) { // This message was deleted
                                            ds.getRef().removeValue();
                                        } else {
                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            hashMap.put("message", encoded_deleted_already_msg); // This message was deleted
                                            ds.getRef().updateChildren(hashMap);
                                        }
                                    }
                                    //second entry delete code
                                    query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds : snapshot.getChildren()) {
                                                String msg = ds.child("message").getValue().toString();
                                                if (msg.equals(encoded_deleted_already_msg)) { // This message was deleted
                                                    ds.getRef().removeValue();
                                                } else {
                                                    HashMap<String, Object> hashMap = new HashMap<>();
                                                    hashMap.put("message", encoded_deleted_already_msg); // This message was deleted
                                                    ds.getRef().updateChildren(hashMap);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                        }
                                    });//second entry end

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                }
                            });//first entry end
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                    return false;
                }
            });

            // This message was deleted
            if (message.equals(encoded_deleted_already_msg)) { // This message was deleted
                byte[] data = Base64.decode(message, Base64.DEFAULT);
                String text = new String(data, StandardCharsets.UTF_8);
                ((RecieverViewHolder) holder).receiverMsg.setText(text);
                ((RecieverViewHolder) holder).receiverMsg.setVisibility(View.VISIBLE);
                ((RecieverViewHolder) holder).receiverImage.setVisibility(View.GONE);
                ((RecieverViewHolder) holder).user_img_msg_layout.setVisibility(View.GONE);
                ((RecieverViewHolder) holder).receiverFile.setVisibility(View.GONE);
                ((RecieverViewHolder) holder).user_doc_msg_layout.setVisibility(View.GONE);
            }

            //delete receiver Image code
            ((RecieverViewHolder) holder).receiverImage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure to Delete this message?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String msgTimestamp = chatmodels.get(position).getTimestamp();
                            DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("Chat");
                            Query query = dbref.child(myMobileNo).child(senderID).child("Messages")
                                    .orderByChild("timestamp").equalTo(msgTimestamp);
                            Query query1 = dbref.child(receiver).child(receiverId).child("Messages")
                                    .orderByChild("timestamp").equalTo(msgTimestamp);
                            //first entry delete code
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        String msg = ds.child("message").getValue().toString();
                                        if (msg.equals(encoded_deleted_already_msg)) { // This message was deleted
                                            ds.getRef().removeValue();
                                        } else {
                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            hashMap.put("message", encoded_deleted_already_msg); // This message was deleted
                                            ds.getRef().updateChildren(hashMap);
                                            //image delete from the firebase storage
                                            byte[] data = Base64.decode(msg, Base64.DEFAULT);
                                            String text = new String(data, StandardCharsets.UTF_8);
                                            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                                            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(text);
                                            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    //image deleted from the firebase storage
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception exception) {
                                                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                    }
                                    //second entry delete code
                                    query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds : snapshot.getChildren()) {
                                                String msg = ds.child("message").getValue().toString();
                                                if (msg.equals(encoded_deleted_already_msg)) { // This message was deleted
                                                    ds.getRef().removeValue();
                                                } else {
                                                    HashMap<String, Object> hashMap = new HashMap<>();
                                                    hashMap.put("message", encoded_deleted_already_msg); // This message was deleted
                                                    ds.getRef().updateChildren(hashMap);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                        }
                                    });//end

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                }
                            });//end
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                    return false;
                }
            });
            ((RecieverViewHolder) holder).user_img_msg_layout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure to Delete this message?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String msgTimestamp = chatmodels.get(position).getTimestamp();
                            DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("Chat");
                            Query query = dbref.child(myMobileNo).child(senderID).child("Messages")
                                    .orderByChild("timestamp").equalTo(msgTimestamp);
                            Query query1 = dbref.child(receiver).child(receiverId).child("Messages")
                                    .orderByChild("timestamp").equalTo(msgTimestamp);
                            //first entry delete code
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        String msg = ds.child("message").getValue().toString();
                                        if (msg.equals(encoded_deleted_already_msg)) { // This message was deleted
                                            ds.getRef().removeValue();
                                        } else {
                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            hashMap.put("message", encoded_deleted_already_msg); // This message was deleted
                                            ds.getRef().updateChildren(hashMap);
                                            //image delete from the firebase storage
                                            byte[] data = Base64.decode(msg, Base64.DEFAULT);
                                            String text = new String(data, StandardCharsets.UTF_8);
                                            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                                            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(text);
                                            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    //image deleted from the firebase storage
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception exception) {
                                                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                    }
                                    //second entry delete code
                                    query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds : snapshot.getChildren()) {
                                                String msg = ds.child("message").getValue().toString();
                                                if (msg.equals(encoded_deleted_already_msg)) { // This message was deleted
                                                    ds.getRef().removeValue();
                                                } else {
                                                    HashMap<String, Object> hashMap = new HashMap<>();
                                                    hashMap.put("message", encoded_deleted_already_msg); // This message was deleted
                                                    ds.getRef().updateChildren(hashMap);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                        }
                                    });//end

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                }
                            });//end
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                    return false;
                }
            });

            //delete receiver file code
            ((RecieverViewHolder) holder).receiverFile.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure to Delete this message?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String msgTimestamp = chatmodels.get(position).getTimestamp();
                            DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("Chat");
                            Query query = dbref.child(myMobileNo).child(senderID).child("Messages")
                                    .orderByChild("timestamp").equalTo(msgTimestamp);
                            Query query1 = dbref.child(receiver).child(receiverId).child("Messages")
                                    .orderByChild("timestamp").equalTo(msgTimestamp);
                            //first entry delete code
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        String msg = ds.child("message").getValue().toString();
                                        if (msg.equals(encoded_deleted_already_msg)) { // This message was deleted
                                            ds.getRef().removeValue();
                                        } else {
                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            hashMap.put("message", encoded_deleted_already_msg); // This message was deleted
                                            ds.getRef().updateChildren(hashMap);
                                            //file delete from the firebase storage
                                            byte[] data = Base64.decode(msg, Base64.DEFAULT);
                                            String text = new String(data, StandardCharsets.UTF_8);
                                            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                                            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(text);
                                            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    //file deleted from the firebase storage
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception exception) {
                                                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                    }
                                    //second entry delete code
                                    query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds : snapshot.getChildren()) {
                                                String msg = ds.child("message").getValue().toString();
                                                if (msg.equals(encoded_deleted_already_msg)) { // This message was deleted
                                                    ds.getRef().removeValue();
                                                } else {
                                                    HashMap<String, Object> hashMap = new HashMap<>();
                                                    hashMap.put("message", encoded_deleted_already_msg); // This message was deleted
                                                    ds.getRef().updateChildren(hashMap);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                        }
                                    });//end

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                }
                            });//end
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                    return false;
                }
            });
            ((RecieverViewHolder) holder).user_doc_msg_layout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure to Delete this message?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String msgTimestamp = chatmodels.get(position).getTimestamp();
                            DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("Chat");
                            Query query = dbref.child(myMobileNo).child(senderID).child("Messages")
                                    .orderByChild("timestamp").equalTo(msgTimestamp);
                            Query query1 = dbref.child(receiver).child(receiverId).child("Messages")
                                    .orderByChild("timestamp").equalTo(msgTimestamp);
                            //first entry delete code
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        String msg = ds.child("message").getValue().toString();
                                        if (msg.equals(encoded_deleted_already_msg)) { // This message was deleted
                                            ds.getRef().removeValue();
                                        } else {
                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            hashMap.put("message", encoded_deleted_already_msg); // This message was deleted
                                            ds.getRef().updateChildren(hashMap);
                                            //file delete from the firebase storage
                                            byte[] data = Base64.decode(msg, Base64.DEFAULT);
                                            String text = new String(data, StandardCharsets.UTF_8);
                                            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                                            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(text);
                                            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    //file deleted from the firebase storage
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception exception) {
                                                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                    }
                                    //second entry delete code
                                    query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds : snapshot.getChildren()) {
                                                String msg = ds.child("message").getValue().toString();
                                                if (msg.equals(encoded_deleted_already_msg)) { // This message was deleted
                                                    ds.getRef().removeValue();
                                                } else {
                                                    HashMap<String, Object> hashMap = new HashMap<>();
                                                    hashMap.put("message", encoded_deleted_already_msg); // This message was deleted
                                                    ds.getRef().updateChildren(hashMap);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                        }
                                    });//end

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                }
                            });//end
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                    return false;
                }
            });
        }
    }

    private void showPopupMenu(Context context,View v) {
        PopupMenu popupMenu=new PopupMenu(context,v);
        popupMenu.inflate(R.menu.menu_reactions);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                return onPopupMenuClick(item);

            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true);
        }
        popupMenu.show();

    }

    private boolean onPopupMenuClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.Bapu:
                Toast.makeText(context, "You select Bapu", Toast.LENGTH_SHORT).show();
            break;
            case  R.id.Thumb:
                Toast.makeText(context, "You select Thumb", Toast.LENGTH_SHORT).show();
            break;
        }
        return true;
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
