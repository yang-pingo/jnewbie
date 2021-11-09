package com.jnewbie;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.commons.logging.LogFactory;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * @program: jnewbie
 * @description: 获取网页
 * @author: pingc
 * @create: 2021-11-05 15:48
 **/
public class JHtml {

    public static Logger log = Logger.getLogger(JHtml.class);

    public static HttpclientManager httpclientManager = new HttpclientManager();
    private  String User_Agent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.160 Safari/537.22";
    private  String Accept_Charset = "utf-8";
    private  String Accept_EnCoding = "gzip, deflate";
    private  String Accept_Language = "zh,zh-CN";
    private String cookie;
    private JHeader jHeader;
    private JParam JParam;
    private JProxy JProxy;
    private Integer jsTime;

    static{
        LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log","org.apache.commons.logging.impl.NoOpLog");
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
        java.util.logging.Logger.getLogger("org.apache.http.client").setLevel(Level.OFF);
    }


    public JHtml(){}
    public JHtml setCookie (String cookie){
       this.cookie = cookie;
       return this;
    }
    public JHtml setParam (JParam JParam){
        this.JParam = JParam;
        return this;
    }
    public JHtml setProxy (JProxy JProxy){
        this.JProxy = JProxy;
        return this;
    }
    public JHtml setHeader (JHeader jHeader){
        this.jHeader = jHeader;
        return this;
    }

