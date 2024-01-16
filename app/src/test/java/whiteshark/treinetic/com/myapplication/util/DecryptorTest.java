package whiteshark.treinetic.com.myapplication.util;

import com.treinetic.google.androidx.Decryptor;

public class DecryptorTest {



    public void test(){
        Decryptor decryptor =new Decryptor("","");
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
