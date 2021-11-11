package com.jnewbie.request;



import lombok.Data;
import org.apache.http.NameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: jnewbie
 * @description: 请求头
 * @author: pingc
 * @create: 2021-11-05 16:45
 **/
@Data
public class JHeader  implements NameValuePair {

    private String name;

    private String value;

    List<JHeader> JHeaders = new ArrayList<>();


    public JHeader() {}

    public JHeader(String name, String value) {
    }

    public void add(String name, String value){
        JHeaders.add(new JHeader(name,value));
    }


    public List<JHeader> getJHeaders(){
        return JHeaders;
    }
}
