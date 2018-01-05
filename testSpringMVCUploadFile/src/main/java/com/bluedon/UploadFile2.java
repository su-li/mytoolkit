package com.bluedon;

import com.bluedon.utils.FileVerifyUtils;
import com.hd.ThreadPoolUtil;
import org.csource.fastdfs.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/*
 *@author HD
 * @date. 2017/12/20
 */
@Controller
@RequestMapping(value = "test2/")
public class UploadFile2 {
    private ThreadPoolUtil pool = ThreadPoolUtil.init(100);
    FDConnectionPool fdConnectionPool = FDConnectionPool.init(50);


    @RequestMapping(value = "uploadFile", method = RequestMethod.POST)
    @ResponseBody
    public DeferredResult<Map<String, String>> uploadFile(@RequestParam(value = "data") MultipartFile file,
                                                          @RequestParam Map<String, String> map,
                                                          HttpServletRequest request,
                                                          HttpServletResponse response) {

        //TODO 权限判断
        //TODO 获取到当前用户

        System.out.println("请求进入...............");
        DeferredResult<Map<String, String>> deferredResult = new DeferredResult<>();

        //TODO  去数据库查询MD5

        if (true) {
            //数据库查询不到MD5 则存入fdfs
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

    public void handle(DeferredResult<Map<String, String>> deferredResult, MultipartFile file, Map<String, String> map) {
        System.out.println("异步处理请求..........");

        String fileName = map.get("fileName");
        String totalMD5 = map.get("totalMD5");
        int total = Integer.parseInt(map.get("total"));
        int index = Integer.parseInt(map.get("index"));
        String md5 = map.get("md5");
        int uploadTime = Integer.parseInt(map.get("uploadTime"));

        try {
            //校验文件是否完整
            boolean mv = FileVerifyUtils.md5VerifyByte(file.getBytes(), md5);
            if (mv) {
                Jedis jedis = JedisUtils.getJedis();
                TrackerServer trackerServer = fdConnectionPool.getConn();
                StorageClient storageClient = new StorageClient(trackerServer, null);
                //文件key
                String fileKey = fileName + "_" + totalMD5;
                //文件分片key
                String fileSliceKey = totalMD5 + "_" + total;

                //文件信息存入redis
                jedis.hmset(fileKey + "_" + uploadTime, map);
                synchronized (this) {
                    //判断 文件某一片是否存在
                    Boolean flag = jedis.hexists(fileSliceKey, index + "");
                    if (!flag) {
                        //若是第一片 则存入fastdfs 返回地址
                        if (index == 1) {
                            String[] strings = storageClient.upload_appender_file(file.getBytes(), null, null);
                            String group = strings[0];
                            String fileRoute = strings[1];
                            jedis.hset(fileSliceKey, "1", group + "/" + fileRoute);

                            if (index == total) {
                                //创建对象文件

                            }
                        } else {
                            //追加文件片
                            String hget = jedis.hget(fileSliceKey, "1");
                            int indexOf = hget.indexOf("/");
                            storageClient.append_file(hget.substring(0, indexOf), hget.substring(indexOf + 1), file.getBytes());
                            jedis.hset(fileSliceKey, index + "", "");
                            if (index == total) {
                                //创建对象文件

                            }
                        }
                    }
                    jedis.close();
                    fdConnectionPool.recycle(trackerServer);
                }

            } else {
                //告诉浏览器此片段后台MD5校验不通过,请重新传
                //deferredResult.setResult()
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        deferredResult.setResult(map);
    }
}
