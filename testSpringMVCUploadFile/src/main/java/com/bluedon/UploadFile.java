package com.bluedon;

import com.hd.ThreadPoolUtil;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author HD
 * @date. 2017/12/14
 */
@Controller
@RequestMapping(value = "test/")
public class UploadFile {
    private static TrackerClient trackerClient;
    private static TrackerServer trackerServer;
    private static StorageClient storageClient;
    public static final String CLIENT_CONFIG_FILE = "client.conf";

    private Long startTime;
    private static Map<String, Integer> fileMap = new HashMap<>();
    static String tmpDir = "f://tmp//";
    static String storageDir = "f://storage//";
    Object lock = new Object();
    ThreadPoolUtil pool = ThreadPoolUtil.init(100);

    static {
        try {
            //
            //初始化配置文件
            ClientGlobal.init(CLIENT_CONFIG_FILE);
            trackerClient = new TrackerClient();
            trackerServer = trackerClient.getConnection();
            storageClient = new StorageClient(trackerServer, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

      /*  new Thread("toMysql") {
            @Override
            public void run() {
                while (true) {
                    Set<Map.Entry<String, Integer>> entries = fileMap.entrySet();
                    File[] files = new File(tmpDir).listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            String name = pathname.getName();
                            return Integer.parseInt(name.substring(name.lastIndexOf("_") + 1)) <= pathname.list().length;
                        }
                    });
                   *//* for (File file : files) {
                        try {
                            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(storageDir + fileName_md5)));
                            for (int i = 1; i <= totalSlice; i++) {
                                File tmp = new File(String.format("%s%s.%s", tmpDir, fileName_md5, i));
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
*//*

                }
            }

        }.start();*/

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
        DeferredResult<Map<String, String>> deferredResult = new DeferredResult<>();

        //TODO  去数据库查询MD5

        //数据库查询不到MD5 则存入本地
        if (true) {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    handle(deferredResult, file, map);
                }
            });
        } else {
            //文件相关信息存入数据库
            //TODO

            deferredResult.setResult(map);
        }

        System.out.println("请求释放..............");
        return deferredResult;
    }

    @RequestMapping(value = "uploadFileBranch/{fileName}/{MD5Value}")
    @ResponseBody
    public Map<String, String> uploadFileBranch(@PathVariable("fileName") String fileName,
                                                @PathVariable("md5Value") String md5Value,
                                                HttpServletRequest request,
                                                HttpServletResponse response) {

        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("fileName", fileName);
        //TODO 去数据库查询MD5值

        if (true) {
            //没有就告诉浏览器进行上传
            returnMap.put("progress", "0");
        } else {
            //有MD5值 就直接存文件名 告诉浏览器 进度100%
            //1.文件名存入数据库
            //返回浏览器
            returnMap.put("progress", "100");
        }
        return returnMap;

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
        String fileName = map.get("fileName");
        int totalSlice = Integer.parseInt(map.get("total"));
        int currentIndex = Integer.parseInt(map.get("index"));
        System.out.println(currentIndex);
        String md5 = map.get("md5");
        try {
            synchronized (lock) {
                File dirTmp = new File(String.format("%s%s_%s_%s", tmpDir, fileName, md5, totalSlice));
                if (!dirTmp.exists()) {
                    dirTmp.mkdir();
                }
                File storageTmp = new File(dirTmp, currentIndex + "");
                if (!storageTmp.exists()) {
                    file.transferTo(storageTmp);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        deferredResult.setResult(map);
    }

}

