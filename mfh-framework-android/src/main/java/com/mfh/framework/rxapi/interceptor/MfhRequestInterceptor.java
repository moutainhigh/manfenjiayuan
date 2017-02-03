package com.mfh.framework.rxapi.interceptor;


import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.login.logic.MfhLoginService;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;

/**
 * Application Interceptors
 * This interceptor compresses the HTTP request body. Many webservers can't handle this!
 */
public class MfhRequestInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        long t1 = System.nanoTime();
        Response response;
        if (originalRequest.body() == null) {
            ZLogger.d(String.format("---> [%s] %s on %s%n%s", originalRequest.method(),
                    originalRequest.url(), chain.connection(), originalRequest.headers()));

            ZLogger.d("method GET must not have a request body.");
            response = chain.proceed(originalRequest);
        }
        else{
            Request compressedRequest = originalRequest.newBuilder()
//                    .header("Content-Encoding", "gzip")
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .header("Accept-Encoding", "gzip")
                    .header("Connection", "Keep-Alive")
//                    .header("Accept", "*/*")
                    .header("Cookie", MfhLoginService.get().getCurrentSessionId())
                    .header("Set-Cookie", MfhLoginService.get().getCurrentSessionId())
                    .header("cookie", MfhLoginService.get().getCurrentSessionId())
                    .method(originalRequest.method(), gzip(originalRequest.body()))
                    .build();

            ZLogger.d(String.format("---> [%s] %s on %s%n%s", compressedRequest.method(),
                    compressedRequest.url(), chain.connection(), compressedRequest.headers()));
            response = chain.proceed(compressedRequest);
        }

        long t2 = System.nanoTime();
        ZLogger.d(String.format("<--- response from %s in %.1fms%n%s",
                response.request().url(), (t2 - t1) / 1e6d, response.headers()));
        return response;
    }

    private RequestBody gzip(final RequestBody body) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return body.contentType();
            }

            @Override
            public long contentLength() {
                return -1; // We don't know the compressed length in advance!
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
                body.writeTo(gzipSink);
                gzipSink.close();
            }
        };
    }
}