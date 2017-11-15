package org.test;

import cn.classload.MyClassLoad;

import java.util.List;

/**
 * @Author HD
 * @Date 2017/11/3
 */
public class TestMyClassLoad {
    public static void main(String[] args) throws Exception {

        MyClassLoad classLoad = new MyClassLoad();
        //把我们的加载器的父设置为根加载器,那么下面就会用我们的了
        //classLoad = new MyClassLoad(ClassLoader.getSystemClassLoader().getParent());
        Class<?> c1 = Class.forName("com.entity.Person", true, classLoad);
        Object obj = c1.newInstance();
        System.out.println(obj.toString());
        System.out.println(obj.getClass().getClassLoader());

    }
}
