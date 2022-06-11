package com.baice.app.sound;

import android.os.Handler;
import android.util.Log;


/**
 * Created by changbinhe on 14/11/22.
 */
public class SoundMan implements Supporter.OnOffSwitcher {
    private static final String TAG = "Filer";

    private static final class SingletonHolder {
        public static final SoundMan INSTANCE = new SoundMan();
    }

    public static SoundMan getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private Codec codec;
    private Filer filer;
    private Recorder recorder;
    private Uploader uploader;

    private boolean isRunning;
    private boolean initialized;
    private Handler handler;

    private SoundMan() {
        handler = new Handler();
    }

    @Override
    public boolean start() {
        Log.i(TAG, "try to start");
        if (isRunning) {
            Log.i(TAG, "already started");
            return false;
        }

        if (!initialized) {
            Log.i(TAG, "try init");
            init();
            initialized = true;
            Log.i(TAG, "init succeed");
        }
        isRunning = true;

        recorder.start();
        codec.start();
        filer.start();
        uploader.start();
        Log.i(TAG, "start succeed");

        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(sliceRunnable, Supporter.SLICE_SECOND * 1000);
        return true;
    }

    @Override
    public boolean isRecording() {
        return isRunning;
    }

    Runnable sliceRunnable = new Runnable() {
        @Override
        public void run() {
            filer.nextSlice();
            handler.postDelayed(sliceRunnable,Supporter.SLICE_SECOND * 1000);
        }
    };

    @Override
    public VoiceRecordResult stop() {
        if (!isRunning)
            return null;

        isRunning = false;
        handler.removeCallbacksAndMessages(null);

        //new slice
        recorder.setRecordCallback(null);
        recorder.stop();
        codec.stop();
        return filer.stop();
    }

    public void cancelRecording() {
        stop();
    }

    public void setRecordCallback(IRecordCallback callback) {
        recorder.setRecordCallback(callback);
    }

    private void init() {
        recorder = new Recorder();
        codec = new Codec();
        filer = new Filer();
        uploader = new Uploader();

        recorder.setPcmConsumer(codec);
        codec.setAmrConsumer(filer);
        filer.setFileConsumer(uploader);
    }
}
