package com.gajdulewicz;


import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.reactor.ConnectingIOReactor;

public class Main {

    public static void main(String[] args) throws Exception {
        int count = 1000;
//        if (args.length > 0) {
//            count = Integer.parseInt(args[1]);
//        }

        System.out.println("Cnt: "+count);
        ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();
        PoolingNHttpClientConnectionManager cm = new PoolingNHttpClientConnectionManager(ioReactor);
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom().setConnectionManager(cm).build();
        cm.setMaxTotal(Integer.MAX_VALUE);
        cm.setDefaultMaxPerRoute(Integer.MAX_VALUE);
        httpclient.start();
        new C100kClient(httpclient, count).start();
        while (true) {
            System.out.println(cm.getTotalStats());
            Thread.sleep(5000);
        }
    }


}
