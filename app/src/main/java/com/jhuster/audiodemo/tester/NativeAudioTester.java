package com.jhuster.audiodemo.tester;

import android.os.SystemClock;

public class NativeAudioTester extends Tester {

    private boolean mIsTestCapture = true;

    public NativeAudioTester(boolean isTestCapture) {
        mIsTestCapture = isTestCapture;
    }

    @Override
    public boolean startTesting() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mIsTestCapture) {
                    nativeStartCapture();
                } else {
                    nativeStartPlayback();
                }
            }
        }).start();
        return false;
    }

    @Override
    public boolean stopTesting() {
        if (mIsTestCapture) {
            nativeStopCapture();
        } else {
            nativeStopPlayback();
        }
        return false;
    }

    private native boolean nativeStartCapture();

    private native boolean nativeStopCapture();

    private native boolean nativeStartPlayback();

    private native boolean nativeStopPlayback();

    static {
        System.loadLibrary("native_audio");
    }
}
