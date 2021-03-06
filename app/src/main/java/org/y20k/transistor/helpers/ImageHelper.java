/**
 * ImageHelper.java
 * Implements the ImageHelper class
 * An ImageHelper formats icons and symbols for use in the app ui
 *
 * This file is part of
 * TRANSISTOR - Radio App for Android
 *
 * Copyright (c) 2015-16 - Y20K.org
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */


package org.y20k.transistor.helpers;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v4.content.ContextCompat;

import org.y20k.transistor.R;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;


/**
 * ImageHelper class
 */
public final class ImageHelper {

    /* Main class variables */
    private final Bitmap mInputImage;
    private final Activity mActivity;


    /* Constructor when given a Bitmap*/
    public ImageHelper(Bitmap inputImage, Activity activity) {
        mInputImage = inputImage;
        mActivity = activity;
    }


    /* Constructor when given an Uri */
    public ImageHelper(Uri inputImageUri, Activity activity) {
        mActivity = activity;
        mInputImage = decodeSampledBitmapFromUri(inputImageUri, 72, 72);
    }


    /* Creates shortcut icon for Home screen */
    public Bitmap createShortcut(int size) {

        // get scaled background bitmap
        Bitmap background = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.background_shortcut_grey);
        background = Bitmap.createScaledBitmap(background, size, size, false);

        // compose output image
        Bitmap outputImage = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outputImage);
        canvas.drawBitmap(background, 0, 0, null);
        canvas.drawBitmap(mInputImage, createTransformationMatrix(size), null);

        return outputImage;
    }


    /* Creates station image on a circular background */
    public Bitmap createCircularFramedImage(int size, int color) {

        Paint background = createBackground(color);

        // create empty bitmap and canvas
        Bitmap outputImage = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas imageCanvas = new Canvas(outputImage);

        // draw circular background
        float cx = size / 2;
        float cy = size / 2;
        float radius = size / 2;
        imageCanvas.drawCircle(cx, cy, radius, background);

        // draw input image onto canvas using transformation matrix
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        imageCanvas.drawBitmap(mInputImage, createTransformationMatrix(size), paint);

        return outputImage;
    }


    /* Setter for color of background */
    private Paint createBackground(int color) {

        // get background color value in the form 0xAARRGGBB
        int backgroundColor;
        try {
            backgroundColor = ContextCompat.getColor(mActivity, color);
        } catch (Exception e) {
            // set default background color white
            backgroundColor = ContextCompat.getColor(mActivity, R.color.transistor_white);
            e.printStackTrace();
        }

        // construct circular background
        Paint background = new Paint();
        background.setColor(backgroundColor);
        background.setStyle(Paint.Style.FILL);

        return background;
    }


    /* Creates a transformation matrix for given */
    private Matrix createTransformationMatrix (int size) {
        Matrix matrix = new Matrix();

        // get size of original image and calculate padding
        float inputImageHeight = (float)mInputImage.getHeight();
        float inputImageWidth = (float)mInputImage.getWidth();
        float padding = (float)size/4;

        // define variables needed for transformation matrix
        float aspectRatio = 0.0f;
        float xTranslation = 0.0f;
        float yTranslation = 0.0f;

        // landscape format and square
        if (inputImageWidth >= inputImageHeight) {
            aspectRatio = (size - padding*2) / inputImageWidth;
            xTranslation = 0.0f + padding;
            yTranslation = (size - inputImageHeight * aspectRatio)/2.0f;
        }
        // portrait format
        else if (inputImageHeight > inputImageWidth) {
            aspectRatio = (size - padding*2) / inputImageHeight;
            yTranslation = 0.0f + padding;
            xTranslation = (size - inputImageWidth * aspectRatio)/2.0f;
        }

        // construct transformation matrix
        matrix.postTranslate(xTranslation, yTranslation);
        matrix.preScale(aspectRatio, aspectRatio);

        return matrix;
    }


    /* Return sampled down image for given Uri */
    private Bitmap decodeSampledBitmapFromUri(Uri imageUri, int reqWidth, int reqHeight) {

        Bitmap bitmap;
        ParcelFileDescriptor parcelFileDescriptor =  null;

        try {
            parcelFileDescriptor = mActivity.getContentResolver().openFileDescriptor(imageUri, "r");
        } catch (FileNotFoundException e) {
            // TODO handle error
            e.printStackTrace();
        }

        if (parcelFileDescriptor != null) {
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

            // decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

            // calculate inSampleSize
            options.inSampleSize = calculateSampleParameter(options, reqWidth, reqHeight);

            // decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

            return bitmap;

        } else {
            return null;
        }

    }


    /* Calculates parameter needed to scale image down */
    private static int calculateSampleParameter(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // get size of original image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // calculates the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }


    /* Getter for input image */
    public Bitmap getInputImage() {
        return mInputImage;
    }

}