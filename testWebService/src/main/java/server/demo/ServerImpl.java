package server.demo;

import javax.jws.WebMethod;
import javax.jws.WebService;
import java.util.Date;

/**
 * @author HD
 * @date. 2018/2/9
 */
@WebService
public class ServerImpl {
    @WebMethod
    public String sayHi(String name) {
        System.out.println(name +" 进入..........");
        return name + " : 当前时间:" + new Date();
    }
}
