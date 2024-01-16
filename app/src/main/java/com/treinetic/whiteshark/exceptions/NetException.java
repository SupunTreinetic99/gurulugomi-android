package com.treinetic.whiteshark.exceptions;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.treinetic.whiteshark.network.Net;

import okhttp3.Response;


/**
 * Created by Nuwan.
 */

public class NetException extends Exception {

    @SerializedName("message")
    private String message;
    @SerializedName("message_id")
    private String message_id;
    @SerializedName("code")
    private int code;
    @SerializedName("maintain")
    private boolean maintain = false;
    private Response response;
    private Net.TryAgain tryAgain;

    public NetException(String message, String message_id, int code) {
        this.message = message;
        this.message_id = message_id;
        this.code = code;
    }

    public static NetException jsonParseError(String message) {
        return new NetException(message, "json_error", -1111);
    }

    public static NetException init(String response, int code) {
        if (code == 503) {
            try {
                Gson g = new Gson();
           /*     JsonElement js = g.fromJson(response, JsonElement.class);
                String mesg = js.getAsJsonObject().get("maintenanceMessage").getAsString();
                boolean main = js.getAsJsonObject().get("isUnderMaintenance").getAsBoolean();
                NetException e = new NetException(mesg, "MAINTAIN", code);
                e.maintain = main;*/
                NetException e = g.fromJson(response, NetException.class);
                return e;
            } catch (Exception e) {
                Log.d("NetException", e.getMessage());
                e.printStackTrace();
                return new NetException("Server Under Maintenance.Please try again later", "BODY_CANT_PASS", code);
            }
        } else {
            try {
                Gson g = new Gson();
                NetException exp = g.fromJson(response, NetException.class);
                exp.setCode(code);
                return exp;
            } catch (Exception e) {
                Log.d("NetException", e.getMessage());
                e.printStackTrace();
                return new NetException("Something went wrong", "BODY_CANT_PASS", code);
            }
        }

    }

    @Override
    public String getMessage() {
        return message;
    }


    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isMaintain() {
        return maintain;
    }

    public void setMaintain(boolean maintain) {
        this.maintain = maintain;
    }

    public Net.TryAgain getTryAgain() {
        return tryAgain;
    }

    public NetException setTryAgain(Net.TryAgain tryAgain) {
        this.tryAgain = tryAgain;
        return this;
    }

    public void printe(String tag) {
        Log.e(tag, "code : " + code + " \nmessage : " + message + " \nmessage_id :" + message_id);
    }

    public void printd(String tag) {
        Log.e(tag, "code : " + code + " \nmessage : " + message + " \nmessage_id :" + message_id);
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }


}
