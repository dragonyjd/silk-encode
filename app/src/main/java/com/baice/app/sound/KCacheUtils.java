package com.baice.app.sound;

import android.content.pm.PackageManager;
import android.os.Environment;


import com.baice.app.MyApplication;

import java.io.File;

import static android.os.Environment.MEDIA_MOUNTED;

public class KCacheUtils {
    private static final String EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";

    public static String getCacheDirectory() {
        String cachedDir = null;
        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && hasExternalStoragePermission()) {
            cachedDir = getExternalCacheDir().getAbsolutePath();
        }
        if (cachedDir == null) {
            cachedDir = MyApplication.getContext().getCacheDir().getAbsolutePath();
        }
        if (cachedDir == null) {
            cachedDir = MyApplication.getContext().getFilesDir().getParentFile().getPath()
                    + MyApplication.getContext().getPackageName() + "/cache";
        }
        return cachedDir;
    }

    private static File getExternalCacheDir() {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, MyApplication.getContext().getPackageName()), "cache");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                return null;
            }
        }
        return appCacheDir;
    }

    private static boolean hasExternalStoragePermission() {
        int perm = MyApplication.getContext().checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION);
        return perm == PackageManager.PERMISSION_GRANTED;
    }
}
