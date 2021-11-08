package com.jnewbie;



import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: jnewbie
 * @description: post参数
 * @author: pingc
 * @create: 2021-11-05 16:45
 **/
public class Param {


    List<NameValuePair> nameValuePairs = new ArrayList<>();


    public Param() {
    }

    public void add(String name, String value){
        nameValuePairs.add(new BasicNameValuePair(name, value));
    }
    public UrlEncodedFormEntity getUrlEncodedFormEntity() throws UnsupportedEncodingException {
        return new UrlEncodedFormEntity(nameValuePairs);
    }

    public List<NameValuePair> getNameValuePairs(){
        return nameValuePairs;
    }
}
