/**
 *
 *  @author Kośka Filip S20804
 *
 */

package zad1;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ChatClient {
    private String host;
    private int port;
    private String id;
    private SocketChannel socket;
    public ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
    private Charset charset = StandardCharsets.UTF_8;
    public StringBuffer reader = new StringBuffer();
    private StringBuffer writer = new StringBuffer();
    private  StringBuilder chatView = new StringBuilder();
    public volatile boolean running = true;
    public volatile boolean logged = false;

    public ChatClient(String host, int port, String id){
        this.host=host;
        this.port=port;
        this.id=id;
        chatView.append("=== ").append(id).append(" chat view\n");
    }

    public void login() throws IOException, InterruptedException {
        socket = SocketChannel.open();
        socket.configureBlocking(false);
        socket.connect(new InetSocketAddress(host,port));
        while (!socket.finishConnect()) {
            Thread.sleep(50);
        }
        String msg = id + " logged in";
        logged=true;
        send(msg);
    }

    public  void logout() throws IOException {
        String msg = id + " logged out";
        send(msg);
    }

    public void send(String msg) throws IOException {
            writer.setLength(0);
            writer.append(msg).append("\n");
            socket.write(charset.encode(CharBuffer.wrap(writer)));
    }

    public  synchronized void getResponse() throws IOException {
        String response = "";
        reader.setLength(0);

        while (logged){
                byteBuffer.clear();
                int n = socket.read(byteBuffer);
                if (n > 0) {
                    byteBuffer.flip();
                    CharBuffer charBuffer = charset.decode(byteBuffer);
                    while (charBuffer.hasRemaining()) {
                        char c = charBuffer.get();
                        reader.append(c);
                    }
                    response = reader.toString();
                    chatView.append(response);
                    byteBuffer.clear();
                }
                if (response.contains(id + " logged out")) {
                    logged = false;
                    notify();
                    socket.close();
                    socket.socket().close();
                }
                reader.setLength(0);
        }
    }

    public synchronized void stopClient(){
        while (logged){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String getChatView(){
        return chatView.toString();
    }
}
