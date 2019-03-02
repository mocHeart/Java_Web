package moc.utils;

import javax.servlet.http.Cookie;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Cookie查找的工具类
 */
public class CookieUtils {
    public static Cookie findCookie(Cookie[] cookies, String name) throws UnsupportedEncodingException {
        if(cookies == null)
            // 说明浏览器没有携带cookie
            return null;
        else {
            // 浏览器存放着cookie
            for (Cookie cookie : cookies){
                if(name.equals(cookie.getName())){
                    return cookie;
                }
            }
        return null;
        }
    }
}
