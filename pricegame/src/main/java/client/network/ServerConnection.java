package client.network;

import common.protocol.Message;
import common.protocol.SocketWrapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.Consumer;

/** Kết nối tới server và luồng lắng nghe message đẩy về. */
public class ServerConnection {
    private SocketWrapper wrapper;
    private Thread reader;

    /** Kết nối; onMessage chạy trên thread đọc, onClosed khi mất kết nối. */
    public void connect(String host, int port, Consumer<Message> onMessage, Runnable onClosed) throws IOException {
        Socket s = new Socket();
        s.connect(new InetSocketAddress(host, port), 5000);
        wrapper = new SocketWrapper(s);
        reader = new Thread(() -> {
            try {
                Message m;
                while ((m = wrapper.receive()) != null) onMessage.accept(m);
            } catch (IOException ignored) {
            } finally {
                wrapper.close();
                onClosed.run();
            }
        }, "server-listener");
        reader.setDaemon(true);
        reader.start();
    }

    public boolean isConnected() { return wrapper != null && !wrapper.isClosed(); }

    public void send(Message m) {
        try {
            if (wrapper != null) wrapper.send(m);
        } catch (IOException e) {
            wrapper.close();
        }
    }

    public void close() {
        if (wrapper != null) wrapper.close();
    }
}
