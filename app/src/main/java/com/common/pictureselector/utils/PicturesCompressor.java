package com.common.pictureselector.utils;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.common.pictureselector.utils.StreamUtils.close;
import static com.common.pictureselector.utils.StreamUtils.copyFile;


@SuppressWarnings("WeakerAccess")
public final class PicturesCompressor {
    /**
     * A default size to use to increase hit rates when the required size isn't defined.
     * Currently 64KB.
     */
    public final static int DEFAULT_BUFFER_SIZE = 64 * 1024;

    private PicturesCompressor() {

    }

    public static BitmapFactory.Options createOptions() {
        return new BitmapFactory.Options();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void resetOptions(BitmapFactory.Options options) {
        options.inTempStorage = null;
        options.inDither = false;
        options.inScaled = false;
        options.inSampleSize = 1;
        options.inPreferredConfig = null;
        options.inJustDecodeBounds = false;
        options.inDensity = 0;
        options.inTargetDensity = 0;
        options.outWidth = 0;
        options.outHeight = 0;
        options.outMimeType = null;

        if (Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT) {
            options.inBitmap = null;
            options.inMutable = true;
        }
    }

    public static String getExtension(String filePath) {
        BitmapFactory.Options options = createOptions();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        String mimeType = options.outMimeType;
        return mimeType.substring(mimeType.lastIndexOf("/") + 1);
    }

    public static Bitmap decodeBitmap(final String filePath,
                                      final int maxWidth,
                                      final int maxHeight,
                                      byte[] byteStorage,
                                      BitmapFactory.Options options,
                                      boolean exactDecode) {
        InputStream is;
        try {
            // In this, we can set the buffer size
            is = new BufferedInputStream(new FileInputStream(filePath),
                    byteStorage == null ? DEFAULT_BUFFER_SIZE : byteStorage.length);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        if (options == null)
            options = createOptions();
        else
            resetOptions(options);

        // First decode with inJustDecodeBounds=true to check dimensions
        options.inJustDecodeBounds = true;

        // 5MB. This is the max image header size we can handle, we preallocate a much smaller buffer
        // but will resize up to this amount if necessary.
        is.mark(5 * 1024 * 1024);
        BitmapFactory.decodeStream(is, null, options);

        // Reset the inputStream
        try {
            is.reset();
        } catch (IOException e) {
            e.printStackTrace();
            close(is);
            resetOptions(options);
            return null;
        }

        // Calculate inSampleSize
        calculateScaling(options, maxWidth, maxHeight, exactDecode);

        // Init the BitmapFactory.Options.inTempStorage value
        if (byteStorage == null)
            byteStorage = new byte[DEFAULT_BUFFER_SIZE];
        options.inTempStorage = byteStorage;

        // Decode bitmap with inSampleSize set FALSE
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);

        // Close the Stream
        close(is);
        // And Reset the option
        resetOptions(options);

        // To scale bitmap to user set
        bitmap = scaleBitmap(bitmap, maxWidth, maxHeight, true);

        return bitmap;

    }

    public static Bitmap scaleBitmap(Bitmap source, float scale, boolean recycleSource) {
        if (scale <= 0 || scale >= 1)
            return source;
        Matrix m = new Matrix();
        final int width = source.getWidth();
        final int height = source.getHeight();
        m.setScale(scale, scale);
        Bitmap scaledBitmap = Bitmap.createBitmap(source, 0, 0, width, height, m, false);
        if (recycleSource)
            source.recycle();
        return scaledBitmap;
    }

    public static Bitmap scaleBitmap(Bitmap source, int targetMaxWidth, int targetMaxHeight, boolean recycleSource) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        Bitmap scaledBitmap = source;
        if (sourceWidth > targetMaxWidth || sourceHeight > targetMaxHeight) {
            float minScale = Math.min(targetMaxWidth / (float) sourceWidth,
                    targetMaxHeight / (float) sourceHeight);
            scaledBitmap = Bitmap.createScaledBitmap(scaledBitmap,
                    (int) (sourceWidth * minScale),
                    (int) (sourceHeight * minScale), false);
            if (recycleSource)
                source.recycle();
        }

        return scaledBitmap;
    }

    public static boolean compressImage(final String srcPath,
                                        final String savePath,
                                        final long maxSize,
                                        final int minQuality,
                                        final int maxWidth,
                                        final int maxHeight) {
        return compressImage(srcPath, savePath, maxSize, minQuality, maxWidth, maxHeight, true);
    }

    public static boolean compressImage(final String srcPath,
                                        final String savePath,
                                        final long maxSize,
                                        final int minQuality,
                                        final int maxWidth,
                                        final int maxHeight,
                                        boolean exactDecode) {
        return compressImage(srcPath, savePath, maxSize, minQuality, maxWidth, maxHeight, null, null, exactDecode);
    }

