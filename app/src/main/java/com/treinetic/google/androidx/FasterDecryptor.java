package com.treinetic.google.androidx;

import androidx.annotation.Keep;

import java.io.*;
import java.util.Random;
@Keep
public class FasterDecryptor {

    private String source_file;
    private int swapKey;
    private byte[] data;


    public FasterDecryptor(String file_path, String key) {
        this.source_file = file_path;
        this.swapKey = this.createNumbericSwapKey(key);
    }


    public FasterDecryptor decrypt(String file_path) throws Exception {
        File input = new File(this.source_file);
        long second_half = (input.length() - this.swapKey) / 2;
        long first_half = input.length() - second_half;
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(file_path)));
        RandomAccessFile rf = new RandomAccessFile(this.source_file, "r");

        seekAndwriteTillEnd(rf, first_half, out);
        writeChunkFromIndex(rf, 0, swapKey, out);
        rf.skipBytes(swapKey * 2);
        write(rf, first_half - (swapKey * 2), out);

        rf.close();
        out.flush();
        out.close();
        return this;
    }


    public FasterDecryptor encrypt(String file_path) throws Exception {
        File input = new File(this.source_file);
        long first_half = (input.length()) / 2;
        long second_half = input.length() - first_half;

        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(file_path)));
        RandomAccessFile rf = new RandomAccessFile(this.source_file, "r");

        writeChunkFromIndex(rf, first_half, swapKey, out);
        out.write(generateRandomString(swapKey));
        write(rf, second_half - swapKey, out);
        write(rf, 0, first_half, out);

        rf.close();
        out.close();
        return this;
    }

    private byte[] generateRandomString(int length) {
        byte[] array = new byte[length];
        new Random().nextBytes(array);
        return array;
    }

    private int createNumbericSwapKey(String key) {
        int counter = 0;
        for (char ch : key.toCharArray()) {
            if (Character.isDigit(ch)) {
                ++counter;
            }
        }
        return counter;
    }

    private void write(RandomAccessFile rf, long from, long to, OutputStream out) throws Exception {
        if (from >= 0) {
            rf.seek(from);
        }
        int length = 0;
        byte[] buffer = new byte[1024];
        int x1024Count = 1;
        long until = to / 1024;
        while ((length = rf.read(buffer)) > 0) {
            if (x1024Count <= until) {
                out.write(buffer, 0, length);
                ++x1024Count;
                continue;
            }
            break;
        }
        until = to % 1024;
        if (until > 0) {
            byte[] br = new byte[(int) until];
            rf.read(br);
            out.write(br);
        }
    }

    private void write(RandomAccessFile rf, long to, OutputStream out) throws Exception {
        this.write(rf, -1, to, out);
    }

    private void seekAndwriteTillEnd(RandomAccessFile rf, long index, OutputStream out) throws Exception {
        int length = 0;
        rf.seek(index);
        byte[] buffer = new byte[1024];
        while ((length = rf.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
    }

    /*
     * only for small chunks
     * take "length" of bytes from the index of "rf" and then writes to "out"
     * */
    private void writeChunkFromIndex(RandomAccessFile rf, long index, int length, OutputStream out) throws Exception {
        if (index >= 0) {
            rf.seek(index);
        }
        byte[] sk = new byte[length];
        rf.read(sk);
        out.write(sk);
    }

    private int find(byte[] mainarr, byte[] needle) {
        int i = 0;
        int index = -1;
        for (byte b : mainarr) {
            if (b == needle[0]) {
                index = i;
                for (int j = 0; j < needle.length; ++j) {
                    if (mainarr[j + i] != needle[j]) {
                        index = -1;
                        break;
                    }
                }
                if (index != -1) {
                    break;
                }
            }
            ++i;
        }
        return index;
    }
}



