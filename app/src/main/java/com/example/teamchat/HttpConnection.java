package com.example.teamchat;

import android.util.Log;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpConnection {
    public static class ReturnResponse {
        public final String body;
        public final int responseCode;

        public ReturnResponse(String body, int responseCode) {
            this.body = body;
            this.responseCode = responseCode;
        }
    }

    private static final String TAG = "HttpConnection";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final OkHttpClient client = getUnsafeOkHttpClient();

    public static ReturnResponse connect(String url, String method, String json) throws IOException{
        Request.Builder builder = new Request.Builder().url(url);

        if (method.equals("POST")) {
            RequestBody body = RequestBody.create(json, JSON);
            builder.post(body);
        } else {
            builder.get();
        }

        Request request = builder.header("Accept-Encoding", "identity").build();

//        return client.newCall(request).execute();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                //Log.e(TAG, "Error connecting to " + url + ": " + response);
                ReturnResponse returnResponse = new ReturnResponse("", -1);
                response.close();
                return returnResponse;
            }

            ReturnResponse returnResponse = new ReturnResponse(
                    Objects.requireNonNull(response.body()).string(),
                    response.code());

            response.close();
            return returnResponse;

        } catch (IOException e) {
            //Log.e(TAG, "Error connecting to " + url, e);
            return null;
        }
    }

    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}