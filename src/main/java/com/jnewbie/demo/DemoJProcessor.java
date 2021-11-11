package com.jnewbie.demo;


import com.jnewbie.JPage;
import com.jnewbie.JProcessor;

import java.util.List;

/**
 * @program: jnewbie
 * @description: 测试处理器实现类
 * @author: pingc
 * @create: 2021-11-08 11:33
 **/
public class DemoJProcessor  extends JProcessor {
    @Override
    public JPage process(JPage jPage) {
        System.out.println("process线程："+Thread.currentThread().getName());
        String url = jPage.getUrl();
        //判断url是不是搜索页面
        if(url.contains("s?q")){
            //获取搜索结果url,写个替换域名进去,因为出来的url没有带域名/book/28659/
            List<String> urls = jPage.xpath("//h4[@class='bookname']/a/@href").replaceFirst("/","https://www.biquge7.com/").getAll();
            //把结果加入到爬取列表
            jPage.addGoUrls(urls);
        }
            //判断url是不是详情页面
        else  if(url.contains("book") && !url.contains("html")) {
                //获取书名
            String name = jPage.xpath("//h1/text()").getAll().get(0);
            //打印书名
            System.out.println(name);
            //获取章节列表url,写个替换域名进去因为出来的url没有带域名/book/28659/
                List<String> urls = jPage.xpath("//div[@class='listmain']//dd/a/@href").replaceFirst("/","https://www.biquge7.com/").getAll();
                //把章节列表url,加入到爬取列表
                jPage.addGoUrls(urls);

        }
        else if(url.contains("book") && url.contains("html")){

            //获取章节标题
            String title = jPage.xpath("//span[@class=\"title\"]/text()").getAll().get(0);
            List<String> urls = jPage.xpath("//div[@id='chaptercontent']/text()").getAll();
            //打印小说正文
//            System.out.println(title);
//            System.out.println(urls);




        }


        return jPage;
    }
}
