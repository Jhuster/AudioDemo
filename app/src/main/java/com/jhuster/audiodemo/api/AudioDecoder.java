/*
 *  COPYRIGHT NOTICE
 *  Copyright (C) 2016, Jhuster <lujun.hust@gmail.com>
 *  https://github.com/Jhuster/AudioDemo
 *
 *  @license under the Apache License, Version 2.0
 *
 *  @file    AudioDecoder.java
 *
 *  @version 1.0
 *  @author  Jhuster
 *  @date    2016/04/02
 */
package com.jhuster.audiodemo.api;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class AudioDecoder {
    
    public static final String TAG = AudioDecoder.class.getSimpleName();

    public static final String DEFAULT_MIME_TYPE = "audio/mp4a-latm";
    public static final int DEFAULT_CHANNEL_NUM = 1;
    public static final int DEFAULT_SAMPLE_RATE = 44100;
    public static final int DEFAULT_MAX_BUFFER_SIZE = 16384;

    private MediaCodec mMediaCodec;
    private OnAudioDecodedListener mAudioDecodedListener;
    private boolean mIsOpened = false;
    private boolean mIsFirstFrame = true;
 
    public interface OnAudioDecodedListener {
        void onFrameDecoded(byte[] decoded, long presentationTimeUs);
    }
    
    public boolean open() {
        if (mIsOpened) {
            return true;
        }
        return open(DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_NUM, DEFAULT_MAX_BUFFER_SIZE);
    }

    public boolean open(int samplerate, int channels, int maxBufferSize) {

        if (mIsOpened) {
            return true;
        }

        try {
            mMediaCodec = MediaCodec.createDecoderByType(DEFAULT_MIME_TYPE);
            MediaFormat format = new MediaFormat();
            format.setString(MediaFormat.KEY_MIME, DEFAULT_MIME_TYPE);
            format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, channels);
            format.setInteger(MediaFormat.KEY_SAMPLE_RATE, samplerate);
            format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, maxBufferSize);
            mMediaCodec.configure(format, null, null, 0);
            mMediaCodec.start();
            mIsOpened = true;
            Log.d(TAG,"AudioDecoder opened !");
        } 
        catch (IOException e) {        
            e.printStackTrace();
            return false;
        }
        return true;
    }
 
    public void close() {
        if (!mIsOpened) {
            return;
        }
        mMediaCodec.stop();
        mMediaCodec.release();
        mMediaCodec = null;
        mIsOpened = false;
        Log.d(TAG,"AudioDecoder closed !");
    }
    
    public boolean isOpened() {
        return mIsOpened;
    }
    
    public void setAudioDecodedListener(OnAudioDecodedListener listener) {
        mAudioDecodedListener = listener;
    }
        
    public synchronized void decode(byte[] input, long presentationTimeUs) {
        
        if (!mIsOpened) {
            return;
        }
        
        try {  
            ByteBuffer[] inputBuffers  = mMediaCodec.getInputBuffers();
            int inputBufferIndex = mMediaCodec.dequeueInputBuffer(1000);
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                inputBuffer.put(input);
                if (mIsFirstFrame) {
                    /**
                     * Some formats, notably AAC audio and MPEG4, H.264 and H.265 video formats
                     * require the actual data to be prefixed by a number of buffers containing
                     * setup data, or codec specific data. When processing such compressed formats,
                     * this data must be submitted to the codec after start() and before any frame data.
                     * Such data must be marked using the flag BUFFER_FLAG_CODEC_CONFIG in a call to queueInputBuffer.
                     */
                    mMediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, presentationTimeUs, MediaCodec.BUFFER_FLAG_CODEC_CONFIG);
                    mIsFirstFrame = false;
                }
                else {
                    mMediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, presentationTimeUs, 0);
                }
            }
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public synchronized void render() {

        if (!mIsOpened) {
            return;
        }

        try {
            ByteBuffer[] outputBuffers = mMediaCodec.getOutputBuffers();
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 1000);
            if (outputBufferIndex >= 0) {
                Log.d(TAG,"onFrameDecoded " + bufferInfo.size);
                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                byte[] outData = new byte[bufferInfo.size];
                outputBuffer.get(outData);
                if (mAudioDecodedListener != null) {
                    mAudioDecodedListener.onFrameDecoded(outData, bufferInfo.presentationTimeUs);
                }
                mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            }
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private MediaFormat createDecoderFormat(String mimetype, int samplerate, int channels, int profile) {

        MediaFormat format = new MediaFormat();

        format.setString(MediaFormat.KEY_MIME, mimetype);
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, samplerate);
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, channels);

        int samplerates[] = {
                96000, 88200, 64000, 48000, 44100, 32000, 24000, 22050,
                16000, 12000, 11025, 8000
        };

        int sampleIndex = -1;
        for(int i = 0; i < samplerates.length; ++i) {
            if (samplerates[i] == samplerate) {
                sampleIndex = i;
                break;
            }
        }

        if(sampleIndex == -1) {
            return null;
        }

        ByteBuffer csd = ByteBuffer.allocate(2);
        csd.put((byte) ((profile << 3) | (sampleIndex >> 1)));
        csd.position(1);
        csd.put((byte) ((byte) ((sampleIndex << 7) & 0x80) | (channels << 3)));
        csd.flip();
        format.setByteBuffer("csd-0", csd);

        return format;
    }
}
