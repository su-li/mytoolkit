package com.hd;

import org.csource.common.MyException;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author HD
 * @date. 2018/1/12
 */
public class TestDownload {
    static int num = 100;
    static ThreadPoolUtil pool = ThreadPoolUtil.init(num);
    static FDConnectionPool fdConnectionPool = FDConnectionPool.init(num);

    public static void main(String[] args) throws Exception {
        List<String> files = new ArrayList<>();
        File file = new File("/root/stor/");
        File[] listFiles = file.listFiles();
        for (File f : listFiles) {
            String line = "";
            BufferedReader reader = new BufferedReader(new FileReader(f));
            while ((line = reader.readLine()) != null) {
                files.add(line);
            }
            reader.close();
        }

        long start = System.currentTimeMillis();
        System.out.println("====================开始下载");

        int total = files.size() / num;
        for (int i = 1; i <= num; i++) {
            int finalI = i;

            pool.execute(new Thread() {
                @Override
                public void run() {
                    TrackerServer trackerServer = fdConnectionPool.getConn();
                    StorageClient storageClient = new StorageClient(trackerServer, null);
                    for (int j = (finalI - 1) * total; j < finalI * total; j++) {

                        String gr = files.get(j);
                        System.out.println(gr);
                        try {
                            byte[] bytes = storageClient.download_file(gr.substring(0, gr.indexOf("/")), gr.substring(gr.indexOf("/") + 1));
                            if (bytes == null) {
                                System.out.println("异常");
                                System.exit(1);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            });
        }
        pool.elapsedTime(start);

    }
}
