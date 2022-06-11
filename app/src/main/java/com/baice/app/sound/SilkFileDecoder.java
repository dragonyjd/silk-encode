package com.baice.app.sound;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.baice.bc_im_silk.SilkDecoder;

public class SilkFileDecoder implements Runnable, SilkDecoder.IDecoderListener {

    static final int SAMPLE_RATE = 16000;

    private Thread mDecodeThread;
    private AudioTrack mAudioTrack;
    int playerBufferSize = 0;
    private String mFilePath;
    boolean isRunning = false;

    public void start(String filepath) {
        if (isRunning) {
            return;
        }

        isRunning = true;
        if (mAudioTrack == null) {
            playerBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    playerBufferSize, AudioTrack.MODE_STREAM);
        }
        mAudioTrack.play();

        SilkDecoder.setDecodeCallback(this);
        mFilePath = filepath;
        mDecodeThread = new Thread(this);
        mDecodeThread.start();
    }

    public void stop() {
        if (!isRunning)
            return;

        isRunning = false;
        mAudioTrack.stop();
        SilkDecoder.setDecodeCallback(null);
        SilkDecoder.reset();
    }

    @Override
    public void decode_callback(short[] outBuf, int len) {
        if (isRunning && mAudioTrack != null && outBuf != null && len > 0) {
            // 播放
            mAudioTrack.write(outBuf, 0, len);
        }
    }

    @Override
    public void run() {
        try {
            SilkDecoder.decode(mFilePath);
            stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
