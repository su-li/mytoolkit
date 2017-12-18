package com.bluedon;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author HD
 * @date. 2017/12/14
 */
public class Client {
    public static void main(String[] args) throws IOException {
        HttpClient httpclient = null;
        try {
            String url = "http://localhost:8080/test/uploadFile";
            httpclient = HttpClientBuilder.create().build();
            HttpPost httppost = new HttpPost(url);

            String filepath = "f://";
            String filename1 = "1.jpg";
            String filename2 = "2.jpg";
            String filename3 = "SLES-11-SP4-DVD-x86_64-GM-DVD1.iso";
            File file1 = new File(filepath + File.separator + filename1);
            File file2 = new File(filepath + File.separator + filename2);
             File file3 = new File(filepath + File.separator + filename3);

            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
            multipartEntityBuilder.addBinaryBody("file", file1);
            multipartEntityBuilder.addBinaryBody("file", file2);
             multipartEntityBuilder.addBinaryBody("file", file3);
            multipartEntityBuilder.addTextBody("id", "110");

            /*reqEntity.addPart("file1", bin);//file1为请求后台的File upload;属性
            reqEntity.addPart("file2", bin2);//file2为请求后台的File upload;属性
            reqEntity.addPart("filename1", comment);//filename1为请求后台的普通参数;属性*/

            httppost.setEntity(multipartEntityBuilder.build());

            HttpResponse response = httpclient.execute(httppost);
            int statusCode = response.getStatusLine().getStatusCode();


            if (statusCode == HttpStatus.SC_OK) {
                System.out.println("服务器正常响应.....");
                HttpEntity resEntity = response.getEntity();
                System.out.println(EntityUtils.toString(resEntity));//httpclient自带的工具类读取返回数据

                System.out.println(resEntity.getContent());

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


}
