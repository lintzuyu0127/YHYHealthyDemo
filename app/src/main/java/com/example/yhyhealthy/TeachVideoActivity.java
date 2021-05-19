package com.example.yhyhealthy;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * 教學影片
 * create 2021/05/18
 * **/
public class TeachVideoActivity extends AppCompatActivity {

    //暫時
    private static final String VIDEO_SAMPLE =
            "http://192.168.1.108/health_education/video/video.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teach_video);

        VideoView videoView = (VideoView) findViewById(R.id.videoView);

        //creating MediaController
        MediaController mediaController = new MediaController(this);

        //set anchor view for video view
        mediaController.setAnchorView(videoView);

        //set the media controller for video view
        videoView.setMediaController(mediaController);

        //set the uri for video view
        Uri uri = Uri.parse(VIDEO_SAMPLE);
        videoView.setVideoURI(uri);

        //start a video
        videoView.start();
    }
}