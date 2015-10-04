package com.gajdulewicz;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class C100kClient {

    private final CloseableHttpAsyncClient httpclient;
    private final int count;

    final ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();

    public C100kClient(CloseableHttpAsyncClient httpclient, int count) {
        this.httpclient = httpclient;
        this.count = count;
    }

    public void start() {
        try {
            for (int i = 0; i < count; i++) {
                poll(0);
            }
        } finally {
            System.out.println("Shutting down");
        }
    }

    private void poll(final int i) {
        HttpGet request = new HttpGet("http://127.0.0.1:8080");
        httpclient.execute(request, new FutureCallback<HttpResponse>() {
            public void completed(HttpResponse response) {
                ex.schedule(() -> poll(i + 1), 10, TimeUnit.MILLISECONDS);
            }

            public void failed(Exception e) {

            }

            public void cancelled() {

            }
        });
    }
}
