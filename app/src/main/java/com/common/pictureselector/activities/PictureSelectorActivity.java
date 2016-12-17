package com.common.pictureselector.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.common.pictureselector.R;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * File Description  : 图片选择器页面
 *
 * @author : zhanggeng
 * @version : v1.0
 *          **************修订历史*************
 * @email : zhanggengdyx@gmail.com
 * @date : 2016/12/17 10:33
 */

public class PictureSelectorActivity extends Activity implements EasyPermissions.PermissionCallbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
    }

    private void initView() {
        setContentView(R.layout.activity_pictures_selector);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    }
}
