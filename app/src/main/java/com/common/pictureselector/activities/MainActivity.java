package com.common.pictureselector.activities;

import android.view.View;
import android.widget.Button;

import com.common.pictureselector.R;
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
public class MainActivity extends BaseActivity implements View.OnClickListener {

    private Button btnInsertPic;

    private PicturesPreviewRecyclerView tweetPicturesPreviewer;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        super.initView();
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
        tweetPicturesPreviewer.onLoadAllPictures();
    }

}
