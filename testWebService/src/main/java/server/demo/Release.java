package server.demo;

import javax.xml.ws.Endpoint;

/**
 * @author HD
 * @date. 2018/2/9
 */
public class Release {
    public static void main(String[] args) {
        String address = "http://127.0.0.1:8080/hello";
        Endpoint.publish(address, new ServerImpl());
        System.out.println("发布webService成功!");
    }
}
