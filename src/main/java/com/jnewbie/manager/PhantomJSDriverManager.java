package com.jnewbie.manager;


import com.jnewbie.JHtml;
import com.jnewbie.request.JProxy;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
/**
 * @program: jnewbie
 * @description: Httpclient连接池
 * @author: pingc
 * @create: 2021-11-11 11:41
 **/
public class PhantomJSDriverManager {

    private static ConcurrentHashMap<String,Worker> pool ;//线程池
    private static int coreSize = 2;//核心线程池，一直驻留的资源
    private static int maxSize = 60;//设置最大数量资源，用于解决瞬时并发，瞬时并发过后要去识别空闲的释放掉，保留coreSize的资源
    private static int count = 0;//当前线程数
    private static int i = 0;  //运行数
    private static int statusSize = 0;//使用中的线程数
    private static String driverPath = "C:\\Users\\YRJ\\Desktop\\phantomjs.exe";   //driver路径
    private static DesiredCapabilities desiredCapabilities;
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
        //驱动支持
        desiredCapabilities.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, driverPath);
    }

    static {
        java.util.logging.Logger.getLogger(PhantomJSDriverService.class.getName()).setLevel(Level.OFF);
        String[] phantomArgs = new  String[] {
                "--webdriver-loglevel=NONE"
        };

        desiredCapabilities = new DesiredCapabilities();
        System.setProperty("phantomjs.binary.path",driverPath);
        //ssl证书支持
        desiredCapabilities.setCapability("acceptSslCerts", true);
        //截屏支持，这里不需要
        desiredCapabilities.setCapability("takesScreenshot", false);
        //css搜索支持
        desiredCapabilities.setCapability("cssSelectorsEnabled", false);
        //js支持
        desiredCapabilities.setJavascriptEnabled(true);
        desiredCapabilities.setCapability("phantomjs.page.settings.userAgent", JHtml.User_Agent);
        desiredCapabilities.setCapability("phantomjs.page.settings.loadImages", false);
        desiredCapabilities.setCapability("phantomjs.page.settings.disk-cache", false);
        //驱动支持
        desiredCapabilities.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, driverPath);
        //禁用大量的日志输出
        desiredCapabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);
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
    private PhantomJSDriverManager(){
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
            return pop();
        }

    }


    public static Worker push(Worker worker){
        pool.put("" + ++i,worker);
        return worker;
    }
    public static Worker push(Worker worker,JProxy jProxy){
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
                if(work.status == 0&& !entry.getKey().contains(":")){
                    return work;
                }
            }
        }
        return null;

    }


    public static Worker pop(JProxy jProxy){
        for (Map.Entry<String, Worker> entry : pool.entrySet()) {
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
            for (Map.Entry<String, Worker> entry : pool.entrySet()) {
                Worker work = entry.getValue();
                if(work.status == 0 && entry.getKey().contains(jProxy.getHost()+":"+jProxy.getPort())){
                    return work;
                }
            }
        }
        return null;
    }

    public static class Worker{
        private PhantomJSDriver driver;
        /**
         * 状态修改要让其他线程可见
         * 0就绪 1运行
         */
        private volatile int status;

        public Worker(){
            //创建无界面浏览器对象
            PhantomJSDriver driver = new PhantomJSDriver(desiredCapabilities);
            this.driver = driver;
            status = 0;
        }
        public Worker(JProxy jProxy){
            //创建无界面浏览器对象
            DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
            //ssl证书支持
            desiredCapabilities.setCapability("acceptSslCerts", true);
            //截屏支持，这里不需要
            desiredCapabilities.setCapability("takesScreenshot", false);
            //css搜索支持
            desiredCapabilities.setCapability("cssSelectorsEnabled", false);
            //js支持
            desiredCapabilities.setJavascriptEnabled(true);
            desiredCapabilities.setCapability("phantomjs.page.settings.userAgent", JHtml.User_Agent);
            desiredCapabilities.setCapability("phantomjs.page.settings.loadImages", false);
            desiredCapabilities.setCapability("phantomjs.page.settings.disk-cache", false);

            //代理
            ArrayList<String> cliArgsCap = new ArrayList<String>();
            cliArgsCap.add("--web-security=false");
            cliArgsCap.add("--ignore-ssl-errors=true");
            cliArgsCap.add("--proxy="+jProxy.getHost()+":"+jProxy.getPort());
            //密码
            if(jProxy.getUsername()!=null &&jProxy.getPassword()!=null) {
                cliArgsCap.add("--proxy-auth=" + jProxy.getUsername() + ":" + jProxy.getPassword());
            }
            cliArgsCap.add("--disk-cache=true");
            cliArgsCap.add("--webdriver-loglevel=NONE");

            desiredCapabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS,cliArgsCap);
            //驱动支持
            desiredCapabilities.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                    driverPath);

            PhantomJSDriver driver = new PhantomJSDriver(desiredCapabilities);
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

        public PhantomJSDriver getDriver() {
            start();
            return driver;
        }

        public void close() {
            driver.close();
        }
    }
}
