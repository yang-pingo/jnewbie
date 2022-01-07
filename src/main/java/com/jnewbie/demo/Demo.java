package com.jnewbie.demo;
import com.alibaba.fastjson.JSONObject;
import com.jnewbie.JHtml;
import com.jnewbie.StringToUrl;
import com.jnewbie.request.JHeader;
import com.jnewbie.request.JPage;
import jdk.nashorn.api.scripting.ScriptUtils;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @program: jnewbie
 * @description:测试启动
 * @author: pingc
 * @create: 2021-11-08 11:20
 **/
public class Demo {
    public static void main(String[] args) throws UnsupportedEncodingException {
        //创建请求客户端
        JHtml jHtml = new JHtml();
        DemoJProcessor demoJProcessor = new DemoJProcessor();
        //将客户端加入到处理器,设置开始url,设置启动线程
        long startTime = System.currentTimeMillis();    //获取开始时间

        demoJProcessor.setJHtml(jHtml)
                .setGetMethod(JHtml.GET)
                .setUrl("https://www.biquge7.com/s?q=古代的舒心日子")
                .start(10);
        demoJProcessor.setJHtml(jHtml)
                .setGetMethod(JHtml.GET)
                .setUrl("https://www.biquge7.com/s?q=古代的舒心日子")
                .start(10);
        long endTime = System.currentTimeMillis();    //获取结束时间

        System.out.println("程序运行时间：" + (endTime - startTime) + "ms");    //输出程序运行时间

    }

}





