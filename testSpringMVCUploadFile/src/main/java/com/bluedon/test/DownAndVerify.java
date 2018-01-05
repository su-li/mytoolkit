package com.bluedon.test;

import com.bluedon.FDConnectionPool;
import com.bluedon.JedisUtils;
import com.bluedon.utils.FileVerifyUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.util.Assert;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.Set;

/**
 * @author HD
 * @date. 2017/12/27
 */
public class DownAndVerify {
    public static void main(String[] args) {

        FDConnectionPool fdConnectionPool = FDConnectionPool.init(50);
        Jedis jedis = JedisUtils.getJedis();
        Set<String> files = jedis.smembers("files");
        for (String s : files) {
            String code = s;
            String group_Fileroute = jedis.hget(s, "uri");

            TrackerServer trackerServer = fdConnectionPool.getConn();
            StorageClient storageClient = new StorageClient(trackerServer, null);
            String group = group_Fileroute.substring(0, group_Fileroute.indexOf("/"));
            String fileRoute = group_Fileroute.substring(group_Fileroute.indexOf("/") + 1);
            try {
                byte[] bytes = storageClient.download_file(group, fileRoute);
                boolean flag = FileVerifyUtils.md5VerifyByte(bytes, code);
                System.out.println(flag);
                storageClient.delete_file(group, fileRoute);
                fdConnectionPool.recycle(trackerServer);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (MyException e) {
                e.printStackTrace();
            }

        }


    }
}
