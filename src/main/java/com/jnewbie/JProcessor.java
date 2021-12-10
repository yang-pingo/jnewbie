package com.jnewbie;

import com.google.common.base.Verify;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.jnewbie.request.JPage;
import net.bytebuddy.asm.Advice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @program: jnewbie
 * @description: 数据处理器
 * @author: pingc
 * @create: 2021-11-06 10:31
 **/
public abstract class JProcessor implements Runnable {
    public static final ExecutorService executorService = Executors.newCachedThreadPool();
    Integer T = 0;
    JHtml jHtml;
    List<String> urls = new ArrayList<>();
    String url;
    volatile JPage jPage;
    //使用哪种get
    volatile boolean is = true;
    Integer getMethod = 1;
    Integer interval = 100;
    boolean filter = true;
    private static final Logger log = LoggerFactory.getLogger(JProcessor.class);
    public JProcessor(){}


    private  static BloomFilter<String> Bloomfilter = BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), 10000000,0.001);

    public static void setBloomfilter(Integer size,float erro) {
        Bloomfilter = BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), size,erro);
    }

    public JProcessor addUrl(String url) {
        this.urls.add(url);
        return this;
    }
    public JProcessor setUrl(String url) {
        this.url=url;
        return this;
    }
    public JProcessor setFilter(Boolean filter) {
        this.filter=filter;
        return this;
    }
    public JProcessor setInterval(Integer interval) {
        this.interval = interval;
        return this;
    }

    public JProcessor setJHtml(JHtml jHtml) {
        this.jHtml = jHtml;
        return this;
    }

    public JHtml getJHtml() {
        return this.jHtml;
    }

    public JProcessor setGetMethod(Integer getMethod) {
        this.getMethod = getMethod;
        return this;
    }
    //页面信息处理
    public abstract JPage process(JPage jPage);


    @Override
    public void run() {
        if(is){
        switch (this.getMethod) {
            case 1:
                jPage = jHtml.get(url);
                break;
            case 2:
                jPage = jHtml.hGet(url);
                break;
            case 3:
                jPage = jHtml.pGet(url);
                break;
            case 4:
                jPage = jHtml.cGet(url);
                break;
        }
        jPage = process(jPage);
        is =false;
        if (this.urls.size() != 0) {
            jPage.addGoUrls(this.urls);
            this.urls.clear();
        }
        if (T != 0) {
            List<Future<?>> list = new ArrayList<>();
            for (int z = 0; z < T; z++) {
                Future<?> submit = executorService.submit(this,"");
                list.add(submit);
            }
            for (Future<?> future : list) {
                try {
                    future.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            }else {
                goRun(jPage);
            }
        }else {
            goRun(jPage);
        }

    }
    private void goRun(JPage jPage)  {
        for (String url : jPage.getGoUrl()) {

            try {
                if (filter) {
                    synchronized (Bloomfilter) {
                        if (!Bloomfilter.mightContain(url)) {
                            Bloomfilter.put(url);
                        } else {
                            continue;
                        }
                    }
                }
                if (interval != null) {
                    try {
                        Thread.sleep(interval);
                    } catch (Exception e) {
                        log.error("爬取间隔错误");
                    }
                }
                JPage j = null;
                switch (this.getMethod) {
                    case 1:
                        j = jHtml.get(url);
                        break;
                    case 2:
                        j = jHtml.hGet(url);
                        break;
                    case 3:
                        j = jHtml.pGet(url);
                        break;
                    case 4:
                        j = jHtml.cGet(url);
                        break;
                }
                j.setTagAll(jPage.getTagAll());
                JPage process = process(j);
                goRun(process);

            } catch (Exception e) {
                StackTraceElement stackTraceElement = e.getStackTrace()[0];
                log.error("错误:" + stackTraceElement.getFileName() + ",方法:" + stackTraceElement.getMethodName() + "，行:" + stackTraceElement.getLineNumber() + "，错误信息：" + e.toString());
            }
        }

        }


    public void start(Integer i){
        this.T = i;
        is = true;
        run();
    }
}
