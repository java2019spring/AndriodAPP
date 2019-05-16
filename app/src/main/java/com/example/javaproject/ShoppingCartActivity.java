package com.example.javaproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ShoppingCartActivity extends AppCompatActivity {

    public static final int TAKE_PHOTO = 1;
    private ImageView picture;
    private Uri imageUri;
    private static final String TAG = "ShoppingCartActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);

        Button takePhoto = (Button) findViewById(R.id.take_photo);
        picture = (ImageView) findViewById(R.id.picture);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create a File object to save the photos
                String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/JavaProject/photo_image";
                if(!new File(dir).exists()) {
                    if(!new File(dir).mkdirs())
                        Log.d(TAG, "dir: cannot create dir");
                    Log.d(TAG, "dir: "+dir);
                }
                //get the state of external storage
                String state = Environment.getExternalStorageState();
                //if the state != MOUNTED, we cannot read or write any file
                if (!state.equals(Environment.MEDIA_MOUNTED)) {
                    return;
                }
                //create image file
                File outputImage = new File(dir + "/output_image.jpg");
                Log.d(TAG, "path: "+ dir+"/output_image.jpg");
                try{
                    if(outputImage.exists())
                        outputImage.delete();
                    outputImage.createNewFile();
                }
                catch (IOException e)
                {
                    Log.d(TAG, "dir: Fail to create image file!");
                    e.printStackTrace();
                }

                //if VERSION >= Android7.0, using the real path of any file will be regarded as unsafe
                if(Build.VERSION.SDK_INT >= 24)
                    imageUri = FileProvider.getUriForFile(ShoppingCartActivity.this, "com.example.cameraalbum.fileprovider", outputImage);
                else
                    imageUri = Uri.fromFile(outputImage);

                //start the camera program
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, TAKE_PHOTO);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d(TAG, "onActivityResult: enter onActivityResult!");
        switch(requestCode)
        {
            case TAKE_PHOTO:
                if(resultCode == RESULT_OK)
                {
                    try
                    {
                        Log.d(TAG, "onActivityResult: set image successfully!");
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        picture.setImageBitmap(bitmap);
                    }
                    catch(FileNotFoundException e)
                    {
                        Log.d(TAG, "onActivityResult: cannot display image!");
                        e.printStackTrace();
                    }
                }
                break;
            default:
                Log.d(TAG, "onActivityResult: requestCode != OK");
                break;
        }
    }
}
