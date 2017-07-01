package com.common.pictureselector.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.common.pictureselector.R;
import com.common.pictureselector.activities.ImageGalleryActivity;
import com.common.pictureselector.activities.PictureSelectorActivity;
import com.common.pictureselector.adapter.BaseRecyclerAdapter;
import com.common.pictureselector.adapter.ImageAdapter;
import com.common.pictureselector.adapter.ImageFolderAdapter;
import com.common.pictureselector.bean.Image;
import com.common.pictureselector.bean.ImageFolder;
import com.common.pictureselector.dialog.ImageFolderPopupWindow;
import com.common.pictureselector.interf.ImageLoaderListener;
import com.common.pictureselector.interf.PictureSelectContact;
import com.common.pictureselector.utils.SpaceGridItemDecoration;
import com.common.pictureselector.utils.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;

/**
 * File Description  : 图片选择的真实数据填充页面
 *
 * @author : zhanggeng
 * @version : v1.0
 *          **************修订历史*************
 * @email : zhanggengdyx@gmail.com
 * @date : 2016/12/19 11:34
 */

public class PictureSelectorFragment extends BaseFragment implements PictureSelectContact.View, BaseRecyclerAdapter.OnItemClickListener, ImageLoaderListener {


    RelativeLayout mToolbar;
    ImageView iconBack;
    Button mSelectFolderView;
    ImageView mSelectFolderIcon;
    RecyclerView mContentView;
    Button mPreviewView;
    Button mDoneView;
    RelativeLayout flImgSelector;

    private ImageFolderPopupWindow mFolderPopupWindow;
    private ImageFolderAdapter mImageFolderAdapter;
    private ImageAdapter mImageAdapter;

    private List<Image> mSelectedImage;

    private String mCamImageName;

    private LoaderListener mCursorLoader = new LoaderListener();

    private PictureSelectContact.Operator mOperator;

