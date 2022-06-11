package com.baice.app.sound;


import android.util.Log;

import com.baice.bc_im_silk.SilkEncoder;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 * Created by changbinhe on 14/11/22.
 */
public class Codec implements Runnable, Supporter.PcmConsumer, Supporter.OnOffSwitcher {
    private static final String TAG = "Codec";

    private List<short[]> pcmFrames;
    private boolean isRunning;

    private Supporter.AmrConsumer amrConsumer;
    private Thread runningThread;
    final private Object waitingObject;
    final int MAX_BYTES_PER_FRAME   = 250; // Equals peak bitrate of 100 kbps
    final int MAX_INPUT_FRAMES      = 5;

    public Codec() {
        pcmFrames = Collections.synchronizedList(new LinkedList<short[]>());
        waitingObject = new Object();
    }

    @Override
    public void onPcmFeed(short[] buffer, int length) {
        //would crash if not 160
        if (length != Recorder.FRAME_SIZE)
            return;
        short[] tempArray = new short[length];
        System.arraycopy(buffer, 0, tempArray, 0, length);

        Log.i(TAG, "onPcmFeed :" + length);
        pcmFrames.add(tempArray);

        Log.i(TAG, "onPcmFeed pcmFrames :" + pcmFrames.size());
        synchronized (waitingObject) {
            waitingObject.notify();
        }
    }

    public void setAmrConsumer(Supporter.AmrConsumer amrConsumer) {
        this.amrConsumer = amrConsumer;
    }

    @Override
    public boolean start() {
        Log.i(TAG, "try to start");
        if (isRunning) {

            Log.i(TAG, "already started");
            return false;
        }
        Log.i(TAG, "start succeed");
        isRunning = true;

        //AmrEncoder.init(0);
        SilkEncoder.init();

        //start
        runningThread = new Thread(this);
        runningThread.start();
        return true;
    }

    @Override
    public boolean isRecording() {
        return isRunning;
    }

    @Override
    public VoiceRecordResult stop() {
        Log.i(TAG, "stop clean up");
        if (!isRunning) {
            Log.i(TAG, "not running");
            return null;
        }

        isRunning = false;

        //todo need sync?
        //finish all
        while (pcmFrames.size() > 0) {
            short[] buffer = pcmFrames.remove(0);
            byte[] encodedData = new byte[MAX_BYTES_PER_FRAME * MAX_INPUT_FRAMES];
            //int encodedLength = AmrEncoder.encode(AmrEncoder.Mode.MR122.ordinal(), buffer, encodedData);
            int encodedLength = SilkEncoder.encode(buffer, encodedData, encodedData.length);

            Log.i(TAG, "clean up encode: length" + encodedLength);
            if (encodedLength > 0) {
                amrConsumer.onAmrFeed(encodedData, encodedLength);
            }
        }

        amrConsumer.setAmrFinish(SilkEncoder.finish());
        return null;
    }

    @Override
    public void run() {
        while (isRunning) {
            synchronized (waitingObject) {
                if (pcmFrames.size() == 0) {
                    try {
                        Log.i(TAG, "wait: " + pcmFrames.size());
                        waitingObject.wait();
                    } catch (InterruptedException e) {
                        Log.e(TAG, "==InterruptedException==");
                        e.printStackTrace();
                    }
                } else {
                    short[] buffer = pcmFrames.remove(0);
                    byte[] encodedData = new byte[MAX_BYTES_PER_FRAME * MAX_INPUT_FRAMES];
                    //int encodedLength = AmrEncoder.encode(AmrEncoder.Mode.MR122.ordinal(), buffer, encodedData);
                    int encodedLength = SilkEncoder.encode(buffer, encodedData, encodedData.length);
                    //Log.i(TAG, "encode: length" + encodedLength);
                    if (encodedLength > 0) {
                        amrConsumer.onAmrFeed(encodedData, encodedLength);
                    }
                }
            }
        }
    }
}
