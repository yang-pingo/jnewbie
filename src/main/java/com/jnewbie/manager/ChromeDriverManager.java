package com.jnewbie.manager;

import com.jnewbie.request.JProxy;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
/**
 * @program: jnewbie
 * @description: ChromeDriver连接池
 * @author: pingc
 * @create: 2021-11-11 11:33
 **/
public class ChromeDriverManager {

    private static ConcurrentHashMap<String,Worker> pool ;//线程池
    private static int coreSize = 2;//核心线程池，一直驻留的资源
    private static int maxSize = 20;//设置最大数量资源，用于解决瞬时并发，瞬时并发过后要去识别空闲的释放掉，保留coreSize的资源
    private static int count = 0;//当前线程数
    private static int i = 0;  //运行数
    private static int statusSize = 0;//使用中的线程数
    private static String driverPath = "C:\\Users\\YRJ\\Desktop\\chromedriver_win32\\chromedriver.exe";   //driver路径

    public static void close(){
        for (Map.Entry<String, Worker> entry : pool.entrySet()) {
            Worker work = entry.getValue();
                pool.remove(entry.getKey());
                work.close();
            }
        count = 0;
        statusSize = 0;
    }
    public static void setDriverPath(String path){
        driverPath = path;
        //驱动初始化
        System.setProperty("webdriver.chrome.driver", driverPath);
    }



    static {
        //禁用日志
        System.setProperty("webdriver.chrome.silentOutput", "true");
        java.util.logging.Logger.getLogger(ChromeDriverService.class.getName()).setLevel(Level.OFF);
        //驱动初始化
        System.setProperty("webdriver.chrome.driver", driverPath);

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
    private ChromeDriverManager(){
    }

    public static void init(int coreSize){//线程池数量初始化
        ChromeDriverManager.coreSize = coreSize;
    }

    public static synchronized Worker getPool(){
            if (count < coreSize ){
                count++;
                return push(new Worker());
            }else{
                return pop();
            }

    }

    public static synchronized Worker getPool(JProxy jProxy){
        if (count < coreSize ){
            count++;
            return push(new Worker(jProxy),jProxy);
        }else{
            return pop(jProxy);
        }

    }


    public static Worker push(Worker worker){
        pool.put("" + ++i,worker);
        return worker;
    }
    public static Worker push(Worker worker, JProxy jProxy){
        pool.put(jProxy.getHost()+":"+jProxy.getPort()+ ":"+ ++i,worker);
        return worker;
    }





    /**
     * 循环获取未使用的资源
     * @return
     */
    public static Worker pop(){
        for (Map.Entry<String,Worker> entry : pool.entrySet()) {
            Worker work = entry.getValue();
            if(work.status == 0 && !entry.getKey().contains(":")){
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
                if(work.status == 0 && !entry.getKey().contains(":")){
                    return work;
                }
            }
        }
        return null;
    }

    public static Worker pop(JProxy jProxy){
        for (Map.Entry<String,Worker> entry : pool.entrySet()) {
            Worker work = entry.getValue();
            if(work.status == 0 && entry.getKey().contains(jProxy.getHost()+":"+jProxy.getPort())){
                return work;
            }
        }
        if (count < maxSize) {
            count++;
            return push(new Worker(jProxy));
        }else {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (Map.Entry<String,Worker> entry : pool.entrySet()) {
                Worker work = entry.getValue();
                if(work.status == 0 && entry.getKey().contains(jProxy.getHost()+":"+jProxy.getPort())){
                    return work;
                }
            }
        }
        return null;
    }

    public static class Worker{
        private ChromeDriver driver;
        /**
         * 状态修改要让其他线程可见
         * 0就绪 1运行
         */
        private volatile int status;

        public Worker(){
            ChromeOptions options=new ChromeOptions();
            options.addArguments("-headless");
            driver = new ChromeDriver(options);
            status = 0;
        }

        public Worker(JProxy jProxy){
            ChromeOptions options=new ChromeOptions();
            options.addArguments("-headless");
            Proxy proxy = new Proxy();
            String proxyServer = jProxy.getHost()+":"+jProxy.getPort();
            proxy.setHttpProxy(proxyServer).setSslProxy(proxyServer);
            options.setProxy(proxy);
            ChromeDriver driver = new ChromeDriver(options);
            this.driver = driver;
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

        public ChromeDriver getDriver() {
            start();
            return driver;
        }

        public void close() {
            driver.close();
        }
    }
}
