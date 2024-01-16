package com.treinetic.whiteshark.services;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.treinetic.whiteshark.BuildConfig;
import com.treinetic.whiteshark.MyApp;
import com.treinetic.whiteshark.models.User;


public class LocalStorageService {
    private static final String TAG = "LocalStorageService";

    private static LocalStorageService instance;
    private static String PACKAGE = BuildConfig.APPLICATION_ID;

    public static LocalStorageService getInstance() {
        if (instance == null) instance = new LocalStorageService();
        return instance;
    }

    public void saveToken(String token) {

        SharedPreferences sharedPreferences = MyApp.getAppContext()
                .getSharedPreferences(PACKAGE,
                        Context.MODE_PRIVATE);
        SharedPreferences.Editor er = sharedPreferences.edit();
        er.putString("TOKEN", token);

        er.commit();
    }

    public String getToken() {
        SharedPreferences sharedPreferences = MyApp
                .getAppContext().getSharedPreferences(
                        PACKAGE, Context.MODE_PRIVATE);

        return sharedPreferences.getString("TOKEN", null);
    }


    public void saveCurrentUser(User user) {
        SharedPreferences sharedPreferences = MyApp.getAppContext()
                .getSharedPreferences(PACKAGE,
                        Context.MODE_PRIVATE);
        SharedPreferences.Editor er = sharedPreferences.edit();
        String userData = new Gson().toJson(user);
        er.putString("USER", userData);

        er.commit();
    }

    public String getCurrentUser() {
        SharedPreferences sharedPreferences = MyApp
                .getAppContext().getSharedPreferences(
                        PACKAGE, Context.MODE_PRIVATE);

        return sharedPreferences.getString("USER", null);


    }

    public void removeCurrentUser() {
        try {
            SharedPreferences sharedPreferences = MyApp
                    .getAppContext().getSharedPreferences(
                            PACKAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor er = sharedPreferences.edit();
            er.remove("USER");
            er.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void savePrivateKey(String key) {
        SharedPreferences sharedPreferences = MyApp.getAppContext()
                .getSharedPreferences(PACKAGE,
                        Context.MODE_PRIVATE);
        SharedPreferences.Editor er = sharedPreferences.edit();
        er.putString("PRIVATE_KEY", key);

        er.commit();
    }

    public String getPrivateKey() {
        SharedPreferences sharedPreferences = MyApp
                .getAppContext().getSharedPreferences(
                        PACKAGE, Context.MODE_PRIVATE);

        return sharedPreferences.getString("PRIVATE_KEY", null);


    }


    public void saveReadLocation(String bookId, String locatorJson) {

        SharedPreferences sharedPreferences = MyApp.getAppContext()
                .getSharedPreferences(PACKAGE,
                        Context.MODE_PRIVATE);
        SharedPreferences.Editor er = sharedPreferences.edit();
        er.putString(bookId, locatorJson);
        er.commit();

    }

    public String getSavedReadLocation(String bookId) {

        SharedPreferences sharedPreferences = MyApp
                .getAppContext().getSharedPreferences(
                        PACKAGE, Context.MODE_PRIVATE);

        return sharedPreferences.getString(bookId, null);

    }


}
