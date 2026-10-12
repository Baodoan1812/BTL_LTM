package server.network;

import server.logic.RequestDispatcher;
import server.logic.ServerContext;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/** Chấp nhận kết nối và tạo ClientHandler (thread-per-client). */
public class ServerListener {
    public static final int DEFAULT_PORT = 9999;

    private final int port;
    private final RequestDispatcher dispatcher;
    private ServerSocket serverSocket;

    public ServerListener(int port, ServerContext ctx) {
        this.port = port;
        this.dispatcher = new RequestDispatcher(ctx);
    }

    /** Mở cổng (port 0 = tự chọn) và trả về cổng thực tế. */
    public int open() throws IOException {
        serverSocket = new ServerSocket(port);
        return serverSocket.getLocalPort();
    }

    public void acceptLoop() throws IOException {
        while (!serverSocket.isClosed()) {
            Socket s = serverSocket.accept();
            ClientHandler h = new ClientHandler(s, dispatcher);
            Thread t = new Thread(h, "client-" + s.getRemoteSocketAddress());
            t.setDaemon(true);
            t.start();
        }
    }

    public void stop() throws IOException {
        if (serverSocket != null) serverSocket.close();
    }

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        ServerListener l = new ServerListener(port, ServerContext.withDatabase());
        System.out.println("Server lắng nghe cổng " + l.open());
        l.acceptLoop();
    }
}
