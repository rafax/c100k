package com.gajdulewicz.jnio;

import java.io.*;

/**
 * Created by rafal on 24/01/16.
 */
public class Responses {

    public interface Response {
        ByteArrayOutputStream getOutput();
    }

    private static int length(String[] lines) {
        int cnt = lines.length;
        for (int i = 0; i < lines.length; i++) {
            cnt += lines[i].getBytes().length;
        }
        return cnt;
    }

    public static Response json(String[] lines) {
        ByteArrayOutputStream res = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(res);
        out.append("HTTP/1.1 200 OK\n");
        out.append("Content-Length: ").append(Long.toString(length(lines))).append("\n");
        out.append("Content-Type: application/json").append("\n");
        out.append("Connection: close").append("\n");
        out.append("\n");
        for (String line : lines) {
            out.append(line).append("\n");
        }
        out.flush();
        out.close();
        return () -> res;
    }

    public static Response badRequest(){
        ByteArrayOutputStream res = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(res);
        out.append("HTTP/1.1 200 OK\n");
        out.append("Connection: close").append("\n");
        out.close();
        return () -> res;
    }

    private static final int BUF_SIZE = 0x1000; // 4K

    public static long copy(InputStream from, OutputStream to)
            throws IOException {
        byte[] buf = new byte[BUF_SIZE];
        long total = 0;
        while (true) {
            int r = from.read(buf);
            if (r == -1) {
                break;
            }
            to.write(buf, 0, r);
            total += r;
        }
        return total;
    }
}
