package com.gajdulewicz.jnio;

public class Main {

    public static void main(String[] args) {
        int port = 8908;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        new TcpServer(port, req -> {
            for (int i = 0; i < req.length; i++) {
                req[i] = req[i].toUpperCase();
            }
            return req;
        }).start();

    }
}
