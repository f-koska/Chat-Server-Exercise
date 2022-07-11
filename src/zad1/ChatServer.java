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
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ChatServer {
    private ServerSocketChannel serverSocket;
    private Selector selector;
    public volatile boolean serverRunning = true;
    private ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
    private StringBuffer requests = new StringBuffer();
    private StringBuffer writer = new StringBuffer();
    private Charset charset = StandardCharsets.UTF_8;
    private StringBuilder serverLog = new StringBuilder();
    private Map<SocketChannel,String> clientId = new HashMap<>();
    Thread thread;

    public ChatServer(String host, int port) throws IOException {
     serverSocket = ServerSocketChannel.open();
     serverSocket.configureBlocking(false);
     serverSocket.socket().bind(new InetSocketAddress(host,port));
     selector = Selector.open();
     serverSocket.register(selector, SelectionKey.OP_ACCEPT);

    }
    public void startServer(){
        System.out.println("Server started");
        Runnable runnable = new Runnable() {
            @Override
            public synchronized void  run() {
                while (serverRunning) {
                    try {
                        selector.select();
                        Set keys = selector.selectedKeys();
                        Iterator iter = keys.iterator();
                        while (iter.hasNext()) {
                            SelectionKey key = (SelectionKey) iter.next();
                            iter.remove();
                            if (key.isAcceptable()) {
                                SocketChannel sc = serverSocket.accept();
                                sc.configureBlocking(false);
                                sc.register(selector, SelectionKey.OP_READ);
                                continue;
                            }
                            if (key.isReadable()) {
                                SocketChannel sc = (SocketChannel) key.channel();
                                if (sc.isOpen()) {
                                    requests.setLength(0);
                                    byteBuffer.clear();
                                    boolean reading = true;
                                    while (reading) {
                                        int size = sc.read(byteBuffer);
                                        if (size > 0) {
                                            byteBuffer.flip();
                                            CharBuffer charBuffer = charset.decode(byteBuffer);
                                            while (charBuffer.hasRemaining()) {
                                                char c = charBuffer.get();
                                                requests.append(c);
                                            }
                                            reading = false;
                                            requests.append("\n");
                                        }
                                    }
                                    String[] msq = requests.toString().split("\n");
                                    for(String m : msq) {
                                        serverLog.append(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))).append(" ").append(m).append("\n");
                                        if (!clientId.containsKey(sc)) {
                                            String[] tab = requests.toString().split(" ");
                                            clientId.put(sc, tab[0]);
                                        }
                                    }

                                    for (String m : msq) {
                                        String id = clientId.get(sc) + ": ";
                                        for (SocketChannel socketChannel : clientId.keySet()) {
                                            //if(!m.contains("logged")) {
                                                writer.append(id);
                                            //}
                                                writer.append(m).append("\n");
                                                ByteBuffer byteBuffer = charset.encode(CharBuffer.wrap(writer));
                                                socketChannel.write(byteBuffer);
                                                writer.setLength(0);
                                                byteBuffer.clear();
                                        }
                                        if (m.contains(clientId.get(sc) + " logged out")) {
                                            clientId.remove(sc);
                                            sc.socket().close();
                                            sc.close();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        };
        thread = new Thread(runnable);
        thread.start();
    }


    public void stopServer(){
        serverRunning=false;
        thread.interrupt();
        System.out.println("\nServer stopped");
    }

    public String getServerLog(){
        return serverLog.toString();
    }
}
