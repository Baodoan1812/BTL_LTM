package common.protocol;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/** Bọc Socket: gửi/nhận mỗi message trên một dòng JSON (UTF-8). */
public class SocketWrapper implements Closeable {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;

    public SocketWrapper(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.out = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)), true);
    }

    /** Nhiều thread có thể gửi cùng lúc nên phải đồng bộ. */
    public synchronized void send(Message m) throws IOException {
        out.println(JsonCodec.encode(m));
        if (out.checkError()) throw new IOException("Không gửi được dữ liệu");
    }

    /** Chặn đến khi có message. Trả về null khi đối phương đóng kết nối. Bỏ qua dòng sai định dạng. */
    public Message receive() throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            Message m = JsonCodec.decode(line);
            if (m != null) return m;
        }
        return null;
    }

    public boolean isClosed() { return socket.isClosed(); }

    @Override
    public void close() {
        try { socket.close(); } catch (IOException ignored) {}
    }
}
