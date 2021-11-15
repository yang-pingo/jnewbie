package com.jnewbie;

import com.gargoylesoftware.htmlunit.*;
import com.jnewbie.manager.ChromeDriverManager;
import com.jnewbie.manager.HtmlUnitManager;
import com.jnewbie.manager.HttpclientManager;
import com.jnewbie.manager.PhantomJSDriverManager;
import com.jnewbie.request.JHeader;
import com.jnewbie.request.JPage;
import com.jnewbie.request.JParam;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * @program: jnewbie
 * @description: 获取网页
 * @author: pingc
 * @create: 2021-11-05 15:48
 **/
public class JHtml {
    private static final Logger log = LoggerFactory.getLogger(JHtml.class);
    public static HttpclientManager httpclientManager = new HttpclientManager();
    public static String User_Agent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.160 Safari/537.22";
    private String cookie;
    private JHeader jHeader;
    private com.jnewbie.request.JParam JParam;
    private com.jnewbie.request.JProxy JProxy;
    private long jsTime;
    public static Integer GET = 1;
    public static Integer HGET = 2;
    public static Integer PGET = 3;
    public static Integer CGET = 4;

    static{
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
    public JHtml setProxy (com.jnewbie.request.JProxy JProxy){
        this.JProxy = JProxy;
        return this;
    }
    public JHtml setHeader (JHeader jHeader){
        this.jHeader = jHeader;
        return this;
    }

    public JHtml setJsTime (long jsTime){
        this.jsTime = jsTime;
        return this;
    }
    //get请求页面
    public JPage get(String url) {
        HttpClientBuilder httpclient = httpclientManager.getHttpClient();
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
            RequestConfig.Builder builder = RequestConfig.custom()
                    .setConnectTimeout(5000).setConnectionRequestTimeout(1000)
                    .setSocketTimeout(5000);
            //如果没代理
            if(JProxy != null){
                HttpHost proxyHost = new HttpHost(JProxy.getHost(), JProxy.getPort());
                builder.setProxy(proxyHost);

                //包含账号密码的代理
                if(JProxy.getUsername()!=null &&JProxy.getPassword()!=null) {
                    CredentialsProvider credsProvider = new BasicCredentialsProvider();
                    credsProvider.setCredentials(new AuthScope(JProxy.getHost(),JProxy.getPort()),
                            new UsernamePasswordCredentials(JProxy.getUsername(), JProxy.getPassword()));
                    httpclient.setDefaultCredentialsProvider(credsProvider);
                }
            }
            RequestConfig config = builder.build();
            httpget.setConfig(config);
            //默认请求头
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
            response = httpclient.build().execute(httpget);
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

        }catch (Exception e) {
            if(JProxy!=null) {
                if (e.toString().contains(JProxy.getHost())) {
                    log.error("代理服务器连接失败：" + e);
                }
            }else{
                StackTraceElement stackTraceElement= e.getStackTrace()[0];
                log.error("获取html阶段错误："+stackTraceElement.getFileName()+",方法:"+stackTraceElement.getMethodName()+"，行:"+stackTraceElement.getLineNumber()+"，错误信息："+e.toString());
            }

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
        HttpClientBuilder httpclient = httpclientManager.getHttpClient();
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
            RequestConfig.Builder builder = RequestConfig.custom()
                    .setConnectTimeout(5000).setConnectionRequestTimeout(1000)
                    .setSocketTimeout(5000);
            //如果没代理
            if(JProxy != null){
                HttpHost proxyHost = new HttpHost(JProxy.getHost(), JProxy.getPort());
                builder.setProxy(proxyHost);

                //包含账号密码的代理
                if(JProxy.getUsername()!=null &&JProxy.getPassword()!=null) {
                    CredentialsProvider credsProvider = new BasicCredentialsProvider();
                    credsProvider.setCredentials(new AuthScope(JProxy.getHost(),JProxy.getPort()),
                            new UsernamePasswordCredentials(JProxy.getUsername(), JProxy.getPassword()));
                    httpclient.setDefaultCredentialsProvider(credsProvider);
                }
            }
            RequestConfig config = builder.build();
            httppost.setConfig(config);

            //默认请求头
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
            response = httpclient.build().execute(httppost);
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
            if(e.toString().contains(JProxy.getHost())){
                log.error("代理服务器连接失败："+e);
            }else{
                StackTraceElement stackTraceElement= e.getStackTrace()[0];
                log.error("获取html阶段错误："+stackTraceElement.getFileName()+",方法:"+stackTraceElement.getMethodName()+"，行:"+stackTraceElement.getLineNumber()+"，错误信息："+e.toString());
            }
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
    public JPage hGet(String url) {
        JPage jPage = new JPage();
        HtmlUnitManager.Worker worke = HtmlUnitManager.getPool();
        try {

            WebClient webClient = worke.getWebClient();
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
                if(JProxy.getUsername()!=null &&JProxy.getPassword()!=null) {
                    ((DefaultCredentialsProvider) webClient.getCredentialsProvider())
                            .addCredentials(JProxy.getUsername(),JProxy.getPassword());

                }
            }

            //请求头
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
             Page page = webClient.getPage(request);
            //等待背景js加载时间
            if(jsTime!= 0){
                webClient.waitForBackgroundJavaScript(jsTime);
            }
            //获取请求
            String pageAsXml  = page.getWebResponse().getContentAsString();

            WebResponse webResponse = page.getWebResponse();

            int statusCode = webResponse.getStatusCode();

            ArrayList<Header> list = new ArrayList<>();
            for (com.gargoylesoftware.htmlunit.util.NameValuePair responseHeader : page.getWebResponse().getResponseHeaders()) {
                list.add(new BasicHeader(responseHeader.getName(),responseHeader.getValue()));
            }

            jPage.setHeaders(list.toArray(new Header[]{}));
            jPage.setCode(statusCode);
            jPage.setContent(pageAsXml);
        } catch (Exception e) {
            if(JProxy!=null) {
                if (e.toString().contains(JProxy.getHost())) {
                    log.error("代理服务器连接失败：" + e);
                }
            }else{
                StackTraceElement stackTraceElement= e.getStackTrace()[0];
                log.error("获取html阶段错误："+stackTraceElement.getFileName()+",方法:"+stackTraceElement.getMethodName()+"，行:"+stackTraceElement.getLineNumber()+"，错误信息："+e.toString());
            }
        }finally {
            //放回连接池
            if(worke!=null){
                worke.shutdown();
            }
        }
        return jPage;
    }



    public JPage cGet(String url) {
        JPage jPage = new JPage();
        WebDriver driver = null;
        ChromeDriverManager.Worker worke = null;
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
                worke = ChromeDriverManager.getPool(JProxy);
                driver = worke.getDriver();
            }else{
                 worke = ChromeDriverManager.getPool();
                driver = worke.getDriver();
            }
            driver.get(url);

            //等待几秒
            if(jsTime!=0){
                Thread.sleep(jsTime);
            }

            String pageSource = driver.getPageSource();
            jPage.setContent(pageSource);

        } catch (Exception e) {
            if(e.toString().contains("ERR_PROXY_CONNECTION_FAILED")){
                log.error("代理服务器连接失败："+e);
            }else{
                StackTraceElement stackTraceElement= e.getStackTrace()[0];
                log.error("获取html阶段错误："+stackTraceElement.getFileName()+",方法:"+stackTraceElement.getMethodName()+"，行:"+stackTraceElement.getLineNumber()+"，错误信息："+e.toString());

            }
        }finally {
            //放回连接池
            if(worke!=null){
                worke.shutdown();
            }
        }


