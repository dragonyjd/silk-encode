package com.baice.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baice.app.sound.IRecordCallback;
import com.baice.app.sound.SilkFileDecoder;
import com.baice.app.sound.SoundMan;
import com.baice.app.sound.VoiceRecordResult;
import com.baice.bc_im_silk.SilkDecoder;

public class MainActivity extends AppCompatActivity {

    private Button mStartBtn;
    private Button mEndBtn;
    private Button mPlayBtn;
    private TextView mLabel;

    private SilkFileDecoder mPlayerDecoder;
    private String mLastSound = "/sdcard/Android/data/com.baice.app/cache/record/52f4e2e3-1574-48a7-807a-29637d00d4f5";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStartBtn = findViewById(R.id.start_btn);
        mEndBtn = findViewById(R.id.end_btn);
        mPlayBtn = findViewById(R.id.play_btn);
        mLabel = findViewById(R.id.label);

        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
                } else {
                    if (!SoundMan.getInstance().isRecording()) {
                        SoundMan.getInstance().start();
                        SoundMan.getInstance().setRecordCallback(new IRecordCallback() {
                            @Override
                            public void onVolumeSize(final int size) {
                                MainActivity.this.runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        mLabel.setText(String.valueOf(size));
                                    }
                                });
                            }
                        });
                    }
                }
            }
        });

        mEndBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SoundMan.getInstance().isRecording()) {
                    VoiceRecordResult result = SoundMan.getInstance().stop();
                    if (result != null) {
                        mLastSound = result.fileName;
                    }
                }
            }
        });

        mPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(mLastSound)) {
                    if (mPlayerDecoder == null) {
                        mPlayerDecoder = new SilkFileDecoder();
                    } else {
                        mPlayerDecoder.stop();
                    }
                    try {
                        mPlayerDecoder.start(mLastSound);     //fis会在中Decoder完后释放
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
