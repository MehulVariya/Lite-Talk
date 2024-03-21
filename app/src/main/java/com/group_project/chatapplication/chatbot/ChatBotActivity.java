package com.group_project.chatapplication.chatbot;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.group_project.chatapplication.R;
import com.group_project.chatapplication.network.ApiInterface;
import com.group_project.chatapplication.network.ChatBotResponse;
import com.group_project.chatapplication.singleChat.single_chat_messages.Chat_Adapter;
import com.group_project.chatapplication.singleChat.single_chat_messages.Single_Chat_Messages_Activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ChatBotActivity extends AppCompatActivity {

    ImageButton sendMessageButton;
    EditText userMessageInput;
    ImageView back_press, profile_img, img_chat_wallpaper;
    TextView user_name;
    String  myMobileNo;
    RecyclerView chattingRecycleView;
    RelativeLayout msgRelativeLayout;
    RelativeLayout invite_user_layout;
    ImageView img_invite;
    TextView btn_invite_user, user_status;
    ChatBotAdapter chatAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);
        user_status = findViewById(R.id.userstatusTv);
        back_press = findViewById(R.id.back_to_screen);
        profile_img = findViewById(R.id.profile_img);
        user_name = findViewById(R.id.user_name);
        msgRelativeLayout = findViewById(R.id.msgRelativeLayout);
        sendMessageButton = findViewById(R.id.send_message_button);
        userMessageInput = findViewById(R.id.input_message);
        img_chat_wallpaper = findViewById(R.id.img_chat_wallpaper);
        invite_user_layout = findViewById(R.id.invite_user_layout);
        img_invite = findViewById(R.id.img_invite);
        btn_invite_user = findViewById(R.id.btn_invite_user);

        invite_user_layout.setVisibility(View.GONE);

        myMobileNo = getIntent().getExtras().get("myMobileNo").toString().trim();

        chattingRecycleView = findViewById(R.id.chat_recyclearview);

        back_press.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ArrayList<Map<String,String>> sampleData = new ArrayList<>() ;
        Map <String,String> person = new HashMap<String,String>();
        person.put("Type","Person");
        person.put("Message","Hii");
        Map <String,String> chatBot = new HashMap<String,String>();
        chatBot.put("Type","ChatBot");
        chatBot.put("Message","Welcome to Lite Talk Bot.\n\"\uD83D\uDE4FJay shree ram \uD83D\uDE4F\"\n" +
                "We can help you directly. Please send a message below.");
        sampleData.add(chatBot);
        chatAd = new ChatBotAdapter(ChatBotActivity.this, sampleData);
        chattingRecycleView.setAdapter(chatAd);

        // Wallpaper display code...
        DatabaseReference dbwallpaper = FirebaseDatabase.getInstance().getReference().child("Chat Wallpaper").child("+91" + myMobileNo).child("wallpaper_image");
        dbwallpaper.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists())) {
                    String retrieveWallpaperImage = snapshot.getValue(String.class);
                    try {
                        Glide.with(getApplicationContext()).load(retrieveWallpaperImage).into(img_chat_wallpaper);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Wallpaper not set!", Toast.LENGTH_SHORT).show();
            }
        });
        do_chat_messages();

        userMessageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0) {

                } else {

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    public void do_chat_messages() {
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map <String,String> person = new HashMap<String,String>();
                person.put("Type","Person");
                String message = userMessageInput.getText().toString();
                person.put("Message",message);
                chatAd.addChatMode(person);
                userMessageInput.setText("");

                int newMessagePosition = chatAd.chatmodels.size() - 1;

// Smoothly scroll to the new message position
                chattingRecycleView.smoothScrollToPosition(newMessagePosition);

                Retrofit retrofit = new Retrofit.Builder().baseUrl("http://api.brainshop.ai/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                ApiInterface apiInterface = retrofit.create(ApiInterface.class);

                Call<ChatBotResponse> chatBotData = apiInterface.getChatBotData(message);
                Log.d("TAG", "onResponse: ");

                chatBotData.enqueue(new Callback<ChatBotResponse>() {
                    @Override
                    public void onResponse(Call<ChatBotResponse> call, Response<ChatBotResponse> response) {
                        Log.d("TAG", "onResponse: 222 ");
                        if (response.isSuccessful()) {
                            // Handle successful response with data
                            ChatBotResponse responseBody = response.body();
                            Map <String,String> bot = new HashMap<String,String>();

                            Log.d("TAG", "onResponse: 3333 "+responseBody);
                            bot.put("Type","ChatBot");
                            String message = responseBody.getCnt();
                            bot.put("Message",message);
                            chatAd.addChatMode(bot);
                            int newMessagePosition = chatAd.chatmodels.size() - 1;

// Smoothly scroll to the new message position
                            chattingRecycleView.smoothScrollToPosition(newMessagePosition);
                            // ... process the response body
                        } else {
                            // Handle unsuccessful response (errors)
                        }
                    }

                    @Override
                    public void onFailure(Call<ChatBotResponse> call, Throwable t) {
                        // Handle network errors
                        Log.d("TAG", "onResponse: 444 fail "+t.getMessage());
                    }
                });

            }
        });
    }


}