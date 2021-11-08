package com.jnewbie;

import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

import java.net.URI;
import java.util.List;

/**
 * @program: jnewbie
 * @description: 获取网页
 * @author: pingc
 * @create: 2021-11-05 15:48
 **/
public class JHtml {

    public static Logger log = Logger.getLogger(JHtml.class);

    public static HttpConnectionManager httpConnectionManager = new HttpConnectionManager();
    private  String User_Agent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.160 Safari/537.22";
    private  String Accept = "text/html";
    private  String Accept_Charset = "utf-8";
    private  String Accept_EnCoding = "gzip";
    private  String Accept_Language = "en-Us,en";
    private String cookie;
    private Param param;
    private Proxy proxy;
    public JHtml(){}
    public JHtml setCookie (String cookie){
       this.cookie = cookie;
       return this;
    }
    public JHtml setParam (Param param){
        this.param = param;
        return this;
    }
    public JHtml setProxy (Proxy proxy){
        this.proxy = proxy;
        return this;
    }

    //get请求页面
    public JPage get(String url) {
        CloseableHttpClient httpclient = httpConnectionManager.getHttpClient();
        CloseableHttpResponse response = null;
        String responseBody = null;
        JPage jPage = new JPage();
        try {
            //如果没参数
            if (param != null) {
                List<NameValuePair> nameValuePairs = param.getNameValuePairs();
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
            if(proxy!= null){
                HttpHost httpHost = new HttpHost(proxy.host,proxy.port);
                requestConfig.custom().setProxy(httpHost).build();
            }
            httpget.setConfig(requestConfig);
            //请求头
            httpget.addHeader("Accept", Accept);
            httpget.addHeader("Accept-Charset", Accept_Charset);
            httpget.addHeader("Accept-Encoding", Accept_EnCoding);
            httpget.addHeader("Accept-Language", Accept_Language);
            httpget.addHeader("User-Agent", User_Agent);
            if(cookie!=null){
                httpget.addHeader("cookie", cookie);
            }
            //发起请求
            response = httpclient.execute(httpget);
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String location = null;
            if (status >= 200 && status < 300) {
                responseBody = entity != null ? EntityUtils.toString(entity) : "";
            } else if (status == HttpStatus.SC_MOVED_PERMANENTLY || status == HttpStatus.SC_MOVED_TEMPORARILY) {
                // 从头中取出转向的地址
                Header locationHeader = httpget.getLastHeader("location");
            if (locationHeader != null) {
                location = locationHeader.getValue();
                responseBody = location;

            }}else {
                responseBody = "";
            }
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
        CloseableHttpClient httpclient = httpConnectionManager.getHttpClient();
        CloseableHttpResponse response = null;
        String responseBody = null;
        JPage jPage = new JPage();
        jPage.setUrl(url);
        try {
            HttpPost httppost = new HttpPost(url);
            // 如果没参数
            if (param != null) {
                httppost.setEntity(param.getUrlEncodedFormEntity());
            }

            //超时
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(5000).setConnectionRequestTimeout(1000)
                    .setSocketTimeout(5000).build();
            //如果没代理
            if(proxy!= null){
                HttpHost httpHost = new HttpHost(proxy.host,proxy.port);
                requestConfig.custom().setProxy(httpHost).build();
            }
            httppost.setConfig(requestConfig);

            //请求头
            httppost.addHeader("Accept", Accept);
            httppost.addHeader("Accept-Charset", Accept_Charset);
            httppost.addHeader("Accept-Encoding", Accept_EnCoding);
            httppost.addHeader("Accept-Language", Accept_Language);
            httppost.addHeader("User-Agent", User_Agent);
            if(cookie!=null){
                httppost.addHeader("cookie", cookie);
            }
            //服务器返回数据处理
            response = httpclient.execute(httppost);
            int status = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String location = null;
            if (status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES) {
                responseBody = entity != null ? EntityUtils.toString(entity) : "";
            } else if (status == HttpStatus.SC_MOVED_PERMANENTLY || status == HttpStatus.SC_MOVED_TEMPORARILY) {
                // 从头中取出转向的地址
                Header locationHeader = httppost.getLastHeader("location");
                if (locationHeader != null) {
                    location = locationHeader.getValue();
                    responseBody = location;
                } else {
                    responseBody = "";
                }
            } else {
                responseBody = "";
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

}