    @Override
    public void onAttach(Context context) {
        this.mOperator = (PictureSelectContact.Operator) context;
        this.mOperator.setDataView(this);
        super.onAttach(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_pictures_selector;
    }

    @Override
    protected void initView(View view) {

        mToolbar = findView(R.id.rl_title);
        iconBack = findView(R.id.icon_back);
        mSelectFolderIcon = findView(R.id.iv_title_select);
        mContentView = findView(R.id.rv_pic);
        mPreviewView = findView(R.id.btn_preview);
        mDoneView = findView(R.id.btn_complete);
        flImgSelector = findView(R.id.fl_img_selector);
        mSelectFolderView = findView(R.id.btn_title_select);

        mContentView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mContentView.addItemDecoration(new SpaceGridItemDecoration((int) dipToPx(getResources(), 2)));
        mImageAdapter = new ImageAdapter(getContext(), this);
        mImageFolderAdapter = new ImageFolderAdapter(getActivity());
        mImageFolderAdapter.setLoader(this);
        mContentView.setAdapter(mImageAdapter);
        mContentView.setItemAnimator(null);
        mImageAdapter.setOnItemClickListener(this);
    }

    /**
     * Change Dip to PX
     *
     * @param resources Resources
     * @param dp        Dip
     * @return PX
     */
    public static float dipToPx(Resources resources, float dp) {
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
    }

    @Override
    protected void initData() {
        mSelectedImage = new ArrayList<>();
        PictureSelectorActivity.Config config = mOperator.getConfig();
        if (config.getSelectCount() > 1 && config.getSelectedImages() != null) {
            String[] images = config.getSelectedImages();
            for (String s : images) {
                // check file exists
                if (s != null && new File(s).exists()) {
                    Image image = new Image();
                    image.setSelect(true);
                    image.setPath(s);
                    mSelectedImage.add(image);
                }
            }
        }
        getLoaderManager().initLoader(0, null, mCursorLoader);
    }


    @OnClick({R.id.icon_back, R.id.btn_title_select, R.id.btn_preview, R.id.btn_complete})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.icon_back:
                // TODO: 2016/12/19 点击返回按钮
                mOperator.onBack();
                break;
            case R.id.btn_title_select:
                // TODO: 2016/12/19 点击选择图片，弹出相册对话框
                showPopupFolderList();
                break;
            case R.id.btn_preview:
                // TODO: 2016/12/19  图片预览动作
                if (mSelectedImage.size() > 0) {
                    ImageGalleryActivity.show(getActivity(), Util.toArray(mSelectedImage), 0, false);
                }
                break;
            case R.id.btn_complete:
                // TODO: 2016/12/19 图片选择完成
                onSelectComplete();
                break;
        }
    }

    private void onSelectComplete() {
        handleResult();
    }


    @Override
    public void onOpenCameraSuccess() {
        toOpenCamera();
    }

    @Override
    public void onCameraPermissionDenied() {

    }

    /**
     * 打开相机
     */
    private void toOpenCamera() {
        // 判断是否挂载了SD卡
        mCamImageName = null;
        String savePath = "";
        if (Util.hasSDCard()) {
            savePath = Util.getCameraPath();
            File saveDir = new File(savePath);
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }
        }

        // 没有挂载SD卡，无法保存文件
        if (TextUtils.isEmpty(savePath)) {
            Toast.makeText(getActivity(), "无法保存照片，请检查SD卡是否挂载", Toast.LENGTH_LONG).show();
            return;
        }

        mCamImageName = Util.getSaveImageFullName();
        File out = new File(savePath, mCamImageName);
        Uri uri = Uri.fromFile(out);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent,
                0x03);
    }

    /**
     * 拍照完成通知系统添加照片
     *
     * @param requestCode requestCode
     * @param resultCode  resultCode
     * @param data        data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == 0x03 && mCamImageName != null) {
            Uri localUri = Uri.fromFile(new File(Util.getCameraPath() + mCamImageName));
            Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri);
            getActivity().sendBroadcast(localIntent);
        }
    }


    @Override
    public void onItemClick(int position, long itemId) {
        PictureSelectorActivity.Config config = mOperator.getConfig();
        if (config.isHaveCamera()) {
            if (position != 0) {
                handleSelectChange(position);
            } else {
                if (mSelectedImage.size() < config.getSelectCount()) {
                    mOperator.requestCamera();
                } else {
                    Toast.makeText(getActivity(), "最多只能选择 " + config.getSelectCount() + " 张图片", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            handleSelectChange(position);
        }
    }

    private void handleSelectChange(int position) {
        Image image = mImageAdapter.getItem(position);
        PictureSelectorActivity.Config config = mOperator.getConfig();
        //如果是多选模式
        final int selectCount = config.getSelectCount();
        if (selectCount > 1) {
            if (image.isSelect()) {
                image.setSelect(false);
                mSelectedImage.remove(image);
                mImageAdapter.updateItem(position);
            } else {
                if (mSelectedImage.size() == selectCount) {
                    Toast.makeText(getActivity(), "最多只能选择 " + selectCount + " 张照片", Toast.LENGTH_SHORT).show();
                } else {
                    image.setSelect(true);
                    mSelectedImage.add(image);
                    mImageAdapter.updateItem(position);
                }
            }
            handleSelectSizeChange(mSelectedImage.size());
        } else {
            mSelectedImage.add(image);
            handleResult();
        }
    }


    @Override
    public void displayImage(ImageView iv, String path) {
        getImgLoader().load(path)
                .asBitmap()
                .centerCrop()
                .error(R.mipmap.ic_split_graph)
                .into(iv);
    }

    private class LoaderListener implements LoaderManager.LoaderCallbacks<Cursor> {
        private final String[] IMAGE_PROJECTION = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.MINI_THUMB_MAGIC,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (id == 0) {
                //数据库光标加载器
                return new CursorLoader(getContext(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                        null, null, IMAGE_PROJECTION[2] + " DESC");
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
                ArrayList<Image> images = new ArrayList<>();
                List<ImageFolder> imageFolders = new ArrayList<>();

                ImageFolder defaultFolder = new ImageFolder();
                defaultFolder.setName("全部照片");
                defaultFolder.setPath("");
                imageFolders.add(defaultFolder);

                int count = data.getCount();
                if (count > 0) {
                    data.moveToFirst();
                    do {
                        String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                        String name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                        long dateTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
                        int id = data.getInt(data.getColumnIndexOrThrow(IMAGE_PROJECTION[3]));
                        String thumbPath = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[4]));
                        String bucket = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[5]));

                        Image image = new Image();
                        image.setPath(path);
                        image.setName(name);
                        image.setDate(dateTime);
                        image.setId(id);
                        image.setThumbPath(thumbPath);
                        image.setFolderName(bucket);
                        images.add(image);

                        //如果是新拍的照片
                        if (mCamImageName != null && mCamImageName.equals(image.getName())) {
                            image.setSelect(true);
                            mSelectedImage.add(image);
                        }

                        //如果是被选中的图片
                        if (mSelectedImage.size() > 0) {
                            for (Image i : mSelectedImage) {
                                if (i.getPath().equals(image.getPath())) {
                                    image.setSelect(true);
                                }
                            }
                        }

                        File imageFile = new File(path);
                        File folderFile = imageFile.getParentFile();
                        ImageFolder folder = new ImageFolder();
                        folder.setName(folderFile.getName());
                        folder.setPath(folderFile.getAbsolutePath());
                        if (!imageFolders.contains(folder)) {
                            folder.getImages().add(image);
                            folder.setAlbumPath(image.getPath());//默认相册封面
                            imageFolders.add(folder);
                        } else {
                            // 更新
                            ImageFolder f = imageFolders.get(imageFolders.indexOf(folder));
                            f.getImages().add(image);
                        }


                    } while (data.moveToNext());
                }

                addImagesToAdapter(images);
                defaultFolder.getImages().addAll(images);
                if (mOperator.getConfig().isHaveCamera()) {
                    defaultFolder.setAlbumPath(images.size() > 1 ? images.get(1).getPath() : null);
                } else {
                    defaultFolder.setAlbumPath(images.size() > 0 ? images.get(0).getPath() : null);
                }
                mImageFolderAdapter.resetItem(imageFolders);

                //删除掉不存在的，在于用户选择了相片，又去相册删除
                if (mSelectedImage.size() > 0) {
                    List<Image> rs = new ArrayList<>();
                    for (Image i : mSelectedImage) {
                        File f = new File(i.getPath());
                        if (!f.exists()) {
                            rs.add(i);
                        }
                    }
                    mSelectedImage.removeAll(rs);
                }

                // If add new mCamera picture, and we only need one picture, we result it.
                if (mOperator.getConfig().getSelectCount() == 1 && mCamImageName != null) {
                    handleResult();
                }

                handleSelectSizeChange(mSelectedImage.size());
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

    private void handleResult() {
        if (mOperator != null && mSelectedImage.size() != 0) {
            mOperator.getCallback().doSelectDone(toArray(mSelectedImage));
            getActivity().finish();
        }
    }

    private void handleSelectSizeChange(int size) {
        if (size > 0) {
            mPreviewView.setEnabled(true);
            mPreviewView.setTextColor(getResources().getColor(R.color.white));
            mDoneView.setEnabled(true);
            mDoneView.setTextColor(getResources().getColor(R.color.white));
            mDoneView.setText(String.format("%s(%s)", getText(R.string.image_select_opt_done), size));
        } else {
            mPreviewView.setEnabled(false);
            mPreviewView.setTextColor(getResources().getColor(R.color.color_939393));
            mDoneView.setEnabled(false);
            mDoneView.setTextColor(getResources().getColor(R.color.color_939393));
            mDoneView.setText(getText(R.string.image_select_opt_done));
        }
    }

    public static String[] toArray(List<Image> images) {
        if (images == null)
            return null;
        int len = images.size();
        if (len == 0)
            return null;

        String[] strings = new String[len];
        int i = 0;
        for (Image image : images) {
            strings[i] = image.getPath();
            i++;
        }
        return strings;
    }


    private void addImagesToAdapter(ArrayList<Image> images) {
        mImageAdapter.clear();
        if (mOperator.getConfig().isHaveCamera()) {
            Image cam = new Image();
            mImageAdapter.addItem(cam);
        }
        mImageAdapter.addAll(images);
    }

    /**
     * 创建弹出的相册
     */
    private void showPopupFolderList() {
        if (mFolderPopupWindow == null) {
            ImageFolderPopupWindow popupWindow = new ImageFolderPopupWindow(getActivity(), new ImageFolderPopupWindow.Callback() {
                @Override
                public void onSelect(ImageFolderPopupWindow popupWindow, ImageFolder model) {
                    addImagesToAdapter(model.getImages());
                    mContentView.scrollToPosition(0);
                    popupWindow.dismiss();
                    mSelectFolderView.setText(model.getName());
                }

                @Override
                public void onDismiss() {
                    mSelectFolderIcon.setImageResource(R.mipmap.ic_arrow_bottom);
                }

                @Override
                public void onShow() {
                    mSelectFolderIcon.setImageResource(R.mipmap.ic_arrow_top);
                }
            });
            popupWindow.setAdapter(mImageFolderAdapter);
            mFolderPopupWindow = popupWindow;
        }
        mFolderPopupWindow.showAsDropDown(mToolbar);
    }
}