    public JHtml setJsTime (Integer jsTime){
        this.jsTime = jsTime;
        return this;
    }
    //get请求页面
    public JPage get(String url) {
        CloseableHttpClient httpclient = httpclientManager.getHttpClient();
        CloseableHttpResponse response = null;
        String responseBody = null;
        JPage jPage = new JPage();
        try {
            //如果没参数
            if (JParam != null) {
                List<NameValuePair> nameValuePairs = JParam.getNameValuePairs();
                NameValuePair remove = nameValuePairs.remove(0);
                url += "?" + remove.getName() + "=" + remove.getValue();
                for (NameValuePair nameValuePair : nameValuePairs) {
                    String name = nameValuePair.getName();
                    String value = nameValuePair.getValue();
                    url += "&" + name + "=" + value;
                }
            }
            jPage.setUrl(url);
            //创建请求
            HttpGet httpget = new HttpGet(url);
            //超时
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(5000).setConnectionRequestTimeout(1000)
                    .setSocketTimeout(5000).build();
            //如果没代理
            if(JProxy != null){
                HttpHost httpHost = new HttpHost(JProxy.host, JProxy.port);
                requestConfig.custom().setProxy(httpHost).build();
            }
            httpget.setConfig(requestConfig);
            //默认请求头
            httpget.addHeader("Accept-Charset", Accept_Charset);
            httpget.addHeader("Accept-Encoding", Accept_EnCoding);
            httpget.addHeader("Accept-Language", Accept_Language);
            httpget.addHeader("User-Agent", User_Agent);
            if(jHeader!=null){
                for (JHeader header : jHeader.getJHeaders()) {
                    httpget.addHeader(header.getName(),header.getValue());
                }
            }
            if(cookie!=null){
                httpget.addHeader("cookie", cookie);
            }
            //发起请求
            response = httpclient.execute(httpget);
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String location = null;
            if (!(status == HttpStatus.SC_MOVED_PERMANENTLY) || !(status == HttpStatus.SC_MOVED_TEMPORARILY)) {
                responseBody = entity != null ? EntityUtils.toString(entity, "UTF-8") : "";
            } else {
                // 从头中取出转向的地址
                Header locationHeader = httpget.getLastHeader("location");
            if (locationHeader != null) {
                location = locationHeader.getValue();
                responseBody = location;

            }}
            //给jpage添加
            jPage.setHeaders(response.getAllHeaders());
            jPage.setContent(responseBody);
            jPage.setCode(status);
            //释放连接
            entity.getContent().close();
        } catch (Exception e) {
            log.error("获取html阶段错误："+e);

        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return jPage;
    }

    //post请求页面
    public  JPage post(String url) {
        CloseableHttpClient httpclient = httpclientManager.getHttpClient();
        CloseableHttpResponse response = null;
        String responseBody = null;
        JPage jPage = new JPage();
        jPage.setUrl(url);
        try {
            HttpPost httppost = new HttpPost(url);
            // 如果没参数
            if (JParam != null) {
                httppost.setEntity(JParam.getUrlEncodedFormEntity());
            }

            //超时
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(5000).setConnectionRequestTimeout(1000)
                    .setSocketTimeout(5000).build();
            //如果没代理
            if(JProxy != null){
                HttpHost httpHost = new HttpHost(JProxy.host, JProxy.port);
                requestConfig.custom().setProxy(httpHost).build();
            }
            httppost.setConfig(requestConfig);

            //默认请求头
            httppost.addHeader("Accept-Charset", Accept_Charset);
            httppost.addHeader("Accept-Encoding", Accept_EnCoding);
            httppost.addHeader("Accept-Language", Accept_Language);
            httppost.addHeader("User-Agent", User_Agent);
            if(jHeader!=null){
                for (JHeader header : jHeader.getJHeaders()) {
                    httppost.addHeader(header.getName(),header.getValue());
                }
            }
            if(cookie!=null){
                httppost.addHeader("cookie", cookie);
            }
            //服务器返回数据处理
            response = httpclient.execute(httppost);
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String location = null;
            if (!(status == HttpStatus.SC_MOVED_PERMANENTLY) || !(status == HttpStatus.SC_MOVED_TEMPORARILY)) {
                responseBody = entity != null ? EntityUtils.toString(entity, "UTF-8") : "";
            } else  {
                // 从头中取出转向的地址
                Header locationHeader = httppost.getLastHeader("location");
                if (locationHeader != null) {
                    location = locationHeader.getValue();
                    responseBody = location;
                } else {
                    responseBody = "";
                }
            }

            //给jpage添加
            jPage.setHeaders(response.getAllHeaders());
            jPage.setContent(responseBody);
            jPage.setCode(status);
            jPage.setRedUrl(location);
            //释放连接
            entity.getContent().close();

        } catch (Exception e) {
            log.error("获取html阶段错误："+e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return jPage;

        }
    }

    //使用HtmlUnit加载动态页面（复杂JS无法加载）
    public JPage jGet(String url) {

        JPage jPage = new JPage();

        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME)) {
            //参数
            if (JParam != null) {
                List<NameValuePair> nameValuePairs = JParam.getNameValuePairs();
                NameValuePair remove = nameValuePairs.remove(0);
                url += "?" + remove.getName() + "=" + remove.getValue();
                for (NameValuePair nameValuePair : nameValuePairs) {
                    String name = nameValuePair.getName();
                    String value = nameValuePair.getValue();
                    url += "&" + name + "=" + value;
                }
            }

            jPage.setUrl(url);
            WebRequest request=new WebRequest(new URL(url));
            request.setCharset(Charset.defaultCharset());
            if(JProxy !=null) {
                request.setProxyHost(JProxy.host);
                request.setProxyPort(JProxy.port);
            }
            //请求头
            request.setAdditionalHeader("Accept-Charset", Accept_Charset);
            request.setAdditionalHeader("Accept-Encoding", Accept_EnCoding);
            request.setAdditionalHeader("Accept-Language", Accept_Language);
            request.setAdditionalHeader("User-Agent", User_Agent);
            if(jHeader!=null){
                for (JHeader header : jHeader.getJHeaders()) {
                    request.setAdditionalHeader(header.getName(),header.getValue());
                }
            }
            if(cookie!=null){
                request.setAdditionalHeader("cookie", cookie);
            }
            //启用js，禁用css等
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setActiveXNative(false);
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.setAjaxController(new NicelyResynchronizingAjaxController());

            final HtmlPage page = webClient.getPage(request);

            //等待背景js加载时间
            if(jsTime!=null){
                webClient.waitForBackgroundJavaScript(jsTime);
            }
            //获取请求
            final String pageAsXml = page.asXml();
            webClient.close();
            WebResponse webResponse = page.getWebResponse();

            int statusCode = webResponse.getStatusCode();

            ArrayList<Header> list = new ArrayList<>();
            for (com.gargoylesoftware.htmlunit.util.NameValuePair responseHeader : page.getWebResponse().getResponseHeaders()) {
                list.add(new BasicHeader(responseHeader.getName(),responseHeader.getValue()));
            }

            jPage.setHeaders(list.toArray(new Header[]{}));
            jPage.setContent(pageAsXml);
            jPage.setCode(statusCode);
            jPage.setContent(pageAsXml);
        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
        }
        return jPage;
    }
    public JPage eGet(String url) {

        JPage jPage = new JPage();

        WebDriver driver = null;
        HttpDriverManager.Worker worke = null;
        //如果没参数
        if (JParam != null) {
            List<NameValuePair> nameValuePairs = JParam.getNameValuePairs();
            NameValuePair remove = nameValuePairs.remove(0);
            url += "?" + remove.getName() + "=" + remove.getValue();
            for (NameValuePair nameValuePair : nameValuePairs) {
                String name = nameValuePair.getName();
                String value = nameValuePair.getValue();
                url += "&" + name + "=" + value;
            }
        }
        jPage.setUrl(url);
        try {
            //如果没代理
            if(JProxy != null){
                ChromeOptions options=new ChromeOptions();
                options.addArguments("-headless");
                String proxyServer = JProxy.host+":"+ JProxy.port;
                Proxy proxy = new Proxy().setHttpProxy(proxyServer).setSslProxy(proxyServer);
                options.setProxy(proxy);
                options.addArguments("-headless");
                driver = new ChromeDriver(options);
            }else{
                 worke = HttpDriverManager.getPool();
                driver = worke.getDriver();
            }
            driver.get(url);

            //等待几秒
            if(jsTime!=null){
                Thread.sleep(jsTime);
            }

            String pageSource = driver.getPageSource();
            jPage.setContent(pageSource);

        } catch (Exception e) {
            log.error(e);
        }finally {
            //放回连接池
            if(worke!=null){
                worke.shutdown();
            }
        }


        return jPage;
    }


}
