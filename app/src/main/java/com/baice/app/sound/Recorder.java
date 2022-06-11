package com.baice.app.sound;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;


/**
 * Created by changbinhe on 14/11/22.
 */
public class Recorder implements Runnable, Supporter.OnOffSwitcher {

    private static String TAG = "AudioRecorder";

    //8 k * 16bit * 1 = 8k shorts
    static final int SAMPLE_RATE = 16000;
    //20 ms second
    //0.02 x 8000 x 2 = 320;160 short
    static final int FRAME_SIZE = 320;

    private AudioRecord audioRecord;

    private short[] audioBuffer;

    private Thread runningThread;

    boolean isRecording;

    private IRecordCallback mCallback;
    public void setRecordCallback(IRecordCallback callback) {
        mCallback = callback;
    }

    Supporter.PcmConsumer pcmConsumer;

    public void setPcmConsumer(Supporter.PcmConsumer pcmConsumer) {
        this.pcmConsumer = pcmConsumer;
    }

    @Override
    public boolean start() {
        if (isRecording) {
            Log.e(TAG, "is recoding");
            return false;
        }
        int bufferSizeInByte = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSizeInByte);

        audioBuffer = new short[bufferSizeInByte / 2];

        audioRecord.startRecording();

        isRecording = true;

        //start
        runningThread = new Thread(this);
        runningThread.start();
        return isRecording;
    }

    @Override
    public boolean isRecording() {
        return isRecording;
    }

    @Override
    public VoiceRecordResult stop() {
        if (!isRecording)
            return null;

        isRecording = false;
        runningThread.interrupt();
        runningThread = null;
        audioRecord.release();
        return null;
    }

    @Override
    public void run() {
        while (isRecording) {
            int read = audioRecord.read(audioBuffer, 0, FRAME_SIZE);
            if (read == AudioRecord.ERROR_INVALID_OPERATION || read == AudioRecord.ERROR_BAD_VALUE) {
                Log.i(TAG, "error:" + read);
                continue;
            }

            if (mCallback != null) {
                mCallback.onVolumeSize((int)calculateVolume(audioBuffer, read));
            }

            pcmConsumer.onPcmFeed(audioBuffer, read);
        }
    }

    private double calculateVolume(short[] buffer, int len) {
        double sumVolume = 0.0;
        double avgVolume = 0.0;
        double volume = 0.0;
        for(short b : buffer){
            sumVolume += Math.abs(b);
        }
        avgVolume = sumVolume / buffer.length;
        volume = Math.log10(1 + avgVolume) * 10;
        return volume;

        // 获取音量分贝值
//        long v = 0;
//        // 将 buffer 内容取出，进行平方和运算
//        for (int i = 0; i < buffer.length; i++) {
//            v += buffer[i] * buffer[i];
//        }
//        // 平方和除以数据总长度，得到音量大小。
//        double mean = v / (double) len;
//        double volume = 10 * Math.log10(mean);
//        return volume;
    }
}
