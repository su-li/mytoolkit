package client.demo;

/**
 * @author HD
 * @date. 2018/2/13
 */
public class Test {
    public static void main(String[] args) {
        ServerImplService service = new ServerImplService();
        ServerImpl serverImplPort = service.getServerImplPort();
        String date = serverImplPort.sayHi("suli");
        System.out.println(date);
    }
}
