package es.urjc.mov.javsan.cards.fichas;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;


/**
 * This class show the photos on one card, the images
 * are passed like path's to the class and the class
 * open the images and show the images on the Activity
 * CardShow in its UI.
 *
 */

public class SeePhotos {

    private AppCompatActivity appCompatActivity;
    private int posImage;

    SeePhotos(AppCompatActivity a, int pos , ArrayList<String> images) {
        appCompatActivity = a;
        posImage = pos;

        if (isStorageWritable()) {
            useStorage(images);
        }
    }

    public int actualPhoto() {
        return posImage;
    }

    private boolean isStorageWritable() {
        String state = Environment.getExternalStorageState();

        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private void useStorage(ArrayList<String> images) {
        ImageButton forward = (ImageButton) appCompatActivity.findViewById(R.id.forward);
        forward.setOnClickListener(new Forward(images));

        ImageButton reward = (ImageButton) appCompatActivity.findViewById(R.id.reward);
        reward.setOnClickListener(new Reward(images));

        if (images.size() > 0) {
            showImage(images.get(posImage));
        }
    }

    /**
     *
     * Button, going to the next photo on the array images...
     */
    private class Forward implements View.OnClickListener {
        private ArrayList<String> images;

        Forward (ArrayList<String> i) {
            images = i;
        }

        @Override
        public void onClick(View v) {
            if (images.size() > 0) {
                posImage = (posImage + 1) % images.size();
                showImage(images.get(posImage));
            }
        }
    }

    /**
     *
     * Button, going to the previous photo on the array images...
     */
    private class Reward implements View.OnClickListener {
        private ArrayList<String> images;

        Reward (ArrayList<String> i) {
            images = i;
        }

        @Override
        public void onClick(View v) {
            if (images.size() > 0) {
                posImage = (posImage - 1) % images.size();
                if (posImage < 0) {
                    posImage = images.size() - 1;
                }
                showImage(images.get(posImage));
            }
        }
    }


    private void showImage(String image) {
        File file = new File(image);
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(file);

            Bitmap b = BitmapFactory.decodeStream(fis);
            ImageView imageView = (ImageView) appCompatActivity.findViewById(R.id.images_cards);

            imageView.setImageBitmap(b);
            imageView.setImageURI(Uri.fromFile(file));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        } catch (IOException e) {
            ;
        } finally {
            close(fis);
        }
    }

    private void close (FileInputStream fis) {
        try {
            if (fis != null) {
                fis.close();
            }
        } catch (IOException e){
        }
    }
}
