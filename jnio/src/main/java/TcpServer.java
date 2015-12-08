import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
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
                Socket s = serverSocket.accept();
                System.out.println("Got request");
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
            if ("\r\n".equals(inputLine)) {
                System.out.println("Read headers, break");
                break;
            }
            System.out.println("Read" + Arrays.toString(inputLine.toCharArray()));
            request.add(inputLine);
        }
        System.out.println("Full request read");
        in.close();
        final String[] response = r.handle((String[]) request.toArray());
        for (String line : response) {
            out.write(line);
        }
        out.close();
    }
}
