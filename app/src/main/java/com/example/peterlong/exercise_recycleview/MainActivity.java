package com.example.peterlong.exercise_recycleview;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.peterlong.exercise_recycleview.Video.VideoListActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnVideoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_video_list).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_video_list:{
                startActivity(new Intent(this, VideoListActivity.class));
            }
        }
    }
}