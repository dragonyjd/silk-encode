package com.baice.bc_im_silk;

/**
 * 支持边录边解，边解边录=
 *
 * @author guoxiaolong
 * @date 2019-09-20
 */
public class SilkEncoder {
    static {
        System.loadLibrary("silkcodec");
    }

    public static native int init();

    public static native int encode(short[] in, byte[] out, int outLen);

    public static native double finish();
}
