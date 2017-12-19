package com.bluedon;

import com.hd.ThreadPoolUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * @author HD
 * @date. 2017/12/14
 */
@Controller
@RequestMapping(value = "test/")
public class UploadFile {

    private Long startTime;
    private Map<String, Integer> fileMap = new HashMap<>();
    Object lock = new Object();
    ThreadPoolUtil pool = ThreadPoolUtil.init(100);

    static {
        new Thread("AAAA") {
            @Override
            public void run() {
                while (true) {

                }
            }
        }.start();
    }

    @RequestMapping(value = "uploadFile", method = RequestMethod.POST)
    @ResponseBody
    public DeferredResult<Map<String, String>> uploadFile(@RequestParam(value = "data") MultipartFile file,
                                                          @RequestParam Map<String, String> map,
                                                          HttpServletRequest request,
                                                          HttpServletResponse response) {
        System.out.println("请求进入...............");
        if (startTime == null) {
            startTime = System.currentTimeMillis();
        }
        synchronized (lock) {
            fileMap.put(map.get("fileName"), fileMap.get(map.get("fileName")) == null ? Integer.parseInt(map.get("total")) - 1 : fileMap.get(map.get("fileName")) - 1);
        }

        DeferredResult<Map<String, String>> deferredResult = new DeferredResult<>();

        pool.execute(new Runnable() {
            @Override
            public void run() {
                handle(deferredResult, file, map);
            }
        });
        new Thread() {
            @Override
            public void run() {
               /* while (true) {
                    Set<String> keys = fileMap.keySet();
                    for (String s : keys) {
                        if (fileMap.get(s) == 0) {
                            try {
                                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File("f://storage//" + fileName)));
                                for (int i = 1; i <= totalSlice; i++) {
                                    File tmp = new File(String.format("%s%s_%s.%s", tmpdir, fileName, md5, i));
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
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                    }
                }
*/
            }
        }.start();
        System.out.println("请求释放..............");
        return deferredResult;
    }


    @RequestMapping(value = "judgeFileExisted/{fileName}/{md5Value}")
    public Map<String, String> judgeFileExisted(@PathVariable("fileName") String fileName,
                                                @PathVariable("md5Value") String md5Value,
                                                HttpServletRequest request,
                                                HttpServletResponse response) {

        Map<String, String> map = new HashMap<>();
        map.put("flag", "true");
        return map;
    }


    @RequestMapping(value = "test", method = RequestMethod.GET)
    @ResponseBody
    public User test(HttpServletRequest request,
                     HttpServletResponse response) {
        return new User(1, "lalall");
    }

    @RequestMapping(value = "test2", method = RequestMethod.GET)
    @ResponseBody
    public DeferredResult<User> test2(HttpServletRequest request,
                                      HttpServletResponse response) {
        DeferredResult<User> dr = new DeferredResult<>();


        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                    dr.setResult(new User(1, "lalall"));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        System.out.println("==================");
        return dr;
    }

    @RequestMapping(value = "test3", method = RequestMethod.GET)
    @ResponseBody
    public Callable<User> test3(HttpServletRequest request,
                                HttpServletResponse response) {

        Callable<User> callable = new Callable<User>() {
            @Override
            public User call() throws Exception {
                Thread.sleep(5000);
                return new User(1, "ppppppppp");
            }
        };
        System.out.println("|||||||||||||||||||||");
        return callable;
    }

    public void handle(DeferredResult<Map<String, String>> deferredResult, MultipartFile file, Map<String, String> map) {
        System.out.println("异步处理请求..........");
        String fileName = map.get("name");
        int totalSlice = Integer.parseInt(map.get("total"));
        int currentIndex = Integer.parseInt(map.get("index"));
        System.out.println(currentIndex);
        String md5 = map.get("md5");
        try {
            String tmpdir = "f://tmp//";
            File storageTmp = new File(String.format("%s%s_%s.%s", tmpdir, fileName, md5, currentIndex));
            if (!storageTmp.exists()) {
                file.transferTo(storageTmp);
            }
            /*  int totalTmp = Objects.requireNonNull(new File(tmpdir).list(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.contains(String.format("%s_%s", fileName, md5));
                        }
                    })).length;*/

                    /*if (totalTmp == totalSlice) {
                        System.out.println("==================" + (System.currentTimeMillis() - startTime));
                        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File("f://storage//" + fileName)));
                        for (int i = 1; i <= totalSlice; i++) {
                            File tmp = new File(String.format("%s%s_%s.%s", tmpdir, fileName, md5, i));
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

                    }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        deferredResult.setResult(map);
    }
}

