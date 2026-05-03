package com.example.distributed_gambling_platform;

import android.graphics.Bitmap;

public class ImageVault {
    private static Bitmap image;

    public static void setImageBm(Bitmap imageBm) {
        image = imageBm;
    }

    public static Bitmap getImageBm() {
        Bitmap tempImage = image;
        image = null;
        return tempImage;
    }

}
