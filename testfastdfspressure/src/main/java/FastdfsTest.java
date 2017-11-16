
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.*;

/**
 * @author HD
 * @date 2017/11/8
 */
public class FastdfsTest {

    private static TrackerClient trackerClient;
    private static TrackerServer trackerServer;
    private static StorageClient storageClient;
    public static final String CLIENT_CONFIG_FILE = "client.conf";

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
    }

    public static void main(String[] args) throws Exception {
        Scanner scan = new Scanner(System.in);
        System.out.println("请输入要测试的每条数据的大小,单位为kb:");
        int cellSize = scan.nextInt();
        System.out.println("请输入要测试的数据条数:");
        int numbers = scan.nextInt();

        Map<String, String> paths = new HashMap<>();
        List<byte[]> list = generateTestData(cellSize, numbers);

        long start = System.currentTimeMillis();
        for (byte[] bytes : list) {
            String[] uploadResults = storageClient.upload_file(bytes, "txt", new NameValuePair[0]);
            String groupName = uploadResults[0];
            String remoteFileName = uploadResults[1];
            System.out.println(groupName + "/" + remoteFileName);
            paths.put(remoteFileName, groupName);
        }
        long end = System.currentTimeMillis();
        System.out.println(String.format("============上传使用时间:%s", end - start));
        Set<Map.Entry<String, String>> entries = paths.entrySet();
        Iterator<Map.Entry<String, String>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> next = iterator.next();
            int i = storageClient.delete_file(next.getValue(), next.getKey());
        }
    }

    /**
     * @param cellSize 单条数据大小 单位: kb
     * @param numbers  总共多少条数据
     * @return
     * @throws Exception
     */
    public static List<byte[]> generateTestData(int cellSize, int numbers) throws Exception {
        //生成一个1m的数组
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < 1024 * cellSize; i++) {
            out.write("1".getBytes());
        }
        byte[] bytes = out.toByteArray();
        out.close();

        //模仿100个1m文件
        List<byte[]> list = new ArrayList<>();
        for (int i = 0; i < numbers; i++) {
            list.add(bytes);
        }
        return list;
    }
}
