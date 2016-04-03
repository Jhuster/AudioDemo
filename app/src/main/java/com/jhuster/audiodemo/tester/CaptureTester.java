package com.jhuster.audiodemo.tester;

import android.os.Environment;

import com.jhuster.audiodemo.api.AudioCapturer;
import com.jhuster.audiodemo.api.WavFileWriter;

import java.io.IOException;

public class CaptureTester extends Tester implements AudioCapturer.OnAudioFrameCapturedListener {

    private static final String DEFAULT_TEST_FILE = Environment.getExternalStorageDirectory() + "/test.wav";

    private AudioCapturer mAudioCapturer;
    private WavFileWriter mWavFileWriter;

    @Override
    public boolean startTesting() {
        mAudioCapturer = new AudioCapturer();
        mWavFileWriter = new WavFileWriter();
        try {
            mWavFileWriter.openFile(DEFAULT_TEST_FILE, 44100, 16, 2);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        mAudioCapturer.setOnAudioFrameCapturedListener(this);
        return mAudioCapturer.startCapture();
    }

    @Override
    public boolean stopTesting() {
        mAudioCapturer.stopCapture();
        try {
            mWavFileWriter.closeFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void onAudioFrameCaptured(byte[] audioData) {
        mWavFileWriter.writeData(audioData, 0, audioData.length);
    }
}
