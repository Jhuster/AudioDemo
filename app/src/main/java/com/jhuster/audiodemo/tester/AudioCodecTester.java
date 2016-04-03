package com.jhuster.audiodemo.tester;

import com.jhuster.audiodemo.api.AudioCapturer;
import com.jhuster.audiodemo.api.AudioDecoder;
import com.jhuster.audiodemo.api.AudioEncoder;

public class AudioCodecTester extends Tester implements AudioCapturer.OnAudioFrameCapturedListener,
        AudioEncoder.OnAudioEncodedListener, AudioDecoder.OnAudioDecodedListener {

    private AudioEncoder mAudioEncoder;
    private AudioDecoder mAudioDecoder;
    private AudioCapturer mAudioCapturer;
    private volatile boolean mIsTestingExit = false;

    @Override
    public boolean startTesting() {
        mAudioCapturer = new AudioCapturer();
        mAudioEncoder = new AudioEncoder();
        mAudioDecoder = new AudioDecoder();
        if (!mAudioEncoder.open() || !mAudioDecoder.open()) {
            return false;
        }
        mAudioEncoder.setAudioEncodedListener(this);
        mAudioDecoder.setAudioDecodedListener(this);
        mAudioCapturer.setOnAudioFrameCapturedListener(this);
        new Thread(mEncodeRenderRunable).start();
        new Thread(mDecodeRenderRunable).start();
        if (!mAudioCapturer.startCapture()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean stopTesting() {
        mIsTestingExit = true;
        mAudioCapturer.stopCapture();
        return true;
    }

    @Override
    public void onAudioFrameCaptured(byte[] audioData) {
        long presentationTimeUs = (System.nanoTime()) / 1000L;
        mAudioEncoder.encode(audioData, presentationTimeUs);
    }

    @Override
    public void onFrameEncoded(byte[] encoded, long presentationTimeUs) {
        mAudioDecoder.decode(encoded, presentationTimeUs);
    }

    private Runnable mEncodeRenderRunable = new Runnable() {
        @Override
        public void run() {
            while (!mIsTestingExit) {
                mAudioEncoder.render();
            }
            mAudioEncoder.close();
        }
    };

    private Runnable mDecodeRenderRunable = new Runnable() {
        @Override
        public void run() {
            while (!mIsTestingExit) {
                mAudioDecoder.render();
            }
            mAudioDecoder.close();
        }
    };

    @Override
    public void onFrameDecoded(byte[] decoded, long presentationTimeUs) {

    }
}
