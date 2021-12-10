package com.jnewbie.manager;


import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * @program: jnewbie
 * @description:  HtmlUnit连接池
 * @author: pingc
 * @create: 2021-11-11 11:31
 **/
public class HtmlUnitManager {

    private static ConcurrentHashMap<String,Worker> pool ;//线程池
    private static int coreSize = 2;//核心线程池，一直驻留的资源
    private static int maxSize = 20;//设置最大数量资源，用于解决瞬时并发，瞬时并发过后要去识别空闲的释放掉，保留coreSize的资源
    private static int count = 0;//当前线程数
    private static int i = 0;  //运行数
    private static int statusSize = 0;//使用中的线程数
    public static void close(){
        for (Map.Entry<String, Worker> entry : pool.entrySet()) {
            Worker work = entry.getValue();
            pool.remove(entry.getKey());
            work.close();
        }
        count = 0;
        statusSize = 0;
    }

    static {
        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
        java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);
        pool = new ConcurrentHashMap<>();
        timer();
    }
    public static void timer() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                int c  = count;
                if(statusSize<c/2 &&count>2){
                    for (Map.Entry<String, Worker> entry : pool.entrySet()) {
                        Worker work = entry.getValue();
                        int i = 0;
                        if(work.status == 0 && i<c/3 &&count>=3){
                            pool.remove(entry.getKey());
                            work.close();
                            i++;
                            count--;
                        }
                    }
                }
            }
        }, 1000, 1000*60*10);// 设定指定的时间time,此处为2000毫秒
    }
    private HtmlUnitManager(){
    }



    public static synchronized Worker getPool(){
            if (count < coreSize ){
                count++;
                return push(new Worker());
            }else{
                return pop();
            }

    }


    public static Worker push(Worker worker){
        pool.put("" + ++i,worker);
        return worker;
    }




    /**
     * 循环获取未使用的资源
     * @return
     */
    public static Worker pop(){
        for (Map.Entry<String,Worker> entry : pool.entrySet()) {
            Worker work = entry.getValue();
            if(work.status == 0){
                return work;
            }
        }
        if (count < maxSize) {
            count++;
            return push(new Worker());
        }else {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (Map.Entry<String,Worker> entry : pool.entrySet()) {
                Worker work = entry.getValue();
                if(work.status == 0){
                    return work;
                }
            }
        }
        return null;

    }



    public static class Worker{
        private WebClient webClient;
        /**
         * 状态修改要让其他线程可见
         * 0就绪 1运行
         */
        private volatile int status;

        public Worker(){
            WebClient webClient = new WebClient(BrowserVersion.CHROME);
            //启用js，禁用css等
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setActiveXNative(false);
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.setAjaxController(new NicelyResynchronizingAjaxController());
            this.webClient = webClient;
            status = 0;
        }

        public void shutdown(){
            this.status = 0;
            statusSize--;
        }


        public void start(){
            this.status = 1;
            statusSize++;
        }

        public WebClient getWebClient() {
            start();
            return this.webClient;
        }

        public void close() {
            webClient.close();
        }
    }
}
