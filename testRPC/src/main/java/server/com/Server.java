package server.com;

import java.io.IOException;

/**
 * @author HD
 * @date. 2018/2/13
 */
public interface Server {
    void stop();

    void start() throws IOException;

    void register(Class serviceInterface, Class impl);

    boolean isRunning();

    int getPort();
}
