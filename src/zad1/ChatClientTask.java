/**
 *
 *  @author Kośka Filip S20804
 *
 */

package zad1;


import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class ChatClientTask extends FutureTask<ChatClient> {
Thread thread;
ChatClient c;

    public ChatClientTask(ChatClient c, List<String> msgs, int wait) {
        super(() -> {
                c.login();
                if (wait != 0) {
                    Thread.sleep(wait);
                }

                for (String msg : msgs) {
                    c.send(msg);
                    if (wait != 0) {
                        Thread.sleep(wait);
                    }
                }
                c.logout();
                if (wait != 0) {
                    Thread.sleep(wait);
                }
                c.stopClient();
                c.running = false;
            return c;
        });
        this.c=c;


        Runnable runnable = () -> {
                while (c.running) {
                    try {
                        c.getResponse();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        };
        thread = new Thread(runnable);
        thread.start();
    }

    public static ChatClientTask create(ChatClient c, List<String> msgs, int wait){
        return new ChatClientTask(c,msgs,wait);
    }

    public ChatClient getClient() {
       thread.interrupt();
        try {
            return super.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
