package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.chatapp.model.ImageMessage;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ShowImageActivity extends AppCompatActivity {

    PhotoView imageView;
    FloatingActionButton fabSave, fabShare, fabAdd;
    Toolbar image_toolbar;
    PhotoViewAttacher pv;
    ImageMessage imageMessage;
    Animation rotateOpen, rotateClose, fromBottom, toBottom;
    boolean clicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        image_toolbar = findViewById(R.id.image_message_toolbar);
        fabSave = findViewById(R.id.fab_save_image);
        fabShare = findViewById(R.id.fab_share_image);
        fabAdd = findViewById(R.id.fab_add);
        rotateOpen = AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim);
        rotateClose = AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim);
        fromBottom = AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim);
        toBottom = AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim);

        setSupportActionBar(image_toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        imageView = findViewById(R.id.imageViewShow);
        /*pv = new PhotoViewAttacher(imageView);
        pv.update();*/
        imageMessage = (ImageMessage) getIntent().getSerializableExtra("imageMessage");
        Glide.with(this).load(imageMessage.getImageUri()).into(imageView);

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddButtonClicked();
            }
        });

        fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                savePhoto(bitmap);
            }
        });

        fabShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                savePhotoAndShare(bitmap);
            }
        });
    }

    private void onAddButtonClicked() {
        setVisibility();
        setAnimation();
        setClickable();
        clicked = !clicked;
    }

    private void setVisibility() {
        if (!clicked) {
            fabShare.setVisibility(View.VISIBLE);
            fabSave.setVisibility(View.VISIBLE);
        } else {
            fabShare.setVisibility(View.INVISIBLE);
            fabSave.setVisibility(View.INVISIBLE);
        }
    }

    private void setAnimation() {
        if(!clicked){
            fabSave.startAnimation(fromBottom);
            fabShare.startAnimation(fromBottom);
            fabAdd.startAnimation(rotateOpen);
        }else{
            fabSave.startAnimation(toBottom);
            fabShare.startAnimation(toBottom);
            fabAdd.startAnimation(rotateClose);
        }
    }

    void setClickable(){
        if(!clicked){
            fabShare.setClickable(true);
            fabSave.setClickable(true);
        }else{
            fabShare.setClickable(false);
            fabSave.setClickable(false);
        }
    }


    private void shareImage(Uri uri) {
        //Toast.makeText(this, uri.toString(), Toast.LENGTH_SHORT).show();
        Intent n = new Intent();
        n.setAction(Intent.ACTION_SEND);
        n.setType("image/*");
        n.putExtra(Intent.EXTRA_STREAM, uri);
        n.putExtra(Intent.EXTRA_TEXT, "this image shared with you");
        Intent n1 = Intent.createChooser(n, "share Image");
        try {
            startActivity(n1);
            Toast.makeText(this, "here", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {

            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    void savePhotoAndShare(Bitmap bitmap) {
        try {
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArray);
            Calendar calendar = Calendar.getInstance();
            String fileName = "img_" + "sharedImage" + ".jpg";
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            new File(storageDir + "/SharePhoto").mkdirs();
            File outputFile = new File(storageDir + "/SharePhoto/", fileName);
            Log.e("te", outputFile.toString());
            FileOutputStream fos = new FileOutputStream(outputFile);
            fos.write(byteArray.toByteArray());
            fos.close();
            shareImage(FileProvider.getUriForFile(this, "com.example.chatapp.provider", outputFile));

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }

    void savePhoto(Bitmap photo) {

        try {
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 50, byteArray);
            String fileName;
            if(imageMessage.getMessageId()!=null)
                fileName = "img_" + imageMessage.getMessageId()+ ".jpg";
            else{
                Calendar calendar = Calendar.getInstance();
                fileName = "img_" + String.valueOf(calendar.getTimeInMillis()) + ".jpg";
            }
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            new File(storageDir + "/ChatApp").mkdirs();
            File outputFile = new File(storageDir + "/ChatApp/", fileName);
            FileOutputStream fos = new FileOutputStream(outputFile);
            fos.write(byteArray.toByteArray());
            fos.close();

            Toast.makeText(this, "image Saved", Toast.LENGTH_SHORT).show();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }
}




/*
try {
            String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FolderName";
            File dir = new File(file_path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, "Image_Share.PNG");

            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArray);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(byteArray.toByteArray());
            fos.close();
            Toast.makeText(this, "done", Toast.LENGTH_SHORT).show();
            shareImage(Uri.fromFile(file));

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

        }


        // save in studio directly

        MediaStore.Images.Media.insertImage(getContentResolver(),bitmap,imageMessage.getSenderName(),getPackageName()+imageMessage.getSenderName());
                Toast.makeText(ShowImageActivity.this, "image saved", Toast.LENGTH_SHORT).show();


                //shareImage(Uri.fromFile(outputFile));

 */