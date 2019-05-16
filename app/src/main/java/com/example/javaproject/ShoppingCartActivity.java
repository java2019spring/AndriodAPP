package com.example.javaproject;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ShoppingCartActivity extends AppCompatActivity {

    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;
    private ImageView picture;
    private Uri imageUri;
    private static final String TAG = "ShoppingCartActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);

        Button takePhoto = (Button) findViewById(R.id.take_photo);
        Button chooseFromAlbum = (Button) findViewById(R.id.choose_from_album);
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

        //choose from album
        chooseFromAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(ShoppingCartActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                 != PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(ShoppingCartActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                else
                    openAlbum();
            }
        });
    }

    private void openAlbum()
    {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        switch(requestCode)
        {
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    openAlbum();
                else
                    Toast.makeText(this, "The permission to writing external storage has been denied", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
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
            case CHOOSE_PHOTO:
                if(resultCode == RESULT_OK)
                {
                    //judge the Android version
                    if(Build.VERSION.SDK_INT >= 19)
                        handleImageOnKitKat(data);  //Android version >= 4.4
                    else
                        handleImageBeforeKitKat(data);  //Android version < 4.4
                }
                break;
            default:
                Log.d(TAG, "onActivityResult: requestCode != OK");
                break;
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data)
    {
        String imagePath = null;
        Uri uri = data.getData();
        //if the uri is of type "document", process it by document id.
        if(DocumentsContract.isDocumentUri(this, uri))
        {
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority()))
            {
                String id = docId.split(":")[1];    //get the id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            }
            else if("com.android.providers.downloads.documents".equals(uri.getAuthority()))
            {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        }
        else if("content".equalsIgnoreCase(uri.getScheme()))
            imagePath = getImagePath(uri, null);
        else if("file".equalsIgnoreCase(uri.getScheme()))
            imagePath = uri.getPath();

        displayImage(imagePath);
    }

    private void handleImageBeforeKitKat(Intent data)
    {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

    private String getImagePath(Uri uri, String selection)
    {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if(cursor != null)
        {
            if(cursor.moveToFirst())
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath)
    {
        if(imagePath != null)
        {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            picture.setImageBitmap(bitmap);
        }
        else
            Toast.makeText(this, "Failed to get image", Toast.LENGTH_SHORT).show();
    }
}
