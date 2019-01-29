package com.example.peterlong.exercise_recycleview.Video;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.util.List;

import com.example.peterlong.exercise_recycleview.R;
import com.example.peterlong.exercise_recycleview.beans.Feed;
import com.example.peterlong.exercise_recycleview.beans.FeedResponse;
import com.example.peterlong.exercise_recycleview.util.IMiniDouyinService;
import com.example.peterlong.exercise_recycleview.util.RetrofitManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class VideoListActivity extends AppCompatActivity implements VideoListAdapter.ListItemOnClickListener {

    private RecyclerView recyclerView;
    private VideoListAdapter videoListAdapter;

    private int firstVisibleItem;
    private int lastVisibleItem;
    //计算可见区域的videoItem数目
    private int visibleCount;

    private static final String TAG = VideoListActivity.class.getName();
    private static final String URL_KEY = "video_url";
    private static final String TITLE_KEY = "video_title";
    private static final String URL_BUNDLE = "url_bundle";

    private static final String HOST = "http://10.108.10.39:8080/";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        recyclerView = findViewById(R.id.rv_video_list);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        videoListAdapter = new VideoListAdapter(VideoListActivity.this);

        recyclerView.setAdapter(videoListAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {


            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.d(TAG, "onScrollStateChanged");
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        //滚动停止
                        //判断视频位置，自动播放
                        autoPlayVideo(recyclerView);
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        //手指拖动
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        //惯性滑动
                        break;

                }

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Log.d(TAG, "onScrolled");
                firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                visibleCount = lastVisibleItem - firstVisibleItem;

                //大于0说明在播放
                if (GSYVideoManager.instance().getPlayPosition() >= 0) {
                    //当前播放位置
                    int position = GSYVideoManager.instance().getPlayPosition();
                    //对应播放列表的TAG
                    if (GSYVideoManager.instance().getPlayTag().equals(VideoListAdapter.TAG) && (position < firstVisibleItem || position > lastVisibleItem)) {
                        GSYVideoManager.onPause();
                    }
                }
            }
        });


    }

    private void autoPlayVideo(RecyclerView view) {
        RecyclerView.LayoutManager layoutManager = view.getLayoutManager();
        for (int i = 0; i < visibleCount; i++) {
            StandardGSYVideoPlayer gsyVideoPlayer = layoutManager
                    .getChildAt(i).findViewById(R.id.video_item_player);
            Rect rect = new Rect();
            if (gsyVideoPlayer.getLocalVisibleRect(rect)) {
                if (rect.height() == gsyVideoPlayer.getHeight()) {
                    Log.d(TAG, "播放视频:" + gsyVideoPlayer.getCurrentState());
                    gsyVideoPlayer.getStartButton().performClick();
                    return;
                }
            }
        }
        GSYVideoManager.releaseAllVideos();
    }


    public void feed(View view) {
        //加载feed流
        Retrofit retrofit = RetrofitManager.get(HOST);

        Call<FeedResponse> call = retrofit.create(IMiniDouyinService.class).feed();

        call.enqueue(new Callback<FeedResponse>() {
            @Override
            public void onResponse(Call<FeedResponse> call, Response<FeedResponse> response) {
                if (response.isSuccessful()) {
                    FeedResponse feedResponse = response.body();
                    if (feedResponse != null && feedResponse.isSuccess()) {
                        List<Feed> feeds = feedResponse.getFeeds();
                        videoListAdapter.refresh(feeds);
                    }
                }
            }

            @Override
            public void onFailure(Call<FeedResponse> call, Throwable t) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (GSYVideoManager.backFromWindowFull(this)) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GSYVideoManager.releaseAllVideos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        GSYVideoManager.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        GSYVideoManager.onPause();
    }

    @Override
    public void onListItemClick(Feed feed) {
        Log.d(TAG, "onListItemClick");
        Intent intent = new Intent(VideoListActivity.this, DetailPlayerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(URL_KEY, feed.getVideoUrl());
        bundle.putString(TITLE_KEY, feed.getUsername());
        intent.putExtra(URL_BUNDLE, bundle);
        startActivity(intent);
    }
}
