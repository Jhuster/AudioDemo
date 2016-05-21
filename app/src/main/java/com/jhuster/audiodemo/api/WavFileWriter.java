/*
 *  COPYRIGHT NOTICE  
 *  Copyright (C) 2016, Jhuster <lujun.hust@gmail.com>
 *  https://github.com/Jhuster/AudioDemo
 *   
 *  @license under the Apache License, Version 2.0 
 *
 *  @file    WavFileWriter.java
 *  
 *  @version 1.0     
 *  @author  Jhuster
 *  @date    2016/03/19
 */
package com.jhuster.audiodemo.api;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WavFileWriter {

    private String mFilepath;
    private int mDataSize = 0;
    private DataOutputStream mDataOutputStream;

    public boolean openFile(String filepath, int sampleRateInHz, int bitsPerSample, int channels) throws IOException {
        if (mDataOutputStream != null) {
            closeFile();
        }
        mFilepath = filepath;
        mDataSize = 0;
        mDataOutputStream = new DataOutputStream(new FileOutputStream(filepath));
        return writeHeader(sampleRateInHz, bitsPerSample, channels);
    }

    public boolean closeFile() throws IOException {
        boolean ret = true;
        if (mDataOutputStream != null) {
            ret = writeDataSize();
            mDataOutputStream.close();
            mDataOutputStream = null;
        }
        return ret;
    }

    public boolean writeData(byte[] buffer, int offset, int count) {

        if (mDataOutputStream == null) {
            return false;
        }

        try {
            mDataOutputStream.write(buffer, offset, count);
            mDataSize += count;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean writeHeader(int sampleRateInHz, int bitsPerSample, int channels) {

        if (mDataOutputStream == null) {
            return false;
        }

        WavFileHeader header = new WavFileHeader(sampleRateInHz, bitsPerSample, channels);

        try {
            mDataOutputStream.writeBytes(header.mChunkID);
            mDataOutputStream.write(intToByteArray((int) header.mChunkSize), 0, 4);
            mDataOutputStream.writeBytes(header.mFormat);
            mDataOutputStream.writeBytes(header.mSubChunk1ID);
            mDataOutputStream.write(intToByteArray((int) header.mSubChunk1Size), 0, 4);
            mDataOutputStream.write(shortToByteArray((short) header.mAudioFormat), 0, 2);
            mDataOutputStream.write(shortToByteArray((short) header.mNumChannel), 0, 2);
            mDataOutputStream.write(intToByteArray((int) header.mSampleRate), 0, 4);
            mDataOutputStream.write(intToByteArray((int) header.mByteRate), 0, 4);
            mDataOutputStream.write(shortToByteArray((short) header.mBlockAlign), 0, 2);
            mDataOutputStream.write(shortToByteArray((short) header.mBitsPerSample), 0, 2);
            mDataOutputStream.writeBytes(header.mSubChunk2ID);
            mDataOutputStream.write(intToByteArray((int) header.mSubChunk2Size), 0, 4);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean writeDataSize() {

        if (mDataOutputStream == null) {
            return false;
        }

        try {
            RandomAccessFile wavFile = new RandomAccessFile(mFilepath, "rw");
            wavFile.seek(WavFileHeader.WAV_CHUNKSIZE_OFFSET);
            wavFile.write(intToByteArray((int) (mDataSize + WavFileHeader.WAV_CHUNKSIZE_EXCLUDE_DATA)), 0, 4);
            wavFile.seek(WavFileHeader.WAV_SUB_CHUNKSIZE2_OFFSET);
            wavFile.write(intToByteArray((int) (mDataSize)), 0, 4);
            wavFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static byte[] intToByteArray(int data) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(data).array();
    }

    private static byte[] shortToByteArray(short data) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(data).array();
    }
}
