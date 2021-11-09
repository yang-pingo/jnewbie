package com.jnewbie;

import org.apache.log4j.Logger;

/**
 * @program: jnewbie
 * @description: 数据处理器
 * @author: pingc
 * @create: 2021-11-06 10:31
 **/
public abstract class JProcessor implements Runnable {
    Integer T = 1;
    JHtml jHtml;
    String url;
    JPage jPage;
    //使用哪种get
    Integer getMethod = 1;

    public static Integer GET = 1;
    public static Integer JGET = 2;
    public static Integer EGET = 3;
    Integer interval = 100;

    public static Logger log = Logger.getLogger(JProcessor.class);
    public JProcessor(){}

    private final static MyBloomFilter myBloomFilter;
    static {myBloomFilter = new MyBloomFilter(); }



    public JProcessor setUrl(String url) {
        this.url = url;
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

    public JProcessor setGetMethod(Integer getMethod) {
        this.getMethod = getMethod;
        return this;
    }
    //页面信息处理
    public abstract JPage process(JPage jPage);


    @Override
    public void run() {
        //检查是否是起步
        JPage jPage = null;
        if (this.jPage == null) {
            switch(this.getMethod){
                case 1 :
                    jPage = jHtml.get(url);
                    break;
                case 2 :
                    jPage = jHtml.jGet(url);
                    break;
                case 3 :
                    jPage = jHtml.eGet(url);
                    break;
            }
            this.jPage = process(jPage);
            for (int z = 0;z<T;z++){
                Thread thread = new Thread(this);
                thread.start();
            }
        }else{
            goRun(this.jPage);
        }

    }
    private void goRun(JPage jPage)  {
        for (String url : jPage.getGoUrl()) {
            Boolean i = false;
            synchronized(this) {
                if (!myBloomFilter.contain(url)) {
                    myBloomFilter.add(url);
                    i = true;
                }
            }
            if(interval!=null) {
                try {
                    Thread.sleep(interval);
                } catch (Exception e) {
                    log.error("爬取间隔错误");
                }
            }
                if(i){
                    JPage j = null;
                    switch(this.getMethod){
                        case 1 :
                            j = jHtml.get(url);
                            break;
                        case 2 :
                            j = jHtml.jGet(url);
                            break;
                        case 3 :
                            j = jHtml.eGet(url);
                            break;
                    }
                    JPage process = process(j);
                    goRun(process);
                }
            }
        }


    public void start(Integer i){
        this.T = i;
        run();
    }
}
