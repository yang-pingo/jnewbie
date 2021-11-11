package com.jnewbie;

import lombok.Data;

/**
 * @program: jnewbie
 * @description: 代理
 * @author: pingc
 * @create: 2021-11-05 18:27
 **/
@Data
public class JProxy {
    public String host;
    public Integer port;
    public String username;
    public String password;
    public JProxy(){
    }

    public JProxy(String host,Integer port){
        this.host = host;
        this.port = port;
    }
    public JProxy(String host,Integer port,String username ,String password){
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }
}
