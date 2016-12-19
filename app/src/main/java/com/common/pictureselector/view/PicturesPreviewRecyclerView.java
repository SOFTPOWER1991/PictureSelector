package com.common.pictureselector.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.common.pictureselector.activities.PictureSelectorActivity;
import com.common.pictureselector.adapter.SelectImageAdapter;
import com.common.pictureselector.interf.PicturesPreviewerItemTouchCallback;

/**
 * File Description  : 自定义recyclerview
 *
 * @author : zhanggeng
 * @email : zhanggengdyx@gmail.com
 * @date : 2016/12/19 21:55
 * @version     : v1.0
 * **************修订历史*************
 */

public class PicturesPreviewRecyclerView extends RecyclerView implements SelectImageAdapter.Callback, PictureSelectorActivity.Callback {
    private SelectImageAdapter mImageAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private RequestManager mCurImageLoader;

    public PicturesPreviewRecyclerView(Context context) {
        super(context);
        init();
    }

    public PicturesPreviewRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PicturesPreviewRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mImageAdapter = new SelectImageAdapter(this);

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
        this.setLayoutManager(layoutManager);
        this.setAdapter(mImageAdapter);
        this.setOverScrollMode(View.OVER_SCROLL_NEVER);

        ItemTouchHelper.Callback callback = new PicturesPreviewerItemTouchCallback(mImageAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(this);
    }

    public void set(String[] paths) {
        mImageAdapter.clear();
        for (String path : paths) {
            mImageAdapter.add(path);
        }
        mImageAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoadMoreClick() {
        PictureSelectorActivity.showImage(getContext(), 9, true, mImageAdapter.getPaths(), this);
    }

    @Override
    public RequestManager getImgLoader() {
        if (mCurImageLoader == null) {
            mCurImageLoader = Glide.with(getContext());
        }
        return mCurImageLoader;
    }

    @Override
    public void onStartDrag(ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    public String[] getPaths() {
        return mImageAdapter.getPaths();
    }

    @Override
    public void doSelectDone(String[] images) {
        set(images);
    }
}
