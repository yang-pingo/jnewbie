package com.jnewbie.request;



import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: jnewbie
 * @description: 参数
 * @author: pingc
 * @create: 2021-11-05 16:45
 **/
public class JParam {


    List<NameValuePair> nameValuePairs = new ArrayList<>();


    public JParam() {
    }

    public JParam add(String name, String value){
        nameValuePairs.add(new BasicNameValuePair(name, value));
        return this;
    }
    public UrlEncodedFormEntity getUrlEncodedFormEntity() throws UnsupportedEncodingException {
        return new UrlEncodedFormEntity(nameValuePairs,"utf-8");
    }

    public List<NameValuePair> getNameValuePairs(){
        return nameValuePairs;
    }
}
