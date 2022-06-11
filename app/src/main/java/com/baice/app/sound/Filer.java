package com.baice.app.sound;



import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;


/**
 * Created by changbinhe on 14/11/22.
 */
public class Filer implements Runnable, Supporter.AmrConsumer, Supporter.OnOffSwitcher {
    private static final String TAG = "Filer";
    final private static byte[] header = new byte[]{0x23, 0x21, 0x53, 0x49, 0x4c, 0x4b, 0x5f, 0x56, 0x33};
    final private static byte[] ender = new byte[]{-1, -1};

    private List<byte[]> amrFrames;

    private DataOutputStream sliceStream;
    private File sliceFile;

    private Supporter.FileConsumer fileConsumer;

    private boolean isRunning;
    private Thread runningThread;
    final private Object waitingObject;
    private VoiceRecordResult recordResult = null;

    public Filer() {
        amrFrames = Collections.synchronizedList(new LinkedList<byte[]>());
        waitingObject = new Object();
    }

    @Override
    public void onAmrFeed(byte[] buffer, int length) {
        byte[] tempData = new byte[length];
        System.arraycopy(buffer, 0, tempData, 0, length);

        amrFrames.add(tempData);

        Log.i(TAG, "add one amr frame, try to notify");

        synchronized (waitingObject) {
            waitingObject.notify();
        }
    }

    @Override
    public void setAmrFinish(double record_length) {
        if (recordResult != null) {
            recordResult.recordLength = record_length;
        }
    }

    public void nextSlice() {
        Log.i(TAG, "next slice");
        makeSlice();
    }

    synchronized private void makeSlice() {
        //flush
        if (sliceStream != null) {
            try {
                sliceStream.flush();
                //send to file consumer
                fileConsumer.onFileFeed(sliceFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File folder = new File(KCacheUtils.getCacheDirectory() + "/record");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(folder.getAbsolutePath(), UUID.randomUUID().toString());
        if (file.exists())
            file.delete();
        try {
            file.createNewFile();
            recordResult = new VoiceRecordResult();
            recordResult.fileName = file.getAbsolutePath();
            recordResult.recordLength = 0;
            Log.i(TAG, "new slice file at:" + file.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        }

        DataOutputStream dos = null;
        try {
            OutputStream os = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            dos = new DataOutputStream(bos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (file.exists() && dos != null) {
            sliceFile = file;
            sliceStream = dos;

            try {
                sliceStream.write(header);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.i(TAG, "file create succeed, try to notify");
            synchronized (waitingObject) {
                waitingObject.notify();
            }
            return;
        }

        Log.i(TAG, "file create failed");
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

        if (sliceStream == null)
            makeSlice();

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
        Log.i(TAG, "stop, clean up");
        if (!isRunning) {
            Log.i(TAG, "not running");
            return null;
        }

        isRunning = false;
        runningThread.interrupt();
        runningThread = null;

        //finish all writing
        if (sliceStream != null) {
            while (amrFrames.size() > 0) {
                byte[] buffer = amrFrames.remove(0);
                try {
                    //
                    Log.i(TAG, "clean up write");
                    // Write payload length
                    sliceStream.write(convertLength(buffer.length), 0, 2);
                    // Write payload
                    sliceStream.write(buffer, 0, buffer.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                /* Write file end */
                sliceStream.write(ender);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                sliceStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        sliceStream = null;
        sliceFile = null;
        return recordResult;
    }

    @Override
    public void run() {
        while (isRunning) {
            synchronized (waitingObject) {
                if (amrFrames.size() == 0 || sliceStream == null) {
                    Log.i(TAG, "waiting :" + amrFrames.size() + sliceStream);
                    try {
                        waitingObject.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    //try to write
                    byte[] buffer = amrFrames.get(0);
                    try {
                        Log.i(TAG, "writing :" + buffer.length);
                        // Write payload length
                        sliceStream.write(convertLength(buffer.length), 0, 2);
                        // Write payload
                        sliceStream.write(buffer, 0, buffer.length);
                        amrFrames.remove(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void setFileConsumer(Supporter.FileConsumer fileConsumer) {
        this.fileConsumer = fileConsumer;
    }

    public byte[] convertLength(int length)
    {
        byte[] bytes = new byte[2];
        bytes[1]=(byte) (length>>8);
        bytes[0]=(byte) (length);

        return bytes;
    }
}
