package com.example.yhyhealthy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import org.jetbrains.annotations.NotNull;

public class VideoShowActivity extends AppCompatActivity {

    private YouTubePlayerView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_show);

        playerView = findViewById(R.id.youtubePlayerView);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            String fileName = bundle.getString("FILE");
            loadVideo(fileName);
        }
    }


    private void loadVideo(String videoId) {
        playerView.enterFullScreen();      //全螢幕
        playerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NotNull YouTubePlayer youTubePlayer) {
                youTubePlayer.loadVideo(videoId, 0);

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playerView.release();
    }
}