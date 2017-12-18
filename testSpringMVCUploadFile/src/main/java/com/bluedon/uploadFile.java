package com.bluedon;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author HD
 * @date. 2017/12/14
 */
@Controller
@RequestMapping(value = "test/")
public class uploadFile {

    private Long startTime;

    @RequestMapping(value = "uploadFile", method = RequestMethod.POST)
    @ResponseBody
    public DeferredResult<Map<String, String>> uploadFile(@RequestParam(value = "data") MultipartFile file,
                                                          @RequestParam Map<String, String> map,
                                                          HttpServletRequest request,
                                                          HttpServletResponse response) {
        Map<String, String> mapTmp = new HashMap<>();
        DeferredResult<Map<String, String>> deferredResult = new DeferredResult<>();
        System.out.println("请求进入...............");
        if (startTime == null) {
            startTime = System.currentTimeMillis();
        }
        new Thread() {
            @Override
            public void run() {
                System.out.println("异步处理请求..........");
                String fileName = map.get("name");
                int totalSlice = Integer.parseInt(map.get("total"));
                int currentIndex = Integer.parseInt(map.get("index"));
                String md5 = map.get("md5");
                try {
                    String tmpdir = "f://tmp//";
                    file.transferTo(new File(tmpdir + fileName + "." + currentIndex));

                    if (currentIndex == totalSlice) {
                        System.out.println("==================" + (System.currentTimeMillis() - startTime));
                        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File("f://storage//" + fileName)));
                        for (int i = 1; i <= totalSlice; i++) {
                            File tmp = new File(tmpdir + fileName + "." + i);
                            BufferedInputStream in = new BufferedInputStream(new FileInputStream(tmp));
                            byte[] bytes = new byte[2048];
                            int len;
                            while ((len = in.read(bytes)) != -1) {
                                out.write(bytes, 0, len);
                            }
                            in.close();
                            //tmp.delete();
                        }
                        out.close();

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mapTmp.put("fileName", fileName);
                mapTmp.put("totalSlice", totalSlice + "");
                mapTmp.put("currentIndex", currentIndex + "");
                mapTmp.put("md5", md5 + "");
                deferredResult.setResult(mapTmp);
            }
        }.start();
        System.out.println("请求释放..............");

        return deferredResult;
    }


    @RequestMapping(value = "test", method = RequestMethod.GET)
    @ResponseBody
    public User test(HttpServletRequest request,
                     HttpServletResponse response) {
        return new User(1, "lalall");
    }
}

