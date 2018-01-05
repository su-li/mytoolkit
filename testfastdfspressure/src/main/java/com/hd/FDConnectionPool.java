package com.hd;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author HD
 * @date. 2017/12/21
 */
public class FDConnectionPool {
    private LinkedBlockingQueue<TrackerServer> idleConnectionPool;
    private static final String CLIENT_CONFIG_FILE = "client.conf";
    private static FDConnectionPool fdConnectionPool;
    private static int totalSize;

    static {
        try {
            ClientGlobal.init(CLIENT_CONFIG_FILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static FDConnectionPool init(int total) {
        if (fdConnectionPool == null) {
            fdConnectionPool = new FDConnectionPool(total);
        }
        return fdConnectionPool;
    }

    private FDConnectionPool(int total) {
        totalSize = total;
        idleConnectionPool = new LinkedBlockingQueue<>(total);
        for (int i = 0; i < total; i++) {
            addElement(idleConnectionPool);
        }
    }

    private void addElement(LinkedBlockingQueue<TrackerServer> idleConnectionPool) {
        try {
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getConnection();
            org.csource.fastdfs.ProtoCommon.activeTest(trackerServer
                    .getSocket());
            synchronized (this) {
                idleConnectionPool.offer(trackerServer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TrackerServer getConn() {
        TrackerServer trackerServer = idleConnectionPool.poll();
        if (trackerServer == null) {
            try {
                addElement(idleConnectionPool);
                trackerServer = idleConnectionPool.poll(300, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return trackerServer;
    }

    public void recycle(TrackerServer trackerServer) {
        if (idleConnectionPool.size() < totalSize) {
            try {
                synchronized (this) {
                    idleConnectionPool.offer(trackerServer);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                trackerServer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
