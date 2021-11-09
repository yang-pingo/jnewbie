package com.jnewbie;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class HttpDriverManager {

    private static ConcurrentHashMap<Integer,Worker> pool ;//线程池
    private static int coreSize = 2;//核心线程池，一直驻留的资源
    private static int maxSize = 10;//设置最大数量资源，用于解决瞬时并发，瞬时并发过后要去识别空闲的释放掉，保留coreSize的资源
    private static int count = 0;//当前线程数
    private static int i = 0;  //运行数
    private static int statusSize = 0;//使用中的线程数
    private static String driverPath;   //driver路径
    private static String driverName;   //driver名称

    public static void setDriverPath(String path){
        driverPath = path;
    }
    public static void setDriverName(String name){
        driverName = name;
    }
    static {
        //禁用日志

        System.setProperty("webdriver.chrome.silentOutput", "true");
        java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);
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
                    for (Map.Entry<Integer, Worker> entry : pool.entrySet()) {
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
    private HttpDriverManager(){
    }

    public static void init(int coreSize){//线程池数量初始化
        HttpDriverManager.coreSize = coreSize;
    }

    public static synchronized Worker getPool(){
            if (count < coreSize ){//自增!!!
                count++;
                return push(new Worker());
            }else{
                return pop();
            }

    }



    public static Worker push(Worker worker){
        pool.put(++i,worker);
        return worker;
    }



    /**
     * 循环获取未使用的资源
     * @return
     */
    public static Worker pop(){
        for (Map.Entry<Integer,Worker> entry : pool.entrySet()) {
            Worker work = entry.getValue();
            if(work.status == 0){
                return work;
            }
        }
        if (count < maxSize) {//自增!!!
            count++;
            return push(new Worker());
        }else {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (Map.Entry<Integer,Worker> entry : pool.entrySet()) {
                Worker work = entry.getValue();
                if(work.status == 0){
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
         * 1就绪 2运行
         */
        private volatile int status;

        public Worker(){
            ChromeOptions options=new ChromeOptions();
            options.addArguments("-headless");
            driver = new ChromeDriver(options);
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
