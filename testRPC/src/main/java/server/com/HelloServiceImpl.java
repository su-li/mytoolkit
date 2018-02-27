package server.com;

/**
 * @author HD
 * @date. 2018/2/13
 */
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHi(String name) {
        return "hi," + name;
    }
}
