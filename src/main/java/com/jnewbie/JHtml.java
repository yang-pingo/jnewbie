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
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
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
    private static boolean logz = true;
    private String encoding = "UTF-8";
    private static final Logger log = LoggerFactory.getLogger(JHtml.class);
    public static String User_Agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Safari/537.36";
    private String cookie;
    private JHeader jHeader;
    private com.jnewbie.request.JParam JParam;
    private com.jnewbie.request.JProxy JProxy;
    private long jsTime;
    private long timeOut= 5000;
    private int retry = 3;
    private long retryTime = 2000;

    public static Integer GET = 1;
    public static Integer HGET = 2;
    public static Integer PGET = 3;
    public static Integer CGET = 4;


    public JHtml setLog(Boolean b){
        logz = b;
        return  this;
    }

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
public JHtml setEncoding (String encoding){
        this.encoding = encoding;
        return this;
}
    public JHtml setJsTime (long jsTime){
        this.jsTime = jsTime;
        return this;
    }
    public JHtml setTimeOut (long timeOut){
        this.timeOut = timeOut;
        return this;
    }
    public JHtml setRetry (int retry){
        this.retry = retry;
        return this;
    }
    public JHtml setRetryTime (long retryTime){
        this.retryTime = retryTime;
        return this;
    }
    //get请求页面
    public JPage get(String url) {
        url = url.trim();
        if(logz) {
            log.info("get : " + url);
        }
        int i = 0;
        JPage jPage = new JPage();
        while (i<retry) {
            CloseableHttpClient httpclient = HttpclientManager.getHttpClient();
            CloseableHttpResponse response = null;
            String responseBody = null;
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
                        .setConnectTimeout((int) timeOut).setConnectionRequestTimeout(1000)
                        .setSocketTimeout((int) timeOut);

                HttpClientContext context = HttpClientContext.create();
                //如果没代理
                if (JProxy != null) {
                    HttpHost proxyHost = new HttpHost(JProxy.getHost(), JProxy.getPort());
                    builder.setProxy(proxyHost);
                    //包含账号密码的代理
                    if (JProxy.getUsername() != null && JProxy.getPassword() != null) {
                        BasicAuthCache authCache = new BasicAuthCache();
                        BasicScheme proxyAuth = new BasicScheme();
                        proxyAuth.processChallenge(new BasicHeader(AUTH.PROXY_AUTH, "BASIC realm=default"));
                        authCache.put(proxyHost, proxyAuth);
                        CredentialsProvider credsProvider = new BasicCredentialsProvider();
                        credsProvider.setCredentials(new AuthScope(JProxy.getHost(), JProxy.getPort()),
                                new UsernamePasswordCredentials(JProxy.getUsername(), JProxy.getPassword()));
//                        httpclient.setDefaultCredentialsProvider(credsProvider);
                        context.setAuthCache(authCache);
                        context.setCredentialsProvider(credsProvider);
                    }
                }
                RequestConfig config = builder.build();
                httpget.setConfig(config);
                //默认请求头
                httpget.addHeader("User-Agent", User_Agent);
                if (jHeader != null) {
                    for (JHeader header : jHeader.getJHeaders()) {
                        httpget.addHeader(header.getName(), header.getValue());
                    }
                }
                if (cookie != null) {
                    httpget.addHeader("cookie", cookie);
                }
                //发起请求
                response = httpclient.execute(httpget,context);
                int status = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    entity = new BufferedHttpEntity(entity);
                }
                String location = null;
                if (!(status == HttpStatus.SC_MOVED_PERMANENTLY) || !(status == HttpStatus.SC_MOVED_TEMPORARILY)) {
                    responseBody = entity != null ? EntityUtils.toString(entity, encoding) : "";
                } else {
                    // 从头中取出转向的地址
                    Header locationHeader = httpget.getLastHeader("location");
                    if (locationHeader != null) {
                        location = locationHeader.getValue();
                        responseBody = location;
                    }
                }
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                entity.writeTo(bos);
                byte[] bytes = bos.toByteArray();

                //给jpage添加
                jPage.setHeaders(response.getAllHeaders());
                jPage.setContent(responseBody);
                jPage.setCode(status);
                jPage.setBytes(bytes);
                //释放连接
                entity.getContent().close();
//                httpget.clone();
                bos.close();
                i=retry;
            } catch (SocketTimeoutException e){
                log.error(url + "请求超时");
                i++;
                log.error(url+"开始重试："+i);
            } catch (Exception e) {
                if (JProxy != null) {
                    if (JProxy !=null &&e.toString().contains(JProxy.getHost())) {
                        log.error("代理服务器连接失败：" + e);
                    }
                } else {
                    e.printStackTrace();
                    StackTraceElement stackTraceElement = e.getStackTrace()[0];
                    log.error(url + "获取html阶段错误：" + stackTraceElement.getFileName() + ",方法:" + stackTraceElement.getMethodName() + "，行:" + stackTraceElement.getLineNumber() + "，错误信息：" + e.toString());
                }
                try {
                    Thread.sleep(retryTime);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
                i++;
                log.error(url+"开始重试："+i);
            } finally {
                if (response != null) {
                    try {
                        response.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return jPage;
    }

    //post请求页面
    public  JPage post(String url) {
        url = url.trim();
        if(logz) {
            log.info("post : " + url);
        }
        int i = 0;
        JPage jPage = new JPage();
        while (i<retry) {
            CloseableHttpClient httpclient = HttpclientManager.getHttpClient();
            CloseableHttpResponse response = null;
            String responseBody = null;
            jPage.setUrl(url);
            try {
                HttpPost httppost = new HttpPost(url);
                // 如果没参数
                if (JParam != null) {
                    httppost.setEntity(JParam.getUrlEncodedFormEntity());
                }

                //超时
                RequestConfig.Builder builder = RequestConfig.custom()
                        .setConnectTimeout((int) timeOut).setConnectionRequestTimeout(1000)
                        .setSocketTimeout((int) timeOut);

                HttpClientContext context = HttpClientContext.create();

                //如果没代理
                if (JProxy != null) {
                    HttpHost proxyHost = new HttpHost(JProxy.getHost(), JProxy.getPort());
                    builder.setProxy(proxyHost);

                    //包含账号密码的代理
                    if (JProxy.getUsername() != null && JProxy.getPassword() != null) {
                        BasicAuthCache authCache = new BasicAuthCache();
                        BasicScheme proxyAuth = new BasicScheme();
                        proxyAuth.processChallenge(new BasicHeader(AUTH.PROXY_AUTH, "BASIC realm=default"));
                        authCache.put(proxyHost, proxyAuth);
                        CredentialsProvider credsProvider = new BasicCredentialsProvider();
                        credsProvider.setCredentials(new AuthScope(JProxy.getHost(), JProxy.getPort()),
                        new UsernamePasswordCredentials(JProxy.getUsername(), JProxy.getPassword()));
//                        httpclient.setDefaultCredentialsProvider(credsProvider);
                        context.setAuthCache(authCache);
                        context.setCredentialsProvider(credsProvider);
                    }
                }
                RequestConfig config = builder.build();
                httppost.setConfig(config);

                //默认请求头
                httppost.addHeader("User-Agent", User_Agent);
                if (jHeader != null) {
                    for (JHeader header : jHeader.getJHeaders()) {
                        httppost.addHeader(header.getName(), header.getValue());
                    }
                }
                if (cookie != null) {
                    httppost.addHeader("cookie", cookie);
                }
                //服务器返回数据处理
                response = httpclient.execute(httppost,context);
                int status = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();
                String location = "";
                if (!(status == HttpStatus.SC_MOVED_PERMANENTLY) || !(status == HttpStatus.SC_MOVED_TEMPORARILY)) {
                    responseBody = entity != null ? EntityUtils.toString(entity, encoding) : "";
                } else {
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
                i=retry;
            } catch (SocketTimeoutException e){
                i++;
                log.error(url + "请求超时");
                log.error(url+"开始重试："+i);
            }
            catch (Exception e) {
                e.printStackTrace();
                if (JProxy !=null &&e.toString().contains(JProxy.getHost())) {
                    log.error("代理服务器连接失败：" + e);
                } else {
                    StackTraceElement stackTraceElement = e.getStackTrace()[0];
                    log.error(url + "获取html阶段错误：" + stackTraceElement.getFileName() + ",方法:" + stackTraceElement.getMethodName() + "，行:" + stackTraceElement.getLineNumber() + "，错误信息：" + e.toString());
                }
                i++;
                try {
                    Thread.sleep(retryTime);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
                i++;
                log.error(url+"开始重试："+i);
            } finally {
                if (response != null) {
                    try {
                        EntityUtils.consume(response.getEntity());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return jPage;


    }

    //使用HtmlUnit加载动态页面（复杂JS无法加载）
    public JPage hGet(String url) {
        url = url.trim();
        if(logz) {
            log.info("hGet : " + url);
        }
        int i = 0;
        JPage jPage = new JPage();
        while (i<retry) {
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
                WebRequest request = new WebRequest(new URL(url));
                request.setCharset(Charset.defaultCharset());
                if (JProxy != null) {
                    request.setProxyHost(JProxy.host);
                    request.setProxyPort(JProxy.port);
                    if (JProxy.getUsername() != null && JProxy.getPassword() != null) {
                        ((DefaultCredentialsProvider) webClient.getCredentialsProvider())
                                .addCredentials(JProxy.getUsername(), JProxy.getPassword());

                    }
                }

                //请求头
                request.setAdditionalHeader("User-Agent", User_Agent);
                if (jHeader != null) {
                    for (JHeader header : jHeader.getJHeaders()) {
                        request.setAdditionalHeader(header.getName(), header.getValue());
                    }
                }
                if (cookie != null) {
                    request.setAdditionalHeader("cookie", cookie);
                }
                //启用js，禁用css等
                webClient.getOptions().setThrowExceptionOnScriptError(false);
                webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
                webClient.getOptions().setActiveXNative(false);
                webClient.getOptions().setCssEnabled(false);
                webClient.getOptions().setJavaScriptEnabled(true);
                webClient.setAjaxController(new NicelyResynchronizingAjaxController());
                webClient.getOptions().setTimeout((int) timeOut);
                Page page = webClient.getPage(request);
                //等待背景js加载时间
                if (jsTime != 0) {
                    webClient.waitForBackgroundJavaScript(jsTime);
                    webClient.setJavaScriptTimeout(jsTime);
                }

                //获取请求
                String pageAsXml = page.getWebResponse().getContentAsString();
                WebResponse webResponse = page.getWebResponse();
                int statusCode = webResponse.getStatusCode();

                ArrayList<Header> list = new ArrayList<>();
                for (com.gargoylesoftware.htmlunit.util.NameValuePair responseHeader : page.getWebResponse().getResponseHeaders()) {
                    list.add(new BasicHeader(responseHeader.getName(), responseHeader.getValue()));
                }

                jPage.setHeaders(list.toArray(new Header[]{}));
                jPage.setCode(statusCode);
                jPage.setContent(pageAsXml);
                i = retry;
            } catch (SocketTimeoutException e){
                log.error(url + "请求超时");
                i++;
                log.error(url+"开始重试："+i);
            } catch (Exception e) {
                if (JProxy != null) {
                    if (JProxy !=null &&e.toString().contains(JProxy.getHost())) {
                        log.error("代理服务器连接失败：" + e);
                    }
                } else {
                    StackTraceElement stackTraceElement = e.getStackTrace()[0];
                    log.error(url + "获取html阶段错误：" + stackTraceElement.getFileName() + ",方法:" + stackTraceElement.getMethodName() + "，行:" + stackTraceElement.getLineNumber() + "，错误信息：" + e.toString());
                }
                try {
                    Thread.sleep(retryTime);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
                i++;
                log.error(url+"开始重试："+i);
            } finally {
                //放回连接池
                if (worke != null) {
                    worke.shutdown();
                }
            }
        }

        return jPage;
    }



    public JPage cGet(String url) {
        url = url.trim();
        if(logz) {
            log.info("cGet : " + url);
        }
        int i = 0;
        JPage jPage = new JPage();
        while (i<retry) {
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
                if (JProxy != null) {
                    worke = ChromeDriverManager.getPool(JProxy);
                    driver = worke.getDriver();
                } else {
                    worke = ChromeDriverManager.getPool();
                    driver = worke.getDriver();
                }
                driver.manage().timeouts().pageLoadTimeout(timeOut, TimeUnit.MILLISECONDS);
                driver.get(url);

                //等待几秒
                if (jsTime != 0) {
                    Thread.sleep(jsTime);
                }

                String pageSource = driver.getPageSource();
                jPage.setContent(pageSource);
                i=retry;
            } catch (Exception e) {
                if (e.toString().contains("ERR_PROXY_CONNECTION_FAILED")) {
                    log.error("代理服务器连接失败：" + e);
                } else {
                    StackTraceElement stackTraceElement = e.getStackTrace()[0];
                    log.error(url + "获取html阶段错误：" + stackTraceElement.getFileName() + ",方法:" + stackTraceElement.getMethodName() + "，行:" + stackTraceElement.getLineNumber() + "，错误信息：" + e.toString());

                }
                try {
                    Thread.sleep(retryTime);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
                i++;
                log.error(url+"开始重试："+i);
            } finally {
                //放回连接池
                if (worke != null) {
                    worke.shutdown();
                }
            }
        }


        return jPage;
    }

    public JPage pGet(String url) {
        url = url.trim();
        if(logz) {
            log.info("pGet : " + url);
        }
        int i = 0;
        JPage jPage = new JPage();
        while (i<retry) {
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
                if (JProxy != null) {
                    worke = PhantomJSDriverManager.getPool(JProxy);
                    driver = worke.getDriver();
                } else {
                    worke = PhantomJSDriverManager.getPool();
                    driver = worke.getDriver();
                }
                driver.manage().timeouts().pageLoadTimeout(timeOut, TimeUnit.MILLISECONDS);
                if (jsTime != 0) {
                    driver.manage().timeouts().implicitlyWait(jsTime, TimeUnit.MILLISECONDS);
                    driver.manage().timeouts().setScriptTimeout(jsTime, TimeUnit.MILLISECONDS);
                    Thread.sleep(jsTime);
                }
                driver.get(StringToUrl.to(url));
                //等待

                String pageSource = driver.getPageSource();
                jPage.setContent(pageSource);
                if (jPage.getContent().equals("<html><head></head><body></body></html>")) {
                    if (JProxy != null) {
                        throw new Exception("连接失败,可导致错误的因素有很多：网络错误，代理连接失败，网站连接失败等\n当前有设置代理：" + JProxy.getHost() + ":" + JProxy.getPort());
                    }
                    throw new Exception("连接失败,可导致错误的因素有很多：网络错误，代理连接失败，网站连接失败等");

                }
                i=retry;
            } catch (SocketTimeoutException e){
                log.error(url + "请求超时");
                i++;
                log.error(url+"开始重试："+i);
            }catch (Exception e) {
                jPage.setContent("");
                if (e.toString().contains("连接失败")) {
                    log.error(e.toString());
                } else {
                    StackTraceElement stackTraceElement = e.getStackTrace()[0];
                    log.error(url + "获取html阶段错误：" + stackTraceElement.getFileName() + ",方法:" + stackTraceElement.getMethodName() + "，行:" + stackTraceElement.getLineNumber() + "，错误信息：" + e.toString());
                }
                try {
                    Thread.sleep(retryTime);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
                i++;
                log.error(url+"开始重试："+i);
            } finally {
                //放回连接池
                if (worke != null) {
                    worke.shutdown();
                }
            }
        }

        return jPage;
    }


    public byte[] download(String url) throws IOException {
        url = url.trim();
        URL urll = new URL(url);
        HttpURLConnection conn = (HttpURLConnection)urll.openConnection();
        //设置超时间为3秒
        conn.setConnectTimeout(5*1000);
        //防止屏蔽程序抓取而返回403错误
        conn.setRequestProperty("User-Agent", User_Agent);
        //得到输入流
        InputStream inputStream = conn.getInputStream();
        //获取自己数组
        byte[] getData = readInputStream(inputStream);
        inputStream.close();
        return getData;

    }
    private byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }
}
