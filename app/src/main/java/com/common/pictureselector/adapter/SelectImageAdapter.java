package com.common.pictureselector.adapter;

import android.content.Context;
import android.os.Vibrator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.common.pictureselector.R;
import com.common.pictureselector.interf.PicturesPreviewerItemTouchCallback;

import java.util.ArrayList;
import java.util.List;

public class SelectImageAdapter extends RecyclerView.Adapter<SelectImageAdapter.SelectImageHolder> implements PicturesPreviewerItemTouchCallback.ItemTouchHelperAdapter {

    private final int MAX_SIZE = 9;
    private final int TYPE_NONE = 0;
    private final int TYPE_ADD = 1;

    private final List<Model> mModels = new ArrayList<>();

    public interface Callback {

        /**
         * 加载设备上的所有图片
         */
        void onLoadAllPictures();

        /**
         * 获取图片加载器
         *
         * @return
         */
        RequestManager getImgLoader();

        /**
         * Called when a view is requesting a start of a drag.
         *
         * @param viewHolder The holder of the view to drag.
         */
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    private Callback mCallback;

    public SelectImageAdapter(Callback callback) {
        mCallback = callback;
    }

    @Override
    public int getItemViewType(int position) {
        int size = mModels.size();
        if (size >= MAX_SIZE)
            return TYPE_NONE;
        else if (position == size) {
            return TYPE_ADD;
        } else {
            return TYPE_NONE;
        }
    }

    @Override
    public SelectImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_perview_picture_list, parent, false);
        if (viewType == TYPE_NONE) {
            return new SelectImageHolder(view, new SelectImageHolder.HolderListener() {
                @Override
                public void onDelete(Model model) {
                    Callback callback = mCallback;
                    if (callback != null) {
                        int pos = mModels.indexOf(model);
                        if (pos == -1)
                            return;
                        mModels.remove(pos);
                        if (mModels.size() > 0)
                            notifyItemRemoved(pos);
                        else
                            notifyDataSetChanged();
                    }
                }

                @Override
                public void onDrag(SelectImageHolder holder) {
                    Callback callback = mCallback;
                    if (callback != null) {
                        // Start a drag whenever the handle view it touched
                        mCallback.onStartDrag(holder);
                    }
                }
            });
        } else {
            return new SelectImageHolder(view, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Callback callback = mCallback;
                    if (callback != null) {
                        callback.onLoadAllPictures();
                    }
                }
            });
        }
    }

    @Override
    public void onBindViewHolder(final SelectImageHolder holder, int position) {
        int size = mModels.size();
        if (size >= MAX_SIZE || size != position) {
            Model model = mModels.get(position);
            holder.bind(position, model, mCallback.getImgLoader());
        }
    }

    @Override
    public void onViewRecycled(SelectImageHolder holder) {
        Glide.clear(holder.mImage);
    }

    @Override
    public int getItemCount() {
        int size = mModels.size();
        if (size == MAX_SIZE) {
            return size;
        } else if (size == 0) {
            return 0;
        } else {
            return size + 1;
        }
    }

    public void clear() {
        mModels.clear();
    }

    public void add(Model model) {
        if (mModels.size() >= MAX_SIZE)
            return;
        mModels.add(model);
    }

    public void add(String path) {
        add(new Model(path));
    }

    public String[] getPaths() {
        int size = mModels.size();
        if (size == 0)
            return null;
        String[] paths = new String[size];
        int i = 0;
        for (Model model : mModels) {
            paths[i++] = model.path;
        }
        return paths;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        //Collections.swap(mModels, fromPosition, toPosition);
        if (fromPosition == toPosition)
            return false;

        if (fromPosition < toPosition) {
            Model fromModel = mModels.get(fromPosition);
            Model toModel = mModels.get(toPosition);

            mModels.remove(fromPosition);
            mModels.add(mModels.indexOf(toModel) + 1, fromModel);
        } else {
            Model fromModel = mModels.get(fromPosition);
            mModels.remove(fromPosition);
            mModels.add(toPosition, fromModel);
        }

        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        mModels.remove(position);
        notifyItemRemoved(position);
    }

    public static class Model {
        public Model(String path) {
            this.path = path;
        }

        public String path;
        public boolean isUpload;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isUpload() {
            return isUpload;
        }

        public void setUpload(boolean upload) {
            isUpload = upload;
        }
    }

    /**
     * SelectImageHolder
     */
    static class SelectImageHolder extends RecyclerView.ViewHolder implements PicturesPreviewerItemTouchCallback.ItemTouchHelperViewHolder {
        private ImageView mImage;
        private ImageView mDelete;
        private ImageView mGifMask;
        private HolderListener mListener;

        private SelectImageHolder(View itemView, HolderListener listener) {
            super(itemView);
            mListener = listener;
            mImage = (ImageView) itemView.findViewById(R.id.iv_content);
            mDelete = (ImageView) itemView.findViewById(R.id.iv_delete);
            mGifMask = (ImageView) itemView.findViewById(R.id.iv_is_gif);

            mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Object obj = v.getTag();
                    final HolderListener holderListener = mListener;
                    if (holderListener != null && obj != null && obj instanceof SelectImageAdapter.Model) {
                        holderListener.onDelete((SelectImageAdapter.Model) obj);
                        Log.e(SelectImageAdapter.class.getSimpleName(), ((Model) obj).getPath());
                    }
                }
            });
            mImage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final HolderListener holderListener = mListener;
                    if (holderListener != null) {
                        holderListener.onDrag(SelectImageHolder.this);
                    }
                    return true;
                }
            });
            mImage.setBackgroundColor(0xffdadada);
        }

        private SelectImageHolder(View itemView, View.OnClickListener clickListener) {
            super(itemView);

            mImage = (ImageView) itemView.findViewById(R.id.iv_content);
            mDelete = (ImageView) itemView.findViewById(R.id.iv_delete);

            mDelete.setVisibility(View.GONE);
            mImage.setImageResource(R.mipmap.ic_add);
            mImage.setOnClickListener(clickListener);
            mImage.setBackgroundDrawable(null);
        }

        public void bind(int position, SelectImageAdapter.Model model, RequestManager loader) {
            mDelete.setTag(model);
            // In this we need clear before load
            Glide.clear(mImage);
            // Load image
            if (model.path.toLowerCase().endsWith("gif")) {
                loader.load(model.path)
                        .asBitmap()
                        .centerCrop()
                        .error(R.mipmap.ic_split_graph)
                        .into(mImage);
                // Show gif mask
                mGifMask.setVisibility(View.VISIBLE);
            } else {
                loader.load(model.path)
                        .centerCrop()
                        .error(R.mipmap.ic_split_graph)
                        .into(mImage);
                mGifMask.setVisibility(View.GONE);
            }
        }


        @Override
        public void onItemSelected() {
            try {
                Vibrator vibrator = (Vibrator) itemView.getContext().getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(20);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onItemClear() {

        }

        /**
         * Holder 与Adapter之间的桥梁
         */
        interface HolderListener {
            void onDelete(SelectImageAdapter.Model model);

            void onDrag(SelectImageHolder holder);
        }
    }

}
