package com.common.pictureselector.interf;

import com.common.pictureselector.activities.PictureSelectorActivity;

/**
 * File Description  : 图片选择器接口回调，将权限操作放在Activity，具体数据放在Fragment
 *
 * @author : zhanggeng
 * @email : zhanggengdyx@gmail.com
 * @date : 2016/12/19 11:06
 * @version     : v1.0
 * **************修订历史*************
 */

public interface PictureSelectContact {

    interface Operator {
        void requestCamera();

        void requestExternalStorage();

        void onBack();

        void setDataView(View view);

        PictureSelectorActivity.Callback getCallback();

        PictureSelectorActivity.Config getConfig();
    }

    interface View {

        void onOpenCameraSuccess();

        void onCameraPermissionDenied();
    }
}
