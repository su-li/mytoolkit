package com.bluedon;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.*;

/**
 * @author HD
 * @date. 2017/12/27
 */
public class Test1 {
    public static void main(String[] args) {
        HttpClient httpclient = null;
        String url = "http://localhost:8080/test3/test";
        httpclient = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(url);

        try {
            HttpResponse httpResponse = httpclient.execute(get);
            HttpEntity entity = httpResponse.getEntity();

            System.out.println(EntityUtils.toString(entity));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
