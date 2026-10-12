package com.weapons18.api;

public final class v {
    private static W18JniCallbackListener callbackListener;

    private v() {
    }

    public interface W18JniCallbackListener {
        int onCallback(int type, byte[] data1, int len1, byte[] data2, int len2);
    }

    public static void setJniCallbackListener(W18JniCallbackListener listener) {
        callbackListener = listener;
    }

    public static int W18CallbackFunc(int type, byte[] data1, int len1, byte[] data2, int len2) {
        W18JniCallbackListener listener = callbackListener;
        if (listener == null) {
            return 0;
        }
        return listener.onCallback(type, data1, len1, data2, len2);
    }

    public static void gsr(long value) {
    }

    public static void svi(int width, int height) {
    }

    public static void svr() {
    }

    public static void yt(int value) {
    }

    public static native int cd(String uid, String password);

    public static native int dd(int handle);

    public static native int dt();

    public static native void gd(int handle);

    public static native int gn(int handle, byte[] output);

    public static native String it(String applicationId, int mode);

    public static native int pd(int handle, String path);

    public static native int sa(int handle);

    public static native int sac(int handle, int command, byte[] data, int length);

    public static native int sam(int handle);

    public static native int sbm(int handle);

    public static native int sda(int handle, byte[] video, int videoLen, byte[] audio, int audioLen, byte[] extra);

    public static native int sed();

    public static native int sp(int handle);

    public static native int spb(int handle, byte[] data);

    public static native int spbm(int handle);

    public static native int spm(int handle);

    public static native int ss(int handle);

    public static native int ssm(int handle);

    public static native int st(Object surface);

    public static native int sv(byte[] video, int videoLen, byte[] extra, int extraLen, byte[] out);

    public static native void vii(long nativeView, int width, int height);

    public static native void vr(long nativeView);
}
