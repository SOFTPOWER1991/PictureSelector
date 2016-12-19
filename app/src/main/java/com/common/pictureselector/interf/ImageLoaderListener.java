package com.common.pictureselector.interf;

import android.widget.ImageView;

/**
 * File Description  : 图片选择器
 *
 * @author : zhanggeng
 * @email : zhanggengdyx@gmail.com
 * @date : 2016/12/19 15:47
 * @version     : v1.0
 * **************修订历史*************
 */

public interface ImageLoaderListener {
    void displayImage(ImageView iv, String path);
}
