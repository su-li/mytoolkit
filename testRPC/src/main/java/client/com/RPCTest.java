package client.com;

import server.com.HelloService;
import server.com.HelloServiceImpl;
import server.com.Server;
import server.com.ServiceCenter;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author HD
 * @date. 2018/2/13
 */
public class RPCTest {
    public static void main(String[] args) throws IOException {

        //服务端启动
        new Thread(new Runnable() {
            public void run() {
                try {
                    Server serviceServer = new ServiceCenter(8088);
                    serviceServer.register(HelloService.class, HelloServiceImpl.class);
                    serviceServer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        //客户端调用
        HelloService service = RPCClient.getRemoteProxyObj(HelloService.class, new InetSocketAddress("localhost", 8088));
        System.out.println(service.sayHi("test"));
    }
}
