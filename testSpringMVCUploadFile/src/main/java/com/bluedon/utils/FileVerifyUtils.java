package com.bluedon.utils;


import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author HD
 * @date. 2017/9/20
 */
public class FileVerifyUtils {


    /**
     * @param inputStream 文件流
     * @param code        文件MD5值
     * @return 布尔值
     */
    public static boolean md5VerifyStream(InputStream inputStream, String code) {

        StringBuffer md5 = new StringBuffer();
        MessageDigest md = null;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1000000);
        byte[] dataBytes = new byte[1024];
        int nread = 0;
        try {
            while ((nread = inputStream.read(dataBytes)) != -1) {
                outputStream.write(dataBytes, 0, nread);
            }
            byte[] bytes = outputStream.toByteArray();
            inputStream.close();
            outputStream.close();
            return md5VerifyByte(bytes, code);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 校验字节数组的MD5值
     *
     * @param bytes 文件字节数组
     * @param code  MD5值
     * @return 布尔值
     */
    public static boolean md5VerifyByte(byte[] bytes, String code) {
        return generateMD5(bytes).equals(code);
    }

    /**
     * @param bytes 待处理的字节数组
     * @return {@link String}  MD5值
     */
    public static String generateMD5(byte[] bytes) {
        StringBuffer md5 = new StringBuffer();
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] digest = md.digest(bytes);
        for (int i = 0; i < digest.length; i++) {
            md5.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
        }
        return md5.toString();
    }

    public static void main(String[] args) throws Exception {
        FileInputStream inputStream = new FileInputStream(new File("F:\\CentOS-6.5-x86_64-minimal.iso"));
        final boolean flag = md5VerifyStream(inputStream, "0d9dc37b5dd4befa1c440d2174e88a87");
        System.out.println(flag);
    }
}
