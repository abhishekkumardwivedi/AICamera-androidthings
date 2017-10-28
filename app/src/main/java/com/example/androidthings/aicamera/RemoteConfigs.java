package com.example.androidthings.aicamera;

import android.util.Log;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

/**
 * Created by abhishek on 27/10/17.
 */

//TODO: handle synchronization
public class RemoteConfigs {
    private static final String TAG = RemoteConfigs.class.getSimpleName();
    private static String trainingObject;

    private static Float confidenceThreshold;
    private static Integer delay;
    private static Integer pause;
    private static Integer count;
    private static Integer cacheExpireDelay;

    public static void doUpdate() {
        FirebaseRemoteConfig firebaseRemoteConfig =
                FirebaseRemoteConfig.getInstance();
        trainingObject = firebaseRemoteConfig.getString("training_object");
        confidenceThreshold = Float.valueOf(firebaseRemoteConfig.getString("confidence_threshold"));
        delay = Integer.valueOf(firebaseRemoteConfig.getString("delay"));
        pause = Integer.valueOf(firebaseRemoteConfig.getString("pause"));
        count = Integer.valueOf(firebaseRemoteConfig.getString("count"));
        cacheExpireDelay = Integer.valueOf(firebaseRemoteConfig.getString("fetch_cache_expire"));
    }

    public static String getTrainingObject() {
        return trainingObject;
    }

    public static Float getConfidenceThreshold() {
        return confidenceThreshold / 100;
    }

    public static Integer getDelay() {
        return delay;
    }

    public static Integer getPause() {
        return pause;
    }

    public static Integer getCount() {
        return count;
    }

    public static Integer getFectchDelay() {
        return cacheExpireDelay;
    }

    public static void dump() {
        Log.d(TAG, "-------------------------------");
        Log.d(TAG, "training_object : " + getTrainingObject());
        Log.d(TAG, "confidence_threshold : " + getConfidenceThreshold());
        Log.d(TAG, "delay : " + getDelay());
        Log.d(TAG, "count : " + getCount());
        Log.d(TAG, "pause : " + getPause());
        Log.d(TAG, "fetch_cache_expire : " + getFectchDelay());
        Log.d(TAG, "-------------------------------");

    }
}
