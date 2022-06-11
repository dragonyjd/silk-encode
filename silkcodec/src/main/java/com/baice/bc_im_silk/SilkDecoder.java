package com.baice.bc_im_silk;

import java.lang.ref.WeakReference;

/**
 * 支持边录边解，边解边录=
 *
 * @author guoxiaolong
 * @date 2019-09-23
 */
public class SilkDecoder {
    static {
        System.loadLibrary("silkcodec");
    }

    public interface IDecoderListener {
        void decode_callback(short[] buf,int len);
    }

    private static WeakReference<IDecoderListener> decodeCallback = null;
    public static void setDecodeCallback(IDecoderListener decodeCallback) {
        SilkDecoder.decodeCallback = new WeakReference<>(decodeCallback);
    }

    public static native int decode(String path);

    public static native void reset();

    public static native double getRecordLength(String path);

    public static void decode_callback(short[] buf,int len) {
        if (decodeCallback != null && decodeCallback.get() != null) {
            decodeCallback.get().decode_callback(buf, len);
        }
    }
}
