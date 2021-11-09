package com.jnewbie.demo;




import com.jnewbie.HttpDriverManager;
import com.jnewbie.JHtml;

import java.io.IOException;

/**
 * @program: jnewbie
 * @description:测试启动
 * @author: pingc
 * @create: 2021-11-08 11:20
 **/
public class Demo {

    public static void main(String[] args)  {
        HttpDriverManager.setDriverPath("C:\\Users\\YRJ\\Desktop\\chromedriver_win32\\chromedriver.exe");
        HttpDriverManager.setDriverName("driverName");
        //创建请求客户端
        JHtml jHtml = new JHtml();
        //创建数据处理器
        DemoJProcessor demoJProcessor = new DemoJProcessor();
        //将客户端加入到处理器,设置开始url,设置启动线程
        demoJProcessor.setJHtml(jHtml)
                .setGetMethod(3)
                .setUrl("https://www.biquge7.com/s?q=123")
                .start(5);
    }


}
