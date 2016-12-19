package com.common.pictureselector.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * File Description  : 文件夹
 *
 * @author : zhanggeng
 * @email : zhanggengdyx@gmail.com
 * @date : 2016/12/19 21:05
 * @version     : v1.0
 * **************修订历史*************
 */

public class ImageFolder implements Serializable{

    private String name;
    private String path;
    private String albumPath;
    private ArrayList<Image> images = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ArrayList<Image> getImages() {
        return images;
    }

    public String getAlbumPath() {
        return albumPath;
    }

    public void setAlbumPath(String albumPath) {
        this.albumPath = albumPath;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof ImageFolder) {
            if (((ImageFolder) o).getPath() == null && path != null)
                return false;
            String oPath = ((ImageFolder) o).getPath().toLowerCase();
            return oPath.equals(this.path.toLowerCase());
        }
        return false;
    }

}