        return jPage;
    }

    public JPage pGet(String url) {
        JPage jPage = new JPage();
        WebDriver driver = null;
        PhantomJSDriverManager.Worker worke = null;
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
                worke = PhantomJSDriverManager.getPool(JProxy);
                driver = worke.getDriver();
            }else{
                worke = PhantomJSDriverManager.getPool();
                driver = worke.getDriver();
            }
            driver.get(StringToUrl.to(url));

            //等待
            if(jsTime!=0) {
                driver.manage().timeouts().implicitlyWait(jsTime, TimeUnit.MILLISECONDS);
            }
            String pageSource = driver.getPageSource();
            jPage.setContent(pageSource);
            if(jPage.getContent().equals("<html><head></head><body></body></html>")){
                if(JProxy!=null){
                    throw new Exception("连接失败,可导致错误的因素有很多：网络错误，代理连接失败，网站连接失败等\n当前有设置代理："+JProxy.getHost()+":"+JProxy.getPort());
                }
                throw new Exception("连接失败,可导致错误的因素有很多：网络错误，代理连接失败，网站连接失败等");

            }
        } catch (Exception e) {
            jPage.setContent("");
            if(e.toString().contains("连接失败")){
                log.error(e.toString());
            }else{
                StackTraceElement stackTraceElement= e.getStackTrace()[0];
                log.error("获取html阶段错误："+stackTraceElement.getFileName()+",方法:"+stackTraceElement.getMethodName()+"，行:"+stackTraceElement.getLineNumber()+"，错误信息："+e.toString());
            }
        }finally {
            //放回连接池
            if(worke!=null){
                worke.shutdown();
            }
        }

        return jPage;
    }
}
