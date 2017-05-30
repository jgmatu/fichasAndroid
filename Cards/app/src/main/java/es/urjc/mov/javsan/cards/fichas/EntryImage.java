package es.urjc.mov.javsan.cards.fichas;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

/**
 * This class is used like a fragment in activity create card,
 * this class be responsible for add a new image selected by the
 * user on the form create card.
 */
public class EntryImage extends Fragment {

    private final String TAG = EntryImage.class.getSimpleName();

    private final int MY_PERMISSIONS_REQUEST_STORATE_WRITABLE = 1001;
    private final int REQUEST_IMAGE_CAPTURE = 1000;

    private ImageView selectedImage;
    private View fragmentLayout;
    private OnImagesListener onImagesListener;
    private String pathImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        fragmentLayout = inflater.inflate(R.layout.gallery_fragment, container, false);
        selectedImage = (ImageView) fragmentLayout.findViewById(R.id.imageView);
        pathImage = "";

        Button b = (Button) fragmentLayout.findViewById(R.id.image_new);
        b.setOnClickListener(new NewImage());

        b = (Button) fragmentLayout.findViewById(R.id.image_done);
        b.setOnClickListener(new ImageDone());

        requestPermission();
        return fragmentLayout;
    }

    /**
     * This method is used to compatibility with android OS with API < 23...
     *
     * @param activity Param neccesary to attach de fragment to an activity
     *                 in android API < 23...
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            setOnImagesListener(activity);
        }
    }

    /**
     * Necessary to force activity implement the interface when use the
     * class like a fragment...
     *
     * @param context The context of the activity to check if implements
     *                the interface.
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setOnImagesListener(context);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("image", pathImage);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {

            ImageView imageView = (ImageView) fragmentLayout.findViewById(R.id.imageView);
            pathImage = getRealPathFromURI(getContext(), data.getData());
            Bitmap bitmap = BitmapFactory.decodeFile(pathImage);

            imageView.setImageBitmap(bitmap);
        }
    }

    public interface OnImagesListener {
        void handleNewImage(String image);
    }

    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] realPath = { MediaStore.Images.Media.DATA };

            cursor = context.getContentResolver().query(contentUri, realPath, null, null, null);
            if (cursor == null) {
                return contentUri.getPath();
            }

            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();

            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private class NewImage implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            newImageFromMediaStore();
        }

        private void newImageFromMediaStore() {
            // Create intent to Open Image applications like Gallery, Google Photos.
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            // Start the Intent to get a new image from local gallery images.
            startActivityForResult(galleryIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private class ImageDone implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            onImagesListener.handleNewImage(pathImage);
            selectedImage.setImageResource(R.mipmap.ic_launcher);
            pathImage = "";
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_STORATE_WRITABLE : {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    Log.e(TAG, "Permission granted...");
                } else {
                    // permission was denied
                    Log.e(TAG, "Permission denied...");
                }
            }
        }

    }

    private void requestPermission() {
        if (isPermissionStorageDeny()) {
            requestStoragePermission();
        }
    }

    private boolean isPermissionStorageDeny(){
        return ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_STORATE_WRITABLE);
    }

    private void setOnImagesListener (Context context) {
        try {
            onImagesListener = (EntryImage.OnImagesListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnLocationListener");
        }
    }

}
