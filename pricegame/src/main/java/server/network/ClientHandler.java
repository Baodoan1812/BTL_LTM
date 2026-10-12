package server.network;

import common.protocol.Message;
import common.protocol.SocketWrapper;
import server.logic.Player;
import server.logic.RequestDispatcher;

import java.io.IOException;
import java.net.Socket;

/** Thread riêng cho mỗi client: đọc message và chuyển cho dispatcher. */
public class ClientHandler extends RequestDispatcher.Session implements Runnable {
    private final SocketWrapper wrapper;
    private final RequestDispatcher dispatcher;

    public ClientHandler(Socket socket, RequestDispatcher dispatcher) throws IOException {
        this.wrapper = new SocketWrapper(socket);
        this.dispatcher = dispatcher;
    }

    @Override
    public void send(Message m) {
        try {
            wrapper.send(m);
        } catch (IOException e) {
            wrapper.close(); // vòng đọc sẽ kết thúc và dọn dẹp
        }
    }

    @Override
    public void logout() {
        wrapper.close();
    }

    @Override
    public void run() {
        try {
            Message m;
            while ((m = wrapper.receive()) != null) {
                try {
                    dispatcher.dispatch(this, m);
                } catch (RuntimeException e) {
                    System.err.println("Lỗi xử lý " + m.type + ": " + e);
                }
            }
        } catch (IOException ignored) {
            // mất kết nối đột ngột
        } finally {
            Player p = player;
            player = null;
            if (p != null) dispatcher.cleanup(p);
            wrapper.close();
        }
    }
}
