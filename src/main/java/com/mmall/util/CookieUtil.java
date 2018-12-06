package com.mmall.util;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by changlei on 2018/11/22.
 */
@Slf4j
public class CookieUtil {

    private final static String COOKIE_DOMAIN = "localhost" ;

    private final static String COOKIE_NAME = "mmall_login_token";
// a,b,c,d,e(都是二级域名) 都可以共享 x的cookie ;
//  a,b拿不到相互的cookie
// b,c 可以共享 a 的cookie  ,      c,d,e 可以共享 a 的cookie
// c,d 可以共享 e的cookie
// d,e
//   X:domain="cl.com"
//   a:A.cl.com                    cookie:domain=A.cl.com;path="/"
//   b:B.cl.com                    cookie:domain=B.cl.com;path="/"
//   c:A.cl.com/test/cc            cookie:domain=A.cl.com;path="/test/cc"
//   d:A.cl.com/test/dd            cookie:domain=A.cl.com;path="/test/dd"
//   e:A.cl.com/test/              cookie:domain=A.cl.com;path="/test"
    public static void writeLoginToken(HttpServletResponse response, String token ){
        Cookie cookie = new Cookie(COOKIE_NAME,token);
        cookie.setDomain(COOKIE_DOMAIN);
        cookie.setPath("/");     // /代表根目录
        cookie.setHttpOnly(true);  // 不允许使用脚本访问cookie ,只允许http 访问

        cookie.setMaxAge(60 * 60 * 365); // -1 代表永久，单位秒，如果不设置不会写入硬盘，指挥写入内存
        log.info("wirte cookiname:{}, cokievalue:{}",cookie.getName(),cookie.getValue());
        response.addCookie(cookie);
    }

    public static String readLoginToken(HttpServletRequest request){
        Cookie[] cks = request.getCookies();
        if(cks != null){
            for(Cookie ck:cks){
               log.info("read cookieName:{},cookieValue:{}",ck.getName(),ck.getValue());
                if(COOKIE_NAME.equals(ck.getName())){
                    log.info("return cookieName:{},cookieValue:{}",ck.getName(),ck.getValue());
                    return ck.getValue();
                }
            }
        }
        return null;
    }

    public static void delLoginToken(HttpServletRequest request,HttpServletResponse response){
        Cookie[] cks = request.getCookies();
        if(cks != null){
            for(Cookie ck:cks){
                log.info("read cookieName:{},cookieValue:{}",ck.getName(),ck.getValue());
                if(COOKIE_NAME.equals(ck.getName())){
                    log.info("return cookieName:{},cookieValue:{}",ck.getName(),ck.getValue());
                    ck.setDomain(COOKIE_DOMAIN);
                    ck.setPath("/");
                    ck.setMaxAge(0); // 删除此cookie
                    response.addCookie(ck);
                }
            }
        }
    }

}
