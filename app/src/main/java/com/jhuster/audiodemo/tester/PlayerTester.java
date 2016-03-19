package com.jhuster.audiodemo.tester;

import android.os.Environment;

import com.jhuster.audiodemo.api.AudioPlayer;
import com.jhuster.audiodemo.api.WavFileReader;

import java.io.IOException;

public class PlayerTester extends Tester {

    private static final String DEFAULT_TEST_FILE = Environment.getExternalStorageDirectory() + "/test.wav";

    private AudioPlayer mAudioPlayer;
    private WavFileReader mWavFileReader;
    private volatile boolean mIsTestingExit = false;

    @Override
    public boolean startTesting() {

        mWavFileReader = new WavFileReader();
        mAudioPlayer = new AudioPlayer();

        try {
            mWavFileReader.openFile(DEFAULT_TEST_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mAudioPlayer.startPlayer();

        new Thread(AudioPlayRunnable).start();

        return true;
    }

    @Override
    public boolean stopTesting() {
        mIsTestingExit = true;
        return true;
    }

    private Runnable AudioPlayRunnable = new Runnable() {
        @Override
        public void run() {
            byte[] buffer = new byte[mAudioPlayer.getMinBufferSize()];
            while (!mIsTestingExit && mWavFileReader.readData(buffer, 0, buffer.length) > 0) {
                mAudioPlayer.play(buffer, 0, buffer.length);
            }
            mAudioPlayer.stopPlayer();
            try {
                mWavFileReader.closeFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
}
