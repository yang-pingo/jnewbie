package com.jnewbie.demo;

import com.jnewbie.JHtml;
/**
 * @program: jnewbie
 * @description:测试启动
 * @author: pingc
 * @create: 2021-11-08 11:20
 **/
public class Demo {
    public static void main(String[] args)  {
            //创建请求客户端
            JHtml jHtml = new JHtml();
            //创建数据处理器
            DemoJProcessor demoJProcessor = new DemoJProcessor();
            //将客户端加入到处理器,设置开始url,设置启动线程
            demoJProcessor.setJHtml(jHtml)
                    .setGetMethod(JHtml.JGET)
                    .setUrl("https://www.biquge7.com/s?q=斗破苍穹")
                    .start(1);
    }
}
