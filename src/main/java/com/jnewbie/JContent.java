package com.jnewbie;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * @program: jnewbie
 * @description: 获取
 * @author: pingc
 * @create: 2021-11-05 11:41
 **/
public class JContent {
    String content;
    List<String> xcontents;

    public List<String> getAll() {
        return xcontents;
    }
    public String get() {
        return xcontents.get(0);
    }

    public JContent(String content) {
        this.content = content;
    }

    //使用xpath抽取页面信息
    public JContent xpath(String xpath) {
        Xpath2Selector xpath2Selector = new Xpath2Selector(xpath);
        List<String> list = new ArrayList<>();
        if (xcontents != null) {
            for (String xcontent : xcontents) {

                list.add(xpath2Selector.selectList(Jsoup.parseBodyFragment(xcontent).outerHtml()).get(0));
            }
        }else{
            list = xpath2Selector.selectList(Jsoup.parseBodyFragment(content).outerHtml());
        }
        xcontents = list;
        return this;
    }

    //使用正则抽取页面信息
    public JContent regex(String regex) {
        List<String> list = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL | Pattern.MULTILINE);
        if (xcontents != null) {
            for (String xcontent : xcontents) {
                Matcher matcher = pattern.matcher(xcontent);
                if  (matcher.find()) {
                    list.add(matcher.group(1).toString());
                }
            }
        }else {
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                list.add(matcher.group(1).toString());
            }
        }
        xcontents = list;
        return this;
    }
    //使用replace替换页面信息
    public JContent replaceAll(String regex, String replacement){
        List<String> list = new ArrayList<>();
        if (xcontents != null) {
            for (String xcontent : xcontents) {
                String replace = xcontent.replaceAll(regex, replacement);
                    list.add(replace);
            }
        }else {
            String replace = content.replaceAll(regex, replacement);
                list.add(replace);
            }
        xcontents = list;
        return this;
    }

    public JContent replaceFirst(String regex, String replacement){
        List<String> list = new ArrayList<>();
        if (xcontents != null) {
            for (String xcontent : xcontents) {
                String replace = xcontent.replaceFirst(regex, replacement);
                list.add(replace);
            }
        }else {
            String replace = content.replaceFirst(regex, replacement);
            list.add(replace);
        }
        xcontents = list;
        return this;
    }

    //使用选择器抽取页面信息
    public Document getSelector(){
        Document document = Jsoup.parse(content);
        return document;
    }

    public String toString(){
        if (xcontents != null) {
                return xcontents.toString();
        }else {
            return content;
        }
    }
}
