package com.jnewbie.demo;




import com.jnewbie.*;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * @program: jnewbie
 * @description:测试启动
 * @author: pingc
 * @create: 2021-11-08 11:20
 **/
public class Demo {

    public static void main(String[] args)  {
//        ;
//            //创建请求客户端
//            JHtml jHtml = new JHtml();
//            //创建数据处理器
//            DemoJProcessor demoJProcessor = new DemoJProcessor();
//            //将客户端加入到处理器,设置开始url,设置启动线程
//            demoJProcessor.setJHtml(jHtml)
//                    .setGetMethod(JHtml.GET)
//                    .setUrl("https://www.biquge7.com/s?q=123")
//                    .start(5);
////        test1();

        //创建请求客户端
        JHtml jHtml = new JHtml();
        JProxy jProxy = new JProxy("120.92.155.74",16817);

//        https://2021.ipchaxun.com/
        JPage jPage = jHtml.get("http://ip111.cn/");
        System.out.println(
                jPage
        );
    }



}
