package com.jhuster.audiodemo.tester;

import com.jhuster.audiodemo.api.audio.AudioCapturer;
import com.jhuster.audiodemo.api.audio.AudioDecoder;
import com.jhuster.audiodemo.api.audio.AudioEncoder;
import com.jhuster.audiodemo.api.audio.AudioPlayer;

public class AudioCodecTester extends Tester implements AudioCapturer.OnAudioFrameCapturedListener,
        AudioEncoder.OnAudioEncodedListener, AudioDecoder.OnAudioDecodedListener {

    private AudioEncoder mAudioEncoder;
    private AudioDecoder mAudioDecoder;
    private AudioCapturer mAudioCapturer;
    private AudioPlayer mAudioPlayer;
    private volatile boolean mIsTestingExit = false;

    @Override
    public boolean startTesting() {
        mAudioCapturer = new AudioCapturer();
        mAudioPlayer = new AudioPlayer();
        mAudioEncoder = new AudioEncoder();
        mAudioDecoder = new AudioDecoder();
        if (!mAudioEncoder.open() || !mAudioDecoder.open()) {
            return false;
        }
        mAudioEncoder.setAudioEncodedListener(this);
        mAudioDecoder.setAudioDecodedListener(this);
        mAudioCapturer.setOnAudioFrameCapturedListener(this);
        new Thread(mEncodeRenderRunnable).start();
        new Thread(mDecodeRenderRunnable).start();
        if (!mAudioCapturer.startCapture()) {
            return false;
        }
        mAudioPlayer.startPlayer();
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

    private Runnable mEncodeRenderRunnable = new Runnable() {
        @Override
        public void run() {
            while (!mIsTestingExit) {
                mAudioEncoder.retrieve();
            }
            mAudioEncoder.close();
        }
    };

    private Runnable mDecodeRenderRunnable = new Runnable() {
        @Override
        public void run() {
            while (!mIsTestingExit) {
                mAudioDecoder.retrieve();
            }
            mAudioDecoder.close();
        }
    };

    @Override
    public void onFrameDecoded(byte[] decoded, long presentationTimeUs) {
        mAudioPlayer.play(decoded, 0, decoded.length);
    }
}
