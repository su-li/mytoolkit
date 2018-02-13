package com.bluedon;

import com.bluedon.utils.FileVerifyUtils;
import com.hd.ThreadPoolUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author HD
 * @date. 2017/12/20
 */
@Controller
@RequestMapping(value = "test3/")
public class UploadFile3 {

    private FDConnectionPool fdConnectionPool = FDConnectionPool.init(50);

    //缓存在磁盘的位置
    private static final String DISK_TMP = "f://Tmp//";
    //存储已经完成的文件的MD5值
    private static final String MD5S = "md5s";
    //上传的文件列表
    private static final String FILE_LIST = "files";

    @RequestMapping(value = "uploadFile", method = RequestMethod.POST)
    @ResponseBody
    public DeferredResult<Map<String, String>> uploadFile(@RequestParam(value = "data") MultipartFile file,
                                                          @RequestParam Map<String, String> map, HttpServletRequest request,
                                                          HttpServletResponse response) {

        //TODO 权限判断
        //TODO 获取到当前用户

        System.out.println("请求进入...............");
        DeferredResult<Map<String, String>> deferredResult = new DeferredResult<>();

        String ip = request.getLocalAddr();
        int port = request.getLocalPort();
        String cip = request.getRemoteAddr();

        String fileName = map.get("fileName");
        String totalMD5 = map.get("totalMD5");
        int total = Integer.parseInt(map.get("total"));
        int index = Integer.parseInt(map.get("index"));
        System.out.println("    =====================  " + index);
        String md5 = map.get("md5");
        long uploadTime = Long.parseLong(map.get("uploadTime"));

        Jedis jedis = JedisUtils.getJedis();
        String fileKey = cip + "_" + fileName + "_" + totalMD5 + "_" + uploadTime;
        jedis.sadd(FILE_LIST, fileKey);
        // 去redis查询MD5
        Boolean exists = jedis.sismember(MD5S, totalMD5);
        if (exists) {
            //TODO 文件存入数据库 //入库前先查下这个文件是否已经存过,是则不存了
            try {
                String uri = jedis.hget(totalMD5, "uri");
                BufferedWriter writer = new BufferedWriter(new FileWriter("f:/stor", true));
                writer.append(uri);
                writer.append("  ");
                writer.append(fileKey);
                writer.append("  ");
                writer.append(totalMD5);
                writer.newLine();
                writer.close();
                jedis.srem(FILE_LIST, fileKey);
                jedis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //TODO 告诉浏览器 上传完成, 打断剩下的片段上传
            map.put("p", "1");
            deferredResult.setResult(map);

        } else {
            //TODO 去数据库查
            if (true) {
                //数据库查询不到MD5 则存入fdfs
                new Thread() {
                    @Override
                    public void run() {
                        handle(deferredResult, file, map, ip, port, cip);
                    }
                }.start();
            } else {
                //文件相关信息存入数据库;并告诉浏览器进度为100% 打断剩余的上传
                //TODO

                deferredResult.setResult(map);
            }
        }

        System.out.println("请求释放..............");
        return deferredResult;
    }

    @RequestMapping(value = "handleTask/{fileSliceKey}/{total}/{index}", method = RequestMethod.GET)
    @ResponseBody
    public DeferredResult<String> handleTask(@PathVariable("fileSliceKey") String fileSliceKey,
                                             @PathVariable("total") int total,
                                             @PathVariable("index") int index,
                                             HttpServletRequest request,
                                             HttpServletResponse response) {
        DeferredResult<String> deferredResult = new DeferredResult<>();

        new Thread() {
            @Override
            public void run() {
                Jedis jedis = JedisUtils.getJedis();
                long status = jedis.hsetnx(fileSliceKey, index + "", "ing");
                if (status == 1) {
                    TrackerServer trackerServer = fdConnectionPool.getConn();
                    StorageClient storageClient = new StorageClient(trackerServer, null);

                    String file = DISK_TMP + fileSliceKey + "_" + index;
                    //追加文件片
                    String hget = jedis.hget(fileSliceKey, "uri");
                    int indexOf = hget.indexOf("/");
                    try {
                        storageClient.append_file(hget.substring(0, indexOf), hget.substring(indexOf + 1), file);
                        jedis.hset(fileSliceKey, index + "", "finish");
                        int hlen = jedis.hlen(fileSliceKey).intValue();
                        if (hlen - 1 == total) {
                            jedis.sadd(MD5S, fileSliceKey);
                            Set<String> files = jedis.smembers(FILE_LIST);
                            for (String f : files) {
                                if (f.contains(fileSliceKey)) {
                                    //TODO 存入数据库
                                    try {
                                        BufferedWriter writer = new BufferedWriter(new FileWriter("f:/stor", true));
                                        writer.append(hget);
                                        writer.append("  ");
                                        writer.append(f);
                                        writer.append("  ");
                                        writer.append(fileSliceKey);
                                        writer.newLine();
                                        writer.close();
                                        //并从Redis清理掉
                                        jedis.srem(FILE_LIST, f);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }


                        }
                        deferredResult.setResult("200");
                        fdConnectionPool.recycle(trackerServer);
                    } catch (Exception e) {
                        deferredResult.setResult("400");
                        e.printStackTrace();
                    }
                }
                jedis.close();
            }
        }.start();
        return deferredResult;
    }

    public void handle(DeferredResult<Map<String, String>> deferredResult, MultipartFile file, Map<String, String> map, String ip, int port, String cip) {
        System.out.println("异步处理请求..........");

        String fileName = map.get("fileName");
        String totalMD5 = map.get("totalMD5");
        int total = Integer.parseInt(map.get("total"));
        int index = Integer.parseInt(map.get("index"));
        String md5 = map.get("md5");
        long uploadTime = Long.parseLong(map.get("uploadTime"));

        try {
            //校验文件是否完整
            boolean mv = FileVerifyUtils.md5VerifyByte(file.getBytes(), md5);
            if (mv) {
                Jedis jedis = JedisUtils.getJedis();
                TrackerServer trackerServer = fdConnectionPool.getConn();
                StorageClient storageClient = new StorageClient(trackerServer, null);

                //文件key
                String fileKey = cip + "_" + fileName + "_" + totalMD5 + "_" + uploadTime;

                //已经完成或正在进行的文件片段的记录在redis上的key
                String fileSliceKey = totalMD5;

                //暂存磁盘的的片段的记录在redis上的key
                String fileTmp = fileSliceKey + "_tmp";

                //文件信息存入redis
                jedis.hmset(fileKey, map);

                jedis.sadd(FILE_LIST, fileKey);

                if (index != 1) {
                    //判断 文件此片的上一片是否存在
                    if (jedis.hexists(fileSliceKey, (index - 1) + "")) {
                        //状态为正在进行 则等待
                        while ("ing".equals(jedis.hget(fileSliceKey, (index - 1) + ""))) {
                        }
                        long isSuccess = jedis.hsetnx(fileSliceKey, index + "", "ing");
                        if (isSuccess == 1) {
                            //追加文件片
                            String group_fileRoute = jedis.hget(fileSliceKey, "uri");
                            int indexOf = group_fileRoute.indexOf("/");
                            storageClient.append_file(group_fileRoute.substring(0, indexOf), group_fileRoute.substring(indexOf + 1), file.getBytes());
                            jedis.hset(fileSliceKey, index + "", "finish");
                            int hlen = jedis.hlen(fileSliceKey).intValue();
                            //若所有分片都已上传完成
                            if (hlen - 1 == total) {
                                jedis.sadd(MD5S, fileSliceKey);
                                Set<String> files = jedis.smembers(FILE_LIST);
                                for (String f : files) {
                                    if (f.contains(fileSliceKey)) {
                                        //TODO 存入数据库
                                        try {
                                            BufferedWriter writer = new BufferedWriter(new FileWriter("f:/stor", true));
                                            writer.append(group_fileRoute);
                                            writer.append("  ");
                                            writer.append(f);
                                            writer.append("  ");
                                            writer.append(totalMD5);
                                            writer.newLine();
                                            writer.close();
                                            //并从Redis清理掉
                                            jedis.srem(FILE_LIST, f);
                                            jedis.del(f);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                            } else {

                                backwardsSlice(fileSliceKey, fileTmp, jedis, index, total);
                            }

                        }
                    } else {
                        long hsetnx = jedis.hsetnx(fileTmp, index + "", ip + ":" + port);
                        if (hsetnx == 1) {
                            //暂存到本地
                            file.transferTo(new File(DISK_TMP + fileSliceKey + "_" + index));
                        }
                    }
                } else {
                    //若是第一片 则存入fastdfs 返回地址
                    long hsetnx = jedis.hsetnx(fileSliceKey, index + "", "ing");
                    if (hsetnx == 1) {
                        String[] strings = storageClient.upload_appender_file(file.getBytes(), null, null);
                        String group = strings[0];
                        String fileRoute = strings[1];
                        jedis.hset(fileSliceKey, index + "", "finish");
                        jedis.hset(fileSliceKey, "uri", group + "/" + fileRoute);

                        int hlen = jedis.hlen(fileSliceKey).intValue();
                        //若所有分片都已上传完成
                        if (hlen - 1 == total) {
                            jedis.sadd(MD5S, fileSliceKey);
                            Set<String> files = jedis.smembers(FILE_LIST);
                            for (String f : files) {
                                if (f.contains(fileSliceKey)) {
                                    //TODO 存入数据库
                                    try {
                                        BufferedWriter writer = new BufferedWriter(new FileWriter("f:/stor", true));
                                        writer.append(group + "/" + fileRoute);
                                        writer.append("  ");
                                        writer.append(f);
                                        writer.append("  ");
                                        writer.append(totalMD5);
                                        writer.newLine();
                                        writer.close();
                                        //并从Redis清理掉
                                        jedis.srem(FILE_LIST, f);
                                        jedis.del(f);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } else {

                            backwardsSlice(fileSliceKey, fileTmp, jedis, index, total);
                        }

                    }
                }
                jedis.close();
                fdConnectionPool.recycle(trackerServer);

            } else {
                //告诉浏览器此片段后台MD5校验不通过,请重新传当前片段
                //deferredResult.setResult()
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        deferredResult.setResult(map);
    }


    private void forwardSlice(String fileSliceKey, String fileTmp, Jedis jedis, int index) {

        //判断前一个片段是否临时缓存在磁盘 且 前前一个片段已经完成或正在存储
        if (jedis.hexists(fileTmp, (index - 1) + "") && jedis.hexists(fileSliceKey, (index - 2) + "")) {
            //去片段物理所在机,追加片段
            String ipPort = jedis.hget(fileTmp, (index - 1) + "");
            HttpClient httpClient = HttpClientBuilder.create().build();
            String url = ipPort + "/test3/uploadFile/" + fileSliceKey + "_" + (index - 1);
            HttpGet get = new HttpGet(url);
            try {
                HttpResponse execute = httpClient.execute(get);
                int statusCode = execute.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    if (jedis.hexists(fileTmp, index + "")) {
                        forwardSlice(fileSliceKey, fileTmp, jedis, index + 1);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (index > 3) {
                forwardSlice(fileSliceKey, fileTmp, jedis, index - 1);

            }
        }
    }

    private void backwardsSlice(String fileSliceKey, String fileTmp, Jedis jedis, int index, int total) {
        if (jedis.hexists(fileTmp, (index + 1) + "")) {
            //去片段物理所在机,追加片段
            String ipPort = jedis.hget(fileTmp, (index + 1) + "");
            HttpClient httpClient = HttpClientBuilder.create().build();
            String url = "http://" + ipPort + "/test3/handleTask/" + fileSliceKey + "/" + total + "/" + (index + 1);
            HttpGet get = new HttpGet(url);
            try {
                HttpResponse httpResponse = httpClient.execute(get);
                String statusCode = EntityUtils.toString(httpResponse.getEntity());
                if ("200".equals(statusCode)) {
                    if (jedis.hexists(fileTmp, (index + 2) + "")) {
                        backwardsSlice(fileSliceKey, fileTmp, jedis, index + 1, total);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @RequestMapping(value = "test")
    @ResponseBody
    public DeferredResult<String> testHttpClient(HttpServletRequest request,
                                                 HttpServletResponse response) {
        DeferredResult<String> deferredResult = new DeferredResult<>();

        HttpClient httpClient = HttpClientBuilder.create().build();
        String url = "http://127.0.0.1:8080/test3/test3/1";
        HttpGet get = new HttpGet(url);
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(get);
            String statusCode = EntityUtils.toString(httpResponse.getEntity());
            if ("200".equals(statusCode)) {
                System.out.println(statusCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        deferredResult.setResult("200");
        return deferredResult;
    }

    @RequestMapping(value = "test3/{index}")
    @ResponseBody
    public DeferredResult<String> test3(HttpServletRequest request,
                                        HttpServletResponse response, @PathVariable("index") String index) {
        DeferredResult<String> deferredResult = new DeferredResult<>();
        System.out.println(index);
        deferredResult.setResult("200");
        return deferredResult;
    }


}
