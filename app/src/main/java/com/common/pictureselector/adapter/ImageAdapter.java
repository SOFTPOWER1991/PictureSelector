package com.common.pictureselector.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.common.pictureselector.R;
import com.common.pictureselector.bean.Image;
import com.common.pictureselector.interf.ImageLoaderListener;


/**
 * 图片列表界面适配器
 */
public class ImageAdapter extends BaseRecyclerAdapter<Image> {

    private int TYPE_CAMERA = 0;
    private int TYPE_ALBUM = 1;
    private ImageLoaderListener loader;

    public ImageAdapter(Context context, ImageLoaderListener loader) {
        super(context, NEITHER);
        this.loader = loader;
    }


    @Override
    public int getItemViewType(int position) {
        Image image = getItem(position);
        if (image.getId() == 0)
            return TYPE_CAMERA;
        return TYPE_ALBUM;
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        if (holder instanceof ImageViewHolder) {
            ImageViewHolder h = (ImageViewHolder) holder;
            //清除掉所有的图片加载请求
            Glide.clear(h.mImageView);
        }
    }

    @Override
    protected RecyclerView.ViewHolder onCreateDefaultViewHolder(ViewGroup parent, int type) {
        if (type == TYPE_CAMERA)
            return new CamViewHolder(mInflater.inflate(R.layout.item_list_cam, parent, false));
        return new ImageViewHolder(mInflater.inflate(R.layout.item_list_image, parent, false));
    }

    @Override
    protected void onBindDefaultViewHolder(RecyclerView.ViewHolder holder, Image item, int position) {
        if (item.getId() != 0) {
            ImageViewHolder h = (ImageViewHolder) holder;
            h.mCheckView.setSelected(item.isSelect());
            h.mMaskView.setVisibility(item.isSelect() ? View.VISIBLE : View.GONE);

            // Show gif mask
            h.mGifMask.setVisibility(item.getPath().toLowerCase().endsWith("gif") ?
                    View.VISIBLE : View.GONE);

            loader.displayImage(h.mImageView, item.getPath());
        }
    }

    private static class CamViewHolder extends RecyclerView.ViewHolder {
        CamViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView mImageView;
        ImageView mCheckView;
        ImageView mGifMask;
        View mMaskView;

        ImageViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.iv_image);
            mCheckView = (ImageView) itemView.findViewById(R.id.cb_selected);
            mMaskView = itemView.findViewById(R.id.lay_mask);
            mGifMask = (ImageView) itemView.findViewById(R.id.iv_is_gif);
        }
    }
}
