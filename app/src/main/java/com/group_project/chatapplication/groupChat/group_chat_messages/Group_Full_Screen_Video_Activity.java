package com.group_project.chatapplication.groupChat.group_chat_messages;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.widget.MediaController;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.firebase.auth.FirebaseAuth;
import com.group_project.chatapplication.R;

import org.junit.runner.manipulation.Ordering;

import java.io.File;

public class Group_Full_Screen_Video_Activity extends AppCompatActivity {

    ImageView back_page, download_receiver_video;
    VideoView full_video;
    String video, senderid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_full_screen_video);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(this.getResources().getColor(R.color.black));

//        ProgressDialog progressDialog = new ProgressDialog(this);
//        progressDialog.setTitle("Please wait");
//        progressDialog.setMessage("Load video...");
//        progressDialog.setCancelable(false);
//        progressDialog.show();

        back_page = findViewById(R.id.back_page);
        download_receiver_video = findViewById(R.id.download_receiver_video);
        full_video = findViewById(R.id.full_video);

        Intent intent = getIntent();
        video = intent.getStringExtra("video");
        senderid = intent.getStringExtra("sender");

        full_video.setVideoURI(Uri.parse(video));
        full_video.start();


        MediaController mediaController = new MediaController(this);
        full_video.setMediaController(mediaController);
        mediaController.setAnchorView(full_video);

        String myuid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (senderid.equals(myuid)) {

            download_receiver_video.setVisibility(View.GONE);
        } else {

            download_receiver_video.setVisibility(View.VISIBLE);
        }

        back_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        download_receiver_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String Directory=Environment.DIRECTORY_DOWNLOADS;

                String filename = String.format("Video_" + System.currentTimeMillis() + ".mp4", System.currentTimeMillis());
                DownloadManager downloadManager=(DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri=Uri.parse(video);
                DownloadManager.Request request=new DownloadManager.Request(uri);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir( ""+Directory,""+filename);
                downloadManager.enqueue(request);
                Toast.makeText(Group_Full_Screen_Video_Activity.this, "Download successfully", Toast.LENGTH_SHORT).show();
            }
        });

    }
}