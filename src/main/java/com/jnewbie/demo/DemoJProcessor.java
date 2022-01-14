package com.jnewbie.demo;


import com.jnewbie.request.JPage;
import com.jnewbie.JProcessor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: jnewbie
 * @description: 测试处理器实现类
 * @author: pingc
 * @create: 2021-11-08 11:33
 **/
public class DemoJProcessor  extends JProcessor {

    private List<String> urls;

    @Override
    public JPage process(JPage jPage) {
        String url = jPage.getUrl();
        //判断url是不是搜索页面
        if(url.contains("s?q")){
            //获取搜索结果url,写个替换域名进去,因为出来的url没有带域名/book/28659/
            List<String> urls = jPage.xpath("//h4[@class='bookname']/a/@href").replaceFirst("/","https://www.biquge7.com/").getAll();
            //把结果加入到爬取列表
            jPage.addGoUrls(urls);
            jPage.addTag("url", jPage.getUrl());

        }
        //判断url是不是详情页面
        else  if(url.contains("book") && !url.contains("html")) {
            //获取书名
            String name = jPage.xpath("//h1/text()").get();
            //打印书名
            //获取章节列表url,写个替换域名进去因为出来的url没有带域名/book/28659/
            List<String>  urls = jPage.xpath("//div[@class='listmain']//dd/a/@href").replaceFirst("/","https://www.biquge7.com/").getAll();
            //把章节列表url,加入到爬取列表
            List<String>  urlsz = new ArrayList<>();
            for (String s : urls) {
                if(!s.trim().equals("javascript:dd_show()")) {
                    urlsz.add(s);
                }
            }
            jPage.addGoUrls(urlsz);

        }
        //判断url是不是章节内容页面
        else if(url.contains("book") && url.contains("html")){
            //获取小说名
            String name = jPage.xpath("//div[@class=\"path wap_none\"]/a[2]/text()").get();
            //获取章节标题
            String title = jPage.xpath("//span[@class=\"title\"]/text()").get();
            //获取正文
            List<String> text = jPage.xpath("//div[@id='chaptercontent']/text()").getAll();
            //打印小说标题
            System.out.println(title);
            //存储的目录
            String path = "C:\\Users\\YRJ\\Desktop\\xs";

            try {
                File file=new File(path+"\\"+name);
                if(!file.exists()){//如果文件夹不存在
                    file.mkdir();//创建文件夹
                }
                //创建流
                BufferedWriter out = new BufferedWriter(new FileWriter(path+"\\"+name+"\\"+title+".txt"));
                //写入到本地
                for (String s : text) {
                    out.write(s);
                    out.newLine();
                }
                out.close();
            } catch (IOException e) {
                System.out.println(e);
            }

        }

        return jPage;

    }
}
