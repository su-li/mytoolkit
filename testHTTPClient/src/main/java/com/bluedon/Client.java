package com.bluedon;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * @author HD
 * @date. 2017/12/14
 */
public class Client {

    public static void main(String[] args) throws Exception {
        // downLoadFile();
        createBucket();
    }

    public static void uploadFile(String[] args) throws IOException {
        HttpClient httpclient = null;
        try {
            String url = "http://localhost:8080/bhsys-webapp/oss/outside/upload.html";
            httpclient = HttpClientBuilder.create().build();
            HttpPost httppost = new HttpPost(url);

            String filepath = "f://";

            String filename1 = "1.jpg";
            File file1 = new File(filepath + File.separator + filename1);

            String filename2 = "2.jpg";
            File file2 = new File(filepath + File.separator + filename2);

            String filename3 = "SLES-11-SP4-DVD-x86_64-GM-DVD1.iso";
            File file3 = new File(filepath + File.separator + filename3);

            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            multipartEntityBuilder.addBinaryBody("file", file1);
            // multipartEntityBuilder.addBinaryBody("file", file2);
            // multipartEntityBuilder.addBinaryBody("file", file3);
            multipartEntityBuilder.addTextBody("bucketIdentifier", "001");
            multipartEntityBuilder.addTextBody("username", "13000000001");
            multipartEntityBuilder.addTextBody("password", "000000");

            /*reqEntity.addPart("file1", bin);//file1为请求后台的File upload;属性
            reqEntity.addPart("file2", bin2);//file2为请求后台的File upload;属性
            reqEntity.addPart("filename1", comment);//filename1为请求后台的普通参数;属性*/

            httppost.setEntity(multipartEntityBuilder.build());

            HttpResponse response = httpclient.execute(httppost);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == HttpStatus.SC_OK) {
                System.out.println("服务器正常响应.....");
                HttpEntity resEntity = response.getEntity();
                //httpclient自带的工具类读取返回数据
                System.out.println(EntityUtils.toString(resEntity));
                EntityUtils.consume(resEntity);
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                httpclient.getConnectionManager().shutdown();
            } catch (Exception ignore) {

            }
        }

    }

    public static void downLoadFile() throws Exception {
        HttpClient httpclient = null;
        String url = "http://localhost:8080/bhsys-webapp/oss/outside/download.html/001/1";
        httpclient = HttpClientBuilder.create().build();
        HttpPost httppost = new HttpPost(url);
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.addTextBody("username", "13000000001");
        multipartEntityBuilder.addTextBody("password", "000000");
        httppost.setEntity(multipartEntityBuilder.build());
        HttpResponse response = httpclient.execute(httppost);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_OK) {
            System.out.println("服务器正常响应.....");
            HttpEntity resEntity = response.getEntity();
            resEntity.writeTo(new FileOutputStream("f://AAAAAA"));
            EntityUtils.consume(resEntity);
        }
        httpclient.getConnectionManager().shutdown();

    }


    public static void createBucket() throws Exception {
        HttpClient httpclient = null;
        String url = "http://localhost:8080/bhsys-webapp/oss/outside/createBucket";
        httpclient = HttpClientBuilder.create().build();
        HttpPost httppost = new HttpPost(url);
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.addTextBody("username", "13000000001");
        multipartEntityBuilder.addTextBody("password", "000000");
        ContentType contentType = ContentType.create("text/plain", Charset.forName("UTF-8"));
        multipartEntityBuilder.addTextBody("name", "桶one", contentType);
        multipartEntityBuilder.addTextBody("scope", "1");
        httppost.setEntity(multipartEntityBuilder.build());
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();

        Map map = JSONObject.parseObject(EntityUtils.toString(resEntity), Map.class);
        EntityUtils.consume(resEntity);

        httpclient.getConnectionManager().shutdown();


    }
}
