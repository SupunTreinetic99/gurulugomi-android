package com.treinetic.google.androidx;

import androidx.annotation.Keep;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;

//import com.crashlytics.android.Crashlytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.treinetic.whiteshark.util.extentions.FileUtilKt;
@Keep
public class Decryptor {

    private String source_file;
    private int swapKey;
    public byte[] data;


    public Decryptor(String file_path, String key) {
        this.source_file = file_path;
        this.swapKey = this.createNumbericSwapKey(key);
        System.out.println("Number " + swapKey);
        FirebaseCrashlytics.getInstance().log("swapKey:"+swapKey+" path:"+file_path);
    }


    public Decryptor decrypt() throws Exception {
        File file_input = new File(source_file);
        long secondHalf = (long) (Math.floor(file_input.length() - swapKey) / 2);
        long firstHalf = file_input.length() - secondHalf;

        FileInputStream stream = new FileInputStream(file_input);
        byte[] before_swap_key = readNBytes(swapKey, stream);
        stream.skip(swapKey);
        byte[] after_junk = readNBytes((int) (firstHalf - 2 * swapKey), stream);

        byte[] start = concat(before_swap_key, after_junk);
        byte[] end = readNBytes((int) secondHalf, stream);
        data = concat(end, start);
        return this;
    }


    public Decryptor encrypt() throws Exception {
        File file_input = new File(source_file);
        long firstHalf = (long) Math.floor(file_input.length() / 2);
        long secondHalf = file_input.length() - firstHalf;

        FileInputStream stream = new FileInputStream(file_input);
        byte[] start = readNBytes((int) firstHalf, stream);

        byte[] end_1 = readNBytes(swapKey, stream);
        byte[] end_2 = readNBytes((int) (secondHalf - swapKey), stream);
        byte[] junk = generateRandomString(swapKey);

        byte[] end = concat(end_1, concat(junk, end_2));
        data = concat(end, start);
        return this;
    }

    public void save(String file_path) throws Exception {
        File f = new File(file_path);
        if (!f.exists()) f.createNewFile();
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(data);
        fos.flush();
        fos.close();
    }

    byte[] readNBytes(int lenth, InputStream in) throws Exception {
        byte[] b = new byte[lenth];
        in.read(b);
        return b;
    }

    private byte[] concat(byte[] a, byte[] b) {
        int lenA = a.length;
        int lenB = b.length;
        byte[] c = Arrays.copyOf(a, lenA + lenB);
        System.arraycopy(b, 0, c, lenA, lenB);
        return c;
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
}



