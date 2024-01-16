package com.treinetic.whiteshark.network;


import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseAndStringRequestListener;

import okhttp3.Headers;
import okhttp3.Response;

import com.treinetic.whiteshark.BuildConfig;
import com.treinetic.whiteshark.constance.DeviceInformation;
import com.treinetic.whiteshark.exceptions.NetException;
import com.treinetic.whiteshark.services.LocalStorageService;
import com.treinetic.whiteshark.services.Service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class Net {

    public enum NetMethod {

        GET, POST, DELETE, PUT, UPLOAD

    }

    public class URL {

        final public static String SERVER = "https://app.gurulugomi.lk/api/";
//        final public static String SERVER = "http://dev.gurulugomi.lk/api/";

        final public static String VERSION = "v1.0/";

        final public static String UPDATE_SERVER = "http://update.treinetic.com/public/api/updates/{publicKey}/v/{version}";
        final public static String HOME = SERVER + VERSION + "home";
        final public static String EPUB = SERVER + VERSION + "epub";
        final public static String BOOK_BYID = SERVER + VERSION + "epub/{id}?ratings=0";
        final public static String BOOK_REVIEWS = SERVER + VERSION + "epub/{id}/rating";
        final public static String BOOK_PROFILE_REVIEWS = SERVER + VERSION + "epub/{id}/profile/rating";
        final public static String EVENT = SERVER + VERSION + "event";
        final public static String CATEGORY_LIST = SERVER + VERSION + "category";

        final public static String WISH_LIST = SERVER + VERSION + "epub?type=WISHLIST";
        final public static String MY_LIBRARY = SERVER + VERSION + "epub?type=MY_LIBRARY";

        final public static String ADD_TO_WISH_LIST = SERVER + VERSION + "wishlist";
        final public static String REMOVE_FROM_WISH_LIST = SERVER + VERSION + "wishlist/remove";

        final public static String CART = SERVER + VERSION + "cart";
        final public static String CHECKOUT = SERVER + VERSION + "cart/checkout";
        final public static String PAYMENT_GENERATE = SERVER + VERSION + "payment/generate";
        final public static String ADD_CART = SERVER + VERSION + "cart";
        final public static String DELETE_CART_ITEMS = SERVER + VERSION + "cart/remove";
        final public static String ORDER = SERVER + VERSION + "order/{id}";

        final public static String BILLING_DETAILS = SERVER + VERSION + "billingDetails";
        final public static String ADD_BILLING_DETAILS = SERVER + VERSION + "billingDetails";

        final public static String PURCHASED_HISTORY = SERVER + VERSION + "purchase";
        final public static String SEARCH = SERVER + VERSION + "search";

        final public static String REGISTER_USER = SERVER + VERSION + "register";
        final public static String LOGIN = SERVER + VERSION + "login";
        final public static String PASSWORD_RESET = SERVER + VERSION + "password/reset";

        final public static String SOCIAL_LOGIN = SERVER + VERSION + "socialLogin";
        final public static String USER_INIT = SERVER + VERSION + "init";

        final public static String SAVE_REVIEW = SERVER + VERSION + "epub/{id}/rating";
        final public static String DELETE_REVIEW = SERVER + VERSION + "epub/{id}/rating/{ratingId}";
        final public static String UPDATE_USER = SERVER + VERSION + "user";

        final public static String DEVICES = SERVER + VERSION + "device";
        final public static String REMOVE_DEVICES = SERVER + VERSION + "device/{id}";
        final public static String DOWNLOAD_FONTS = SERVER + VERSION + "fonts";
        final public static String PROMOTIONS = SERVER + VERSION + "promotions";
        final public static String LIBRARY = SERVER + VERSION + "library";

        final public static String PUBLISHERS = SERVER + VERSION + "search/publishers?searchTerm=null";
        final public static String AUTHORS = SERVER + VERSION + "search/author?searchTerm=null";

        final public static String SHARE_LINK = SERVER + VERSION + "share/e-pub/{id}/image";

        final public static String ADD_PROMO_CODE = SERVER + VERSION + "promo_code/validate";
        final public static String REMOVE_PROMO_CODE = SERVER + VERSION + "promo_code/cancel";

        final public static String LOYALTY_POINT_REQUEST_OTP = SERVER + VERSION + "otp";
        final public static String LOYALTY_POINT_OTP_VERIFY = SERVER + VERSION + "otp/verify";
        final public static String LOYALTY_POINT_ADD = SERVER + VERSION + "loyalty_points/validate";
        final public static String LOYALTY_POINT_CANCEL = SERVER + VERSION + "loyalty_points/cancel";

        final public static String E_VOUCHER_ADD = SERVER + VERSION + "e_voucher/validate";
        final public static String E_VOUCHER_CANCEL = SERVER + VERSION + "e_voucher/cancel";

        final public static String TOPICS = SERVER + VERSION + "topic";
        final public static String APPLE_AUTH = SERVER + VERSION + "login/apple/callback";


    }

    private final static String TAG = "Net";

    private static String TOKEN = null;
    private String url;
    private Object body;
    private Map<String, Object> queryParam = new HashMap<>();
    private Map<String, Object> pathParam = new HashMap<>();
    private Map<String, File> files = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();
    private NetMethod method;
    private Service.Success<Float> progressListener;
    private boolean excludeToken = false;

    public Net(String url, NetMethod method, Object body,
               Map<String, Object> queryParam,
               Map<String, Object> pathParam,
               Map<String, String> headers) {
        this.url = url;
        this.body = body;
        this.queryParam = queryParam;
        this.pathParam = pathParam;
        this.headers = headers;
        this.method = method;
    }

    public Net setProgressListener(Service.Success<Float> progressListner) {
        this.progressListener = progressListner;
        return this;
    }

    public Net setFiles(Map<String, File> files) {
        this.files = files;
        return this;
    }


    public Net perform(final Success success, final Error error) {
        ANRequest request = null;

        switch (method) {
            case GET:
                request = GET();
                break;
            case POST:
                request = POST();
                break;
            case DELETE:
                request = DELETE();
                break;
            case PUT:
                request = PUT();
                break;
            case UPLOAD:
                request = MULTIPART();
                break;
        }

        Log.i(TAG, "[" + method + "] url - " + url + ", body " + body + ", query " + queryParam +
                ", path " + pathParam + ", headers " + headers + ", files " + files);

        request.getAsOkHttpResponseAndString(new OkHttpResponseAndStringRequestListener() {

            @Override
            public void onResponse(Response okHttpResponse, String response) {
                String endpoint = url;
                if (okHttpResponse.networkResponse() != null && okHttpResponse.networkResponse().request() != null)
                    endpoint = okHttpResponse.networkResponse().request().url().toString();
                Log.i(TAG, "onResponse: " + endpoint);
                if (okHttpResponse.isSuccessful()) {
                    Log.i(TAG, "[" + method + "] success - " + 200 + ", body - " + response);
                    headerParse(okHttpResponse.headers());
                    if (success != null) success.success(response);
                    return;
                }
                //call back error
                String body = null;
                try {
                    body = okHttpResponse.networkResponse().body().string();
                } catch (Exception e) {
                    e.printStackTrace();
                    if (error != null)
                        error.error(new NetException(e.getMessage(), "BODY_CANT_PASS", -999));
                }
                Log.e(TAG, "[" + method + "] error - " + okHttpResponse.networkResponse().code() + ", body - " + body);
                if (error != null)
                    error.error(NetException.init(body, okHttpResponse.networkResponse().code()).setTryAgain(() -> {
                        perform(success, error);
                    }));

            }

            @Override
            public void onError(ANError anError) {
                Log.i(TAG, "onError: " + anError.getErrorCode());
                Log.i(TAG, "onError: " + anError.getErrorBody());
                Log.i(TAG, "onError: " + anError.getErrorDetail());
                NetException exception = null;
                String body = null;
                try {
                    body = anError.getErrorBody();
                    if (body != null)
                        exception = NetException.init(body, anError.getErrorCode());
                    else
                        exception = new NetException(anError.getErrorDetail(), "BODY_CANT_PASS", anError.getErrorCode());

                } catch (Exception e) {
                    e.printStackTrace();
                    exception = new NetException(body, "BODY_CANT_PASS", -999);
                }
                Log.e(TAG, "[" + method + "] error - " + exception.getCode() +
                        ", body - " + exception.getMessage() +
                        ", description - " + anError.getLocalizedMessage()
                );
                exception.setResponse(anError.getResponse());
                if (error != null)
                    error.error(exception.setTryAgain(() -> {
                        perform(success, error);
                    }));
            }
        });

        return this;
    }

    private ANRequest GET() {
        ANRequest.GetRequestBuilder requestBuilder = AndroidNetworking.get(url);
        if (queryParam != null) requestBuilder.addQueryParameter(getAsString(queryParam));
        if (pathParam != null) requestBuilder.addPathParameter(getAsString(pathParam));
        requestBuilder.addHeaders(getHeadersForRequest());
        requestBuilder.doNotCacheResponse();
        return requestBuilder.build();
    }

    private ANRequest POST() {
        ANRequest.PostRequestBuilder requestBuilder = AndroidNetworking.post(url);
        if (queryParam != null) requestBuilder.addQueryParameter(getAsString(queryParam));
        if (pathParam != null) requestBuilder.addPathParameter(getAsString(pathParam));
        requestBuilder.addHeaders(getHeadersForRequest());
        if (body != null) requestBuilder.addApplicationJsonBody(body);
        requestBuilder.doNotCacheResponse();
        return requestBuilder.build();
    }

    private ANRequest MULTIPART() {
        ANRequest.MultiPartBuilder requestBuilder = AndroidNetworking.upload(url);
        if (queryParam != null) requestBuilder.addQueryParameter(getAsString(queryParam));
        if (pathParam != null) requestBuilder.addPathParameter(getAsString(pathParam));
        requestBuilder.addHeaders(getHeadersForRequest());
        requestBuilder.addMultipartParameter(body);
        if (files != null) requestBuilder.addMultipartFile(files);
        requestBuilder.doNotCacheResponse();
        return requestBuilder.build().setUploadProgressListener((bytesUploaded, totalBytes) -> {
            if (progressListener != null) {
                progressListener.success(bytesUploaded / totalBytes * 100f);
            }
        });
    }

    private ANRequest DELETE() {
        ANRequest.PostRequestBuilder requestBuilder = AndroidNetworking.delete(url);
        if (queryParam != null) requestBuilder.addQueryParameter(getAsString(queryParam));
        if (pathParam != null) requestBuilder.addPathParameter(getAsString(pathParam));
        requestBuilder.addHeaders(getHeadersForRequest());
        if (body != null) requestBuilder.addApplicationJsonBody(body);
        requestBuilder.doNotCacheResponse();
        return requestBuilder.build();
    }

    private ANRequest PUT() {
        ANRequest.PostRequestBuilder requestBuilder = AndroidNetworking.put(url);
        if (queryParam != null) requestBuilder.addQueryParameter(getAsString(queryParam));
        if (pathParam != null) requestBuilder.addPathParameter(getAsString(pathParam));
        requestBuilder.addHeaders(getHeadersForRequest());
        if (body != null) requestBuilder.addApplicationJsonBody(body);
        requestBuilder.doNotCacheResponse();
        return requestBuilder.build();
    }

    private void headerParse(Headers headers) {
        if (headers.get("Authorization") != null) {
            TOKEN = headers.get("Authorization");
            LocalStorageService.getInstance().saveToken(TOKEN);
            Log.i(TAG, "token set - " + TOKEN);
        }
    }

    private Map<String, String> getHeadersForRequest() {

        if (headers == null) {
            headers = new HashMap<>();
        }

        if (TOKEN == null) {
            TOKEN = LocalStorageService.getInstance().getToken();
        }
//        headers.put("development", "true");
        if (TOKEN != null && !headers.containsKey("Authorization") && !excludeToken) {
            headers.put("Authorization", TOKEN);
        }

        if (!headers.containsKey("t-device-id")) {
            String deviceId = DeviceInformation.Companion.getInstance().getDeviceId();
//            String deviceId = "12345";
            headers.put("t-device-id", deviceId);
        }
        headers.put("T-AppVersion-Code", String.valueOf(BuildConfig.VERSION_CODE));
        headers.put("T-App", "android");
        headers.put("T-Version", BuildConfig.VERSION_NAME);

        return headers;
    }

    private Map<String, String> getAsString(Map<String, Object> oldParams) {
        Map<String, String> param = new HashMap<>();
        for (Map.Entry<String, Object> entry : oldParams.entrySet()) {
            param.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return param;
    }


    public interface Success {
        public void success(String response);
    }

    public interface Error {
        public void error(NetException exception);
    }

    public interface TryAgain {
        public void tryAgain();
    }

    public static void setTOKEN(String TOKEN) {
        Net.TOKEN = TOKEN;
    }
}


