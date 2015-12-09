import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class TcpServer {

    public interface RequestHandler {
        String[] handle(String[] response);
    }

    private final int port;
    private final RequestHandler r;

    public TcpServer(int port, RequestHandler r) {
        this.port = port;
        this.r = r;
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                final Socket s = serverSocket.accept();
                handleRequest(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(Socket s) throws IOException {
        PrintWriter out =
                new PrintWriter(s.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(s.getInputStream()));
        LinkedList<String> request = new LinkedList<>();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            if ("".equals(inputLine)) {
                break;
            }
            request.add(inputLine);
        }
        final String[] response = r.handle(request.stream().toArray(String[]::new));
        long length = response.length;
        for (String line : response) {
            length += line.getBytes().length;
        }
        out.append("HTTP/1.1 200 OK\n");
        out.append("Content-Length: ").append(Long.toString(length)).append("\n");
        out.append("Content-Type: application/json").append("\n");
        out.append("Connection: close").append("\n");
        out.append("\n");
        for (String line : response) {
            out.append(line).append("\n");
        }
        out.flush();
        out.close();
        in.close();
        s.close();
    }
}
