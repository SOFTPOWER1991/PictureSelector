package com.common.pictureselector.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.common.pictureselector.R;
import com.common.pictureselector.interf.PublishContract;
import com.common.pictureselector.view.PicturesPreviewRecyclerView;

/**
 * File Description  : 图片选择器首页
 *
 * @author : zhanggeng
 * @version : v1.0
 *          **************修订历史*************
 * @email : zhanggengdyx@gmail.com
 * @date : 2016/12/17 10:33
 */
public class MainActivity extends Activity implements View.OnClickListener, PublishContract.View {

    private Button btnInsertPic;

    private PicturesPreviewRecyclerView tweetPicturesPreviewer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        btnInsertPic = (Button) findViewById(R.id.btn_insert_picture);
        btnInsertPic.setOnClickListener(this);

        tweetPicturesPreviewer = (PicturesPreviewRecyclerView) findViewById(R.id.recycler_images);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_insert_picture:
                openPictureSelector();
                break;
        }
    }

    private void openPictureSelector() {
        Log.e(MainActivity.class.getSimpleName(), "picture selector open!");
        tweetPicturesPreviewer.onLoadMoreClick();
    }


    @Override
    public String[] getImages() {
        return tweetPicturesPreviewer.getPaths();
    }

    @Override
    public void setImages(String[] paths) {
        tweetPicturesPreviewer.set(paths);
    }


}
