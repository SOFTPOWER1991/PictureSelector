package com.common.pictureselector.interf;

public interface PublishContract {

    interface View {

        String[] getImages();

        void setImages(String[] paths);
    }
}
