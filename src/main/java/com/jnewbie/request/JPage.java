package com.jnewbie.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.*;

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
    private byte[] bytes;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    volatile Map<String,Object> tag = new HashMap<>();
    volatile List<Object> list =  Collections.synchronizedList(new ArrayList<Object>());
    volatile List<String> goUrl = Collections.synchronizedList(new ArrayList<String>());

    public JPage addTag(String key,Object value){
        tag.put(key,value);
        return  this;
    }
    public Boolean containsTag(String key){
        Set<String> strings = tag.keySet();
        return  strings.contains(key);
    }
    public Object getTag(String key){
        return tag.get(key);
    }
    public Map<String,Object> getTagAll(){
        return tag;
    }
    public JPage setTagAll(Map<String,Object> map){
        tag = map;
        return this;
    }
    public Boolean addList(Object value){
        return  list.add(value);
    }
    public Boolean containsList(Object key){
        return  list.contains(key);
    }
    public Object getList(Integer key){
        return list.get(key);
    }
    public List<Object> getListAll(){
        return list;
    }
    public JPage setListAll(List<Object> list){
        this.list = list;
        return this;
    }
    public Object delTag(String key){
        return tag.remove(key);
    }
    public void addGoUrl(String url){
        goUrl.add(url);
    }
    public void addGoUrls(List<String> urls){
        goUrl.addAll(urls);
    }

    public boolean delGoUrl(String url){
        return goUrl.remove(url);
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





