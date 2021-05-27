package com.example.yhyhealthy.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.yhyhealthy.ArticleActivity;
import com.example.yhyhealthy.R;
import com.example.yhyhealthy.VideoActivity;

/***
 * 衛教頁面
 * 文章,影片,討論區,線上諮詢(暫不支援)
 */


public class EducationFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "EducationFragment";

    private View view;

    private Button btnArticle, btnVideo, btnForum, btnOnline;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (view != null) return view;
        view = inflater.inflate(R.layout.fragment_education, container, false);

        btnArticle = view.findViewById(R.id.btnArticle);
        btnVideo = view.findViewById(R.id.btnVideo);
        btnForum = view.findViewById(R.id.btnDiscuss);
        btnForum.setVisibility(View.GONE);
        btnOnline = view.findViewById(R.id.onlineCall);
        btnOnline.setVisibility(View.GONE);

        btnArticle.setOnClickListener(this);
        btnVideo.setOnClickListener(this);
        btnForum.setOnClickListener(this);
        btnOnline.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        Class<?> target = null;

        switch (v.getId()){
            case R.id.btnArticle:
                Log.d(TAG, "onClick: !!!!");
                target = ArticleActivity.class;
                break;
            case R.id.btnVideo:
                target = VideoActivity.class;
                break;
            case R.id.btnDiscuss:
//                target = FourmActivity.class;
                break;
            case R.id.onlineCall:
//                target = OnLineCallActivity.class;
                break;
        }
        if (target != null) startActivity(new Intent(getContext(), target));
    }
}