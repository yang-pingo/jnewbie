package com.jnewbie.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: jnewbie
 * @description: 一个url数据
 * @author: pingc
 * @create: 2021-11-06 10:36
 **/
@Data
public class JPage {
    private Header[] headers;
    private String content;
    private Integer code;
    private String url;
    private String redUrl;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Map<String,String> tag = new HashMap<>();

    private List<String> goUrl = new ArrayList<>();

    public JPage setTag(String key,String value){
        tag.put(key,value);
        return  this;
    }
    public String getTag(String key){
        String s = tag.get(key);
        return s;
    }
    public Map<String,String> getTagAll(){
        return tag;
    }
    public JPage setTagAll(Map<String,String> map){
        tag = map;
        return this;
    }

    public void addGoUrl(String url){
        goUrl.add(url);
    }
    public void addGoUrls(List<String> urls){
        goUrl.addAll(urls);
    }


    public JContent trim(){
        return new JContent(content).trim();
    }
    //使用xpath抽取页面信息
    public JContent xpath(String xptah){
            return new JContent(content).xpath(xptah);
    }
    //使用正则抽取页面信息
    public JContent regex(String regex){
        return new JContent(content).regex(regex);
    }
    //使用replace替换页面信息
    public JContent replaceAll(String regex, String replacement){
        return new JContent(content).replaceAll(regex,replacement);
    }

    public JContent replaceFirst(String regex, String replacement){
        return new JContent(content).replaceFirst(regex,replacement);
    }
    //使用选择器抽取页面信息
    public Document getSelector(){
        Document document = Jsoup.parse(content);
        return document;
    }
    public String getHeader (String name){
        for (Header header : headers) {
            if(header.getName().equals(name)){
                return header.getValue();
            }
        }
        return null;
    }


    public String toString(){
            return content;
        }


}





