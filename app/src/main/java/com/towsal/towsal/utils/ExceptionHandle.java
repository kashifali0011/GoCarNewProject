package com.towsal.towsal.utils;

import com.google.gson.JsonParseException;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;

import java.io.IOException;
import java.net.ConnectException;

import retrofit2.HttpException;

/**
 * Class for handling exceptions getting in api's
 * */
public class ExceptionHandle {

    private static final int UNAUTHORIZED = 401;
    private static final int FORBIDDEN = 403;
    private static final int NOT_FOUND = 404;
    private static final int REQUEST_TIMEOUT = 408;
    private static final int INTERNAL_SERVER_ERROR = 500;
    private static final int BAD_GATEWAY = 502;
    private static final int SERVICE_UNAVAILABLE = 503;
    private static final int GATEWAY_TIMEOUT = 504;

    /**
     * Functions for handling api response exceptions
     */
    public static String handleException(Throwable e) {
        String errorMsg;
        if (e instanceof HttpException) {
            HttpException httpException = (HttpException) e;
            switch (httpException.code()) {
                case UNAUTHORIZED:
                case FORBIDDEN:
                case NOT_FOUND:
                case REQUEST_TIMEOUT:
                case GATEWAY_TIMEOUT:
                case INTERNAL_SERVER_ERROR:
                case BAD_GATEWAY:
                case SERVICE_UNAVAILABLE:
                default:
                    errorMsg = e.getLocalizedMessage();
                    break;
            }
            return errorMsg + ":" + httpException.code();
        } else if (e instanceof JsonParseException || e instanceof JSONException) {
            return e.getLocalizedMessage();
        } else if (e instanceof ConnectException) {
            return e.getLocalizedMessage();
        } else if (e instanceof javax.net.ssl.SSLHandshakeException) {
            return e.getLocalizedMessage();
        } else if (e instanceof ConnectTimeoutException) {
            return e.getLocalizedMessage();
        } else if (e instanceof java.net.SocketTimeoutException) {
            return e.getLocalizedMessage();
        } else if (e instanceof IOException) {
            return e.getLocalizedMessage();
        } else {
            return "Something Went Wrong Try Again Later";
        }
    }

}
