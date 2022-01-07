package com.jnewbie;

import com.google.common.base.Verify;
import com.google.common.collect.Sets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.jnewbie.request.JPage;
import jdk.nashorn.internal.ir.WhileNode;
import net.bytebuddy.asm.Advice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.*;
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
    private static final Logger log = LoggerFactory.getLogger(JProcessor.class);
    volatile List<String> urls = new ArrayList<>();
    volatile JPage jPage;
    volatile boolean is = true;
    boolean filter = true;
    //使用哪种get
    Integer getMethod = 1;
    //线程数
    Integer T = 0;
    JHtml jHtml;
    String url;
    Integer interval = 0;
    Set<String> taskNum = Collections.synchronizedSet(new HashSet<>());

    public JProcessor(){}

    private  static BloomFilter<String> Bloomfilter = BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), 10000000,0.001);

    public static void initializeFilter(){
        Bloomfilter = BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), 10000000,0.001);
    }
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

    public List<String> getUrls() {
        return urls;
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
        urlsAdd(jPage.getGoUrl());
        if (T != 0) {
            List<Future<?>> list = new ArrayList<>();
            for (int z = 0; z < T; z++) {
                Future<?> submit = executorService.submit(this);
                list.add(submit);
            }
            for (Future<?> future : list) {
                    while (!future.isDone());
            }

            }else {
                goRun(jPage);
            }
        }else {
            goRun(jPage);
        }

    }

    private synchronized void urlsAdd(List<String> list){
        list.addAll(urls);
        urls = list;

    }
    private void goRun(JPage jPage)  {
        JPage jj = jPage;
        Random r = new Random(1);
        do{
            while (urls.size() > 0) {
                boolean i = false;
                String url = null;
                try {
                    taskNum.add(Thread.currentThread().getName());
                    synchronized (this) {
                        if (urls.size() != 0) {
                            url = urls.remove(0);
                        } else {
                            i = true;
                        }
                    }
                    if (!i) {
                        if (filter) {
                            synchronized (Bloomfilter) {
                                if (!Bloomfilter.mightContain(url)) {
                                    Bloomfilter.put(url);
                                } else {
                                    i = true;
                                }
                            }
                        }
                        if (!i) {
                            if (interval != 0) {
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
                            j.setTagAll(jj.getTagAll());
                            jj = process(j);
                            urlsAdd(jj.getGoUrl());
                        }
                    }
                } catch (Exception e) {
                    StackTraceElement stackTraceElement = e.getStackTrace()[0];
                    log.error("错误:" + stackTraceElement.getFileName() + ",方法:" + stackTraceElement.getMethodName() + "，行:" + stackTraceElement.getLineNumber() + "，错误信息：" + e.toString());
                }finally {
                    taskNum.remove(Thread.currentThread().getName());
                }
            }
            int ran1 = r.nextInt(2000);
//            System.out.println(taskNum.size()+Thread.currentThread().getName());
            try {
                Thread.sleep(ran1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while (taskNum.size() > 0);
    }







    public void start(Integer i){
        this.T = i;
        is = true;
        run();
    }
}
