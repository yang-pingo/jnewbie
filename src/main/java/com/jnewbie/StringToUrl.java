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
//Unicode转中文
    public static String convertUnicode(String ori){
        char aChar;
        int len = ori.length();
        StringBuffer outBuffer = new StringBuffer(len);
        for (int x = 0; x < len;) {
            aChar = ori.charAt(x++);
            if (aChar == '\\') {
                aChar = ori.charAt(x++);
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = ori.charAt(x++);
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed   \\uxxxx   encoding.");
                        }
                    }
                    outBuffer.append((char) value);
                } else {
                    if (aChar == 't')
                        aChar = '\t';
                    else if (aChar == 'r')
                        aChar = '\r';
                    else if (aChar == 'n')
                        aChar = '\n';
                    else if (aChar == 'f')
                        aChar = '\f';
                    outBuffer.append(aChar);
                }
            } else
                outBuffer.append(aChar);

        }
        return outBuffer.toString();
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