    public static boolean compressImage(final String srcPath,
                                        final String savePath,
                                        final long maxSize,
                                        final int minQuality,
                                        final int maxWidth,
                                        final int maxHeight,
                                        byte[] byteStorage,
                                        BitmapFactory.Options options,
                                        boolean exactDecode) {
        // build source file
        final File sourceFile = new File(srcPath);
        if (!sourceFile.exists())
            return false;

        // build save file
        final File saveFile = new File(savePath);
        File saveDir = saveFile.getParentFile();
        if (!saveDir.exists()) {
            if (!saveDir.mkdirs())
                return false;
        }

        // if the in file size <= maxSize, we can copy to savePath
        if (sourceFile.length() <= maxSize) {
            return copyFile(sourceFile, saveFile);
        }

        // create new temp file
        final File tempFile = new File(saveDir, "temp.m");
        if (!tempFile.exists()) {
            try {
                if (!tempFile.createNewFile())
                    return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        // build to bitmap
        Bitmap bitmap = decodeBitmap(srcPath, maxWidth, maxHeight, byteStorage, options, exactDecode);
        if (bitmap == null)
            return false;

        Bitmap.CompressFormat compressFormat = bitmap.hasAlpha() ?
                Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;

        // Write to out put file
        BufferedOutputStream outputStream = null;
        try {
            boolean isOk = false;
            for (int i = 1; i <= 10; i++) {
                // In this we change the quality start 92%
                int quality = 92;
                for (; ; ) {
                    outputStream = new BufferedOutputStream(new FileOutputStream(tempFile));
                    bitmap.compress(compressFormat, quality, outputStream);
                    outputStream.flush();
                    close(outputStream);
                    long outSize = tempFile.length();
                    if (outSize <= maxSize) {
                        isOk = true;
                        break;
                    }
                    if (quality < minQuality)
                        break;
                    quality--;
                }

                if (isOk) {
                    break;
                } else {
                    // If not ok, we need scale the Bitmap to small
                    // In this, once subtract 2%, most 20%
                    bitmap = scaleBitmap(bitmap, 1 - (0.2f * i), true);
                }
            }
            // The end, If not success, return false
            if (!isOk)
                return false;
        } catch (IOException e) {
            e.printStackTrace();
            close(outputStream);
            return false;
        } finally {
            bitmap.recycle();
        }

        // End clear the out file data
        if (saveFile.exists()) {
            if (!saveFile.delete())
                return false;
        }
        // Rename to out file
        return tempFile.renameTo(saveFile);
    }

    private static void calculateScaling(BitmapFactory.Options options,
                                         final int requestedMaxWidth, final int requestedMaxHeight, boolean exactDecode) {
        int sourceWidth = options.outWidth;
        int sourceHeight = options.outHeight;

        if (sourceWidth <= requestedMaxWidth && sourceHeight <= requestedMaxHeight) {
            return;
        }

        final float maxFloatFactor = Math.max(sourceHeight / (float) requestedMaxHeight,
                sourceWidth / (float) requestedMaxWidth);
        final int maxIntegerFactor = (int) Math.floor(maxFloatFactor);
        final int lesserOrEqualSampleSize = Math.max(1, Integer.highestOneBit(maxIntegerFactor));

        options.inSampleSize = lesserOrEqualSampleSize;
        // Density scaling is only supported if inBitmap is null prior to KitKat. Avoid setting
        // densities here so we calculate the final Bitmap size correctly.
        if (exactDecode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            float scaleSize = sourceWidth / (float) lesserOrEqualSampleSize;
            float outSize = sourceWidth / maxFloatFactor;

            options.inTargetDensity = 1000;
            options.inDensity = (int) (1000 * (scaleSize / outSize) + 0.5);

            // If isScaling
            if (options.inTargetDensity != options.inDensity) {
                options.inScaled = true;
            } else {
                options.inDensity = options.inTargetDensity = 0;
            }
        }
    }

    public static String verifyPictureExt(String filePath) {
        BitmapFactory.Options option = createOptions();
        option.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, option);
        String mimeType = option.outMimeType.toLowerCase();
        int doIndex = filePath.lastIndexOf(".") + 1;
        String ext = filePath.substring(doIndex).toLowerCase();
        if (mimeType.contains("x-ico")) {
            //TODO
        } else if (mimeType.contains("jpeg")) {
            ext = "jpg";
        } else if (mimeType.contains("png")) {
            ext = "png";
        } else if (mimeType.contains("webp")) {
            //TODO
        } else if (mimeType.contains("vnd.wap.wbmp")) {
            //TODO
        }
        String newFilePath = filePath.substring(0, doIndex) + ext;

        if (!filePath.equals(newFilePath)) {
            if (new File(filePath).renameTo(new File(newFilePath)))
                return newFilePath;
        }
        return filePath;
    }
}
