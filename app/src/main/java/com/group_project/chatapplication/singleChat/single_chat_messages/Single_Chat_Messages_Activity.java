package com.group_project.chatapplication.singleChat.single_chat_messages;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.group_project.chatapplication.R;
import com.group_project.chatapplication.registration.User_Model;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class Single_Chat_Messages_Activity extends AppCompatActivity {


    Toolbar mToolbar;
    ImageButton sendMessageButton;
    EditText userMessageInput;
    ImageView back_press, profile_img, attachbtn, img_chat_wallpaper;
    TextView user_name;
    String currentContactName, myMobileNo, receiverMobileNo, getName, senderRoom, receiverRoom;
    RecyclerView chattingRecycleView;
    RelativeLayout msgRelativeLayout;
    RelativeLayout invite_user_layout;
    ImageView img_invite;
    TextView btn_invite_user,user_status;
    Chat_Adapter chatAd;
    User_Model user_model;
    Receiver_info_Model msg_model = new Receiver_info_Model();
    ArrayList<Chatmodel> chatModel = new ArrayList<>();
    FirebaseAuth auth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    int feeling=-1;
    Uri image_uri = null;
    Uri uri;
    String picturePath;
    int IMAGE_PICK_CAMERA_CODE = 300;
    int IMAGE_PICK_GALLERY_CODE = 400;
    int DOCUMENT_PICK_CODE = 500;
    long length;
    int file_size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_chat_messages);
        user_status=findViewById(R.id.userstatusTv);
        back_press = findViewById(R.id.back_to_screen);
        profile_img = findViewById(R.id.profile_img);
        user_name = findViewById(R.id.user_name);
        attachbtn = findViewById(R.id.send_file);
        msgRelativeLayout = findViewById(R.id.msgRelativeLayout);
        sendMessageButton = findViewById(R.id.send_message_button);
        userMessageInput = findViewById(R.id.input_message);
        img_chat_wallpaper = findViewById(R.id.img_chat_wallpaper);
        invite_user_layout = findViewById(R.id.invite_user_layout);
        img_invite = findViewById(R.id.img_invite);
        btn_invite_user = findViewById(R.id.btn_invite_user);

        invite_user_layout.setVisibility(View.GONE);

        currentContactName = getIntent().getExtras().get("pass_receiver_name").toString().trim();
        receiverMobileNo = getIntent().getExtras().get("pass_receiver_number").toString().replace(" ", "").replace("-", "").replace("+91", "");

        mToolbar = findViewById(R.id.chat_bar_layout);

        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        assert user != null;
        myMobileNo = Objects.requireNonNull(user.getPhoneNumber()).replace("+91", "");
        getName = user.getDisplayName();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Chat");

        chattingRecycleView = findViewById(R.id.chat_recyclearview);

        senderRoom = myMobileNo + receiverMobileNo;
        receiverRoom = receiverMobileNo + myMobileNo;

        back_press.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        attachbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        // Wallpaper display code...
        DatabaseReference dbwallpaper = FirebaseDatabase.getInstance().getReference().child("Chat Wallpaper").child("+91" + myMobileNo).child("wallpaper_image");
        dbwallpaper.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists())) {
                    String retrieveWallpaperImage = snapshot.getValue(String.class);
                    try {
                        Glide.with(getApplicationContext()).load(retrieveWallpaperImage).into(img_chat_wallpaper);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Wallpaper not set!", Toast.LENGTH_SHORT).show();
            }
        });

        check_number_exist_or_not();

        do_chat_messages();
        display_chat_messages();

        userMessageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length()==0){
                    checkTypingStatus("noOne");
                }else {
                    checkTypingStatus(myMobileNo);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    //start checking online & typing
    public void checkOnlineStatus(String status){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users Details").child("+91" + myMobileNo);

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("onlineStatus",status);
        reference.updateChildren(hashMap);
    }

//    public void checkReceiverStatus(String status){
//        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users Details").child("+91" + receiverMobileNo);
//
//        HashMap<String,Object> hashMap=new HashMap<>();
//        hashMap.put("onlineStatus",status);
//        reference.updateChildren(hashMap);
//    }

    public void checkTypingStatus(String typing){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users Details").child("+91" + myMobileNo);

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("typing",typing);
        reference.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        checkOnlineStatus("online");
        //checkReceiverStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();

        String timestamp=String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestamp);
        checkTypingStatus("noOne");
       // checkReceiverStatus(timestamp);
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
       // checkReceiverStatus("online");
        super.onResume();
    }



    //send message
    public void do_chat_messages() {
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String getTxtMessage = userMessageInput.getText().toString().trim();
                byte[] data = getTxtMessage.getBytes(StandardCharsets.UTF_8);
                String encode_txt_msg = Base64.encodeToString(data, Base64.DEFAULT);
                final Chatmodel chatmodel = new Chatmodel(myMobileNo, encode_txt_msg, "text",feeling);

                if (TextUtils.isEmpty(getTxtMessage)) {
                    Toast.makeText(getApplicationContext(), "Can't send empty message", Toast.LENGTH_SHORT).show();
                } else {
                    String currentTime = String.valueOf(new Date().getTime());
                    chatmodel.setTimestamp(currentTime);
                    databaseReference.child(myMobileNo)
                            .child(senderRoom)
                            .child("Messages")
                            .child(currentTime)
                            .setValue(chatmodel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    databaseReference.child(receiverMobileNo)
                                            .child(receiverRoom)
                                            .child("Messages")
                                            .child(currentTime)
                                            .setValue(chatmodel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                }
                                            });
                                }
                            });
                    userMessageInput.setText("");
                }
            }
        });
    }

    // display user messages
    public void display_chat_messages() {
        databaseReference.child(myMobileNo)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        chatModel.clear();
                        for (DataSnapshot snapshot1 : snapshot.child(senderRoom).child("Messages").getChildren()) {
                            Chatmodel chatmodel = snapshot1.getValue(Chatmodel.class);
                            chatModel.add(chatmodel);
                        }
                        chatAd = new Chat_Adapter(Single_Chat_Messages_Activity.this, chatModel, senderRoom, receiverRoom, receiverMobileNo);
                        chattingRecycleView.setAdapter(chatAd);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    // check user can use this app or not
    public void check_number_exist_or_not() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users Details").child("+91" + receiverMobileNo);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // fetch values from User Details table

                    // Upload receiver details in our room id
                    firebaseDatabase.getReference().child("Users Details").child("+91" + receiverMobileNo).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            user_model = snapshot.getValue(User_Model.class);
                            msg_model.setReceiverNo(user_model.getPhone());
                            msg_model.setReceiverName(user_model.getName());
                            msg_model.setReceiverProfileImg(user_model.getProfile_image());
                            msg_model.setReceiverInfo(user_model.getAbout());
                            msg_model.setRoomId(senderRoom);
                            msg_model.setOnlineStatus(user_model.getOnlineStatus());
                            msg_model.setTyping(user_model.getTyping());

                            HashMap<String, Object> objectsHashMap = new HashMap<>();
                            objectsHashMap.put("receiver_no", msg_model.getReceiverNo());
                            objectsHashMap.put("receiver_name", msg_model.getReceiverName());
                            objectsHashMap.put("receiver_profileImage", msg_model.getReceiverProfileImg());
                            objectsHashMap.put("receiver_info", msg_model.getReceiverInfo());
                            objectsHashMap.put("room_id", msg_model.getRoomId());
                            objectsHashMap.put("onlineStatus",msg_model.getOnlineStatus());
                            objectsHashMap.put("typing",msg_model.getTyping());

                            firebaseDatabase.getReference().child("Chat").child(myMobileNo).child(senderRoom).updateChildren(objectsHashMap);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    // Upload sender details in receiver room id
                    firebaseDatabase.getReference().child("Users Details").child("+91" + myMobileNo).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            user_model = snapshot.getValue(User_Model.class);
                            msg_model.setReceiverNo(user_model.getPhone());
                            msg_model.setReceiverName(user_model.getName());
                            msg_model.setReceiverProfileImg(user_model.getProfile_image());
                            msg_model.setReceiverInfo(user_model.getAbout());
                            msg_model.setRoomId(receiverRoom);
                            msg_model.setOnlineStatus(user_model.getOnlineStatus());
                            msg_model.setTyping(user_model.getTyping());

                            HashMap<String, Object> objectsHashMap = new HashMap<>();
                            objectsHashMap.put("receiver_no", msg_model.getReceiverNo());
                            objectsHashMap.put("receiver_name", msg_model.getReceiverName());
                            objectsHashMap.put("receiver_profileImage", msg_model.getReceiverProfileImg());
                            objectsHashMap.put("receiver_info", msg_model.getReceiverInfo());
                            objectsHashMap.put("room_id", msg_model.getRoomId());
                            objectsHashMap.put("onlineStatus",msg_model.getOnlineStatus());
                            objectsHashMap.put("typing",msg_model.getTyping());
                            firebaseDatabase.getReference().child("Chat").child(receiverMobileNo).child(receiverRoom).updateChildren(objectsHashMap);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    //inflate menu for clear chat
                    mToolbar.inflateMenu(R.menu.delete_chat_menu);
                    mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getItemId() == R.id.clear_chat_for_me) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(Single_Chat_Messages_Activity.this);
                                builder.setTitle("Clear chat?");
                                builder.setMessage("Are you sure to clear your chat?");
                                builder.setPositiveButton("Clear", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        FirebaseDatabase.getInstance().getReference().child("Chat").child(myMobileNo).child(senderRoom).child("Messages").setValue(null);
                                        Toast.makeText(Single_Chat_Messages_Activity.this, "All chat cleared for you", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                builder.create().show();
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(Single_Chat_Messages_Activity.this);
                                builder.setTitle("Clear chat?");
                                builder.setMessage("Are you sure to clear chat for everyone?");
                                builder.setPositiveButton("Clear", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        FirebaseDatabase.getInstance().getReference().child("Chat").child(myMobileNo).child(senderRoom).child("Messages").setValue(null);
                                        FirebaseDatabase.getInstance().getReference().child("Chat").child(receiverMobileNo).child(receiverRoom).child("Messages").setValue(null);
                                        Toast.makeText(Single_Chat_Messages_Activity.this, "All chat cleared for everyone", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                builder.create().show();
                            }
                            return false;
                        }
                    });

                    // navigate to info activity
                    databaseReference.child(myMobileNo).child(senderRoom)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot ds) {
                                    String title = "" + ds.child("receiver_name").getValue();
                                    String profileImage = "" + ds.child("receiver_profileImage").getValue();
                                    String mobile = "" + ds.child("receiver_no").getValue();
                                    String about = "" + ds.child("receiver_info").getValue();
                                    String online=""+ds.child("onlineStatus").getValue();
                                    String typing=""+ds.child("typing").getValue();
                                    if (typing.equals(receiverMobileNo)){
                                        user_status.setText("typing...");
                                    }

                                    else   if (online.equals("online")){
                                        user_status.setText(online);
                                    }//the end
                                    else {
                                        user_status.setText("");
                                    }

                                    user_name.setText(title);
                                    try {
                                        Glide.with(profile_img).load(profileImage).into(profile_img);
                                    } catch (Exception e) {
                                        profile_img.setImageResource(R.drawable.img_default_person);
                                    }

                                    user_name.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(getApplicationContext(), Single_Chat_Info_Activity.class);
                                            intent.putExtra("title", title);
                                            intent.putExtra("mobile", mobile);
                                            intent.putExtra("profileImage", profileImage);
                                            intent.putExtra("about", about);
                                            intent.putExtra("contactName", currentContactName);
                                            intent.putExtra("contactNumber", receiverMobileNo);
                                            startActivity(intent);
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                } else {
                    try {
                        Glide.with(img_chat_wallpaper).load(R.drawable.img_white_bg).into(img_chat_wallpaper);
                        Glide.with(profile_img).load(R.drawable.img_contact_user).into(profile_img);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    user_name.setText(currentContactName);
                    msgRelativeLayout.setVisibility(View.GONE);
                    sendMessageButton.setVisibility(View.GONE);
                    img_chat_wallpaper.setVisibility(View.GONE);
                    invite_user_layout.setVisibility(View.VISIBLE);
                    btn_invite_user.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            String Body = "Let's chat on Lite Talk! It's a fast, simple, and secure app we can use to message each other for free. Get it at \n\n ";
                            intent.putExtra(Intent.EXTRA_TEXT, Body);
                            startActivity(Intent.createChooser(intent, "Share App"));
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void showDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet_layout);

        ImageView camera = dialog.findViewById(R.id.camera);
        ImageView gallery = dialog.findViewById(R.id.gallery);
        ImageView pdf = dialog.findViewById(R.id.pdf);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                pickFromCamera();
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                pickfromGallery();
            }
        });

        pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                pickfromDocument();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialoAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void pickfromDocument() {
        Intent intent1 = new Intent();
        intent1.setType("application/*");
        intent1.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent1, "Select Document"), DOCUMENT_PICK_CODE);
    }

    public void pickfromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    public void pickFromCamera() {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Image Icon Title");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Image Icon Description");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private void sendImagemessage() {
        //progress
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Sending Image..");
        progressDialog.setCancelable(false);
        progressDialog.show();

        //file path
        String filepath = "Single User Messages/" + myMobileNo + "/" + "Images/" + System.currentTimeMillis() + ".jpg";
        //upload
        StorageReference storageReference;
        storageReference = FirebaseStorage.getInstance().getReference(filepath);
        storageReference.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> p_uriTasl = taskSnapshot.getStorage().getDownloadUrl();
                while (!p_uriTasl.isSuccessful()) ;
                Uri p_downloadUri = p_uriTasl.getResult();
                if (p_uriTasl.isSuccessful()) {
                    String timestamp = "" + System.currentTimeMillis();
                    String img_uri_normal = p_downloadUri.toString();
                    byte[] data = img_uri_normal.getBytes(StandardCharsets.UTF_8);
                    String encode_img_msg = Base64.encodeToString(data, Base64.DEFAULT);

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", "" + myMobileNo);
                    hashMap.put("message", "" + encode_img_msg);
                    hashMap.put("timestamp", "" + timestamp);
                    hashMap.put("type", "" + "image");//text,image,file

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chat");
                    reference.child(myMobileNo)
                            .child(senderRoom)
                            .child("Messages")
                            .child(timestamp)
                            .setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    reference.child(receiverMobileNo)
                                            .child(receiverRoom)
                                            .child("Messages")
                                            .child(timestamp)
                                            .setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {

                                                }
                                            });
                                    userMessageInput.setText("");
                                    progressDialog.dismiss();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                image_uri = data.getData();

                String[] filepathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(image_uri, filepathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filepathColumn[0]);
                picturePath = cursor.getString(columnIndex);
                cursor.close();

                File img = new File(picturePath);
                length = img.length();
                file_size = Integer.parseInt(String.valueOf(length / 1024));
                if (file_size < 5000) {
                    sendImagemessage();
                } else {
                    Toast.makeText(this, "greater", Toast.LENGTH_SHORT).show();
                }

            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {

                sendImagemessage();

            } else if (requestCode == DOCUMENT_PICK_CODE) {
                uri = data.getData();
                sendDocument();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendDocument() {
        //progress
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        // progressDialog.setMessage("Sending Image..");
        progressDialog.setCancelable(false);
        progressDialog.show();

        //file path
        String filepath = "Single User Messages/" + myMobileNo + "/" + "Documents/" + System.currentTimeMillis();
        //upload
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filepath);
        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> p_uriTasl = taskSnapshot.getStorage().getDownloadUrl();
                while (!p_uriTasl.isSuccessful()) ;
                Uri p_downloadUri = p_uriTasl.getResult();
                if (p_uriTasl.isSuccessful()) {
                    String timestamp = "" + System.currentTimeMillis();
                    String doc_uri_normal = p_downloadUri.toString();
                    byte[] data = doc_uri_normal.getBytes(StandardCharsets.UTF_8);
                    String encode_doc_msg = Base64.encodeToString(data, Base64.DEFAULT);

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender", "" + myMobileNo);
                    hashMap.put("message", "" + encode_doc_msg);
                    hashMap.put("timestamp", "" + timestamp);
                    hashMap.put("type", "" + "file");//text,image,file

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chat");
                    reference.child(myMobileNo)
                            .child(senderRoom)
                            .child("Messages")
                            .child(timestamp)
                            .setValue(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    //send
                                    //clear
                                    reference.child(receiverMobileNo)
                                            .child(receiverRoom)
                                            .child("Messages")
                                            .child(timestamp)
                                            .setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {

                                                }
                                            });

                                    userMessageInput.setText("");
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "Document sent", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "Failed to send", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Failed to send", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double p = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                progressDialog.setMessage((int) p + "% Uploading...");
            }
        });
    }

}