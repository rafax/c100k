package com.gajdulewicz.jnio;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TcpServer {

    public interface RequestHandler {
        String[] handle(String[] request);
    }

    private final int port;
    private final RequestHandler r;
    private final ScheduledExecutorService ex;
    private final AtomicInteger total, inFlight;

    public TcpServer(int port, RequestHandler r) {
        this.port = port;
        this.r = r;
        ex = Executors.newScheduledThreadPool(10);
        total = new AtomicInteger(0);
        inFlight = new AtomicInteger(0);
        ex.scheduleAtFixedRate(() -> System.out.println(
                String.format("[%s] Total: %s In-flight: %s", LocalDateTime.now(), total.get(), inFlight.get())
        ), 5, 5, TimeUnit.SECONDS);
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                final Socket s = serverSocket.accept();
                total.incrementAndGet();
                inFlight.incrementAndGet();
                ex.submit(() -> {
                    handleRequest(s);
                    inFlight.decrementAndGet();
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(Socket s) {
        OutputStream out = null;
        BufferedReader in = null;
        try {
            out = s.getOutputStream();
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            LinkedList<String> request = new LinkedList<>();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if ("".equals(inputLine)) {
                    break;
                }
                request.add(inputLine);
            }
            final String[] response = r.handle(request.stream().toArray(String[]::new));
            Responses.json(response).getOutput().writeTo(out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
