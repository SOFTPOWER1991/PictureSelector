package com.common.pictureselector.activities;

import android.widget.Button;

import com.common.pictureselector.R;
import com.common.pictureselector.view.PicturesPreviewRecyclerView;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * File Description  : 图片选择器首页
 *
 * @author : zhanggeng
 * @version : v1.0
 *          **************修订历史*************
 * @email : zhanggengdyx@gmail.com
 * @date : 2016/12/17 10:33
 */
public class MainActivity extends BaseActivity {

    @BindView(R.id.btn_insert_picture)
    Button btnInsertPic;
    @BindView(R.id.recycler_images)
    PicturesPreviewRecyclerView tweetPicturesPreviewer;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @OnClick(R.id.btn_insert_picture)
    public void onClick() {
        openPictureSelector();
    }

    private void openPictureSelector() {
        tweetPicturesPreviewer.onLoadAllPictures();
    }
}
