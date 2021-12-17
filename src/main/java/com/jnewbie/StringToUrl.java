package com.jnewbie;

/**
 * @program: jnewbie
 * @description: URL编码转换
 * @author: pingc
 * @create: 2021-11-12 21:34
 **/
public class StringToUrl {

    public static String to(String url){
        String u = url;
        try {
            u = re(java.net.URLEncoder.encode(url, "utf-8"));
        }catch (Exception e){
        }
        return u;
    }


    private static String re(String s){
       return  s.replace("%25","%")
                .replace("%5E","^")
               .replace("+"," ")
                .replace("%26","&")
               .replace("&amp;","&")
                .replace("%2F","/")
               .replace("%7B","{")
               .replace("%7D","}")
                .replace("%3F","?")
                .replace("%5B","[")
                .replace("%5D","]")
               .replace("%27","'")
                .replace("%EF%BC%81","！")
               .replace("%3A",":")
                .replace("%EF%BC%9A","：")
                .replace("%E2%80%99","’")
                .replace("%2C",",")
                .replace("%E3%80%82","。")
                .replace("%22","\"")
                .replace("%E2%80%9D","”")
                .replace("%E2%80%94%E2%80%94","——")
                .replace("%2B","+")
                .replace("%EF%BC%88","（")
                .replace("%EF%BC%89","）")
                .replace("%40","@")
                .replace("%24","$")
                .replace("%EF%BF%A5","￥")
                .replace("%23","#")
                .replace("%E2%80%98","‘")
                .replace("%E2%80%9C","“")
                .replace("%E2%80%9D","”")
                .replace("%3D","=");
    }
}
