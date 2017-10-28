package com.example.androidthings.aicamera;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.androidthings.aicamera.classifier.Classifier;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by abhishek on 27/10/17.
 */
public class ImageUploader {
    private static final String TAG = ImageUploader.class.getSimpleName();
    private static Integer count;
    private static Integer pause;
    private static Integer delay;
    private static boolean lock;
    private static String LOCAL_TEMP_IMAGE = "/sdcard/temp.jpg";
    Handler mBackgroundHandler;
    private StorageReference mStorageRef;
    private HandlerThread mBackgroundThread;

    private ImageUploader() {
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mBackgroundThread = new HandlerThread("BackgroundThread");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        count = 0;
        delay = 0;
    }

    public static ImageUploader getInstance() {
        return InstanceHolder.mUploader;
    }

    public void uploadImage(Bitmap bitmap, List<Classifier.Recognition> objects) {
        if (needUpload(objects)) {
            new AsyncImageUpload().execute(bitmap);
        }

    }

    // TODO: If there is not change is object at all, don't upload as object may be stand still
    private boolean needUpload(List<Classifier.Recognition> objects) {
        for (int i = 0; i < objects.size(); i++) {

            Log.d(TAG, "" + objects.get(i).getConfidence());
            Log.d(TAG, "" + RemoteConfigs.getConfidenceThreshold());
            Log.d(TAG, "" + (objects.get(i).getConfidence() > RemoteConfigs.getConfidenceThreshold()));
            if (!lock && objects.get(i).getTitle().equals(RemoteConfigs.getTrainingObject())
                    && (objects.get(i).getConfidence() > RemoteConfigs.getConfidenceThreshold())
                    && count < RemoteConfigs.getCount()) {
                return true;
            }
        }
        return false;
    }

    private void delayNextUpload() {
        lock = true;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                lock = false;
            }
        }, RemoteConfigs.getDelay());
    }

    private void uploadImage(Bitmap bitmap) {
        delayNextUpload();

        String uploadFileName = RemoteConfigs.getTrainingObject()
                + "/" + RemoteConfigs.getTrainingObject() + "_" + count + ".jpg";

        Log.d(TAG, "Uploading Image:" + uploadFileName);
        ++count;

        OutputStream stream;
        try {
            stream = new FileOutputStream(LOCAL_TEMP_IMAGE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        Uri uri = Uri.fromFile(new File(LOCAL_TEMP_IMAGE));

        StorageReference uploadRef = mStorageRef.child(uploadFileName);
        uploadRef.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Upload filed for reason:" + e.getCause());
                    }
                });
    }

    private static class InstanceHolder {
        private static ImageUploader mUploader = new ImageUploader();
    }

    private class AsyncImageUpload extends AsyncTask<Bitmap, Void, Void> {

        @Override
        protected Void doInBackground(Bitmap... bitmaps) {
            uploadImage(bitmaps[0]);
            return null;
        }
    }
}
