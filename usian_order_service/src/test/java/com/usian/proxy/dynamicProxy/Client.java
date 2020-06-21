package com.usian.proxy.dynamicProxy;

import com.usian.proxy.staticProxy.RealStar;
import com.usian.proxy.staticProxy.Star;

import java.lang.reflect.Proxy;

public class Client {
    /*public static void main(String[] args) {
        Star realStar = new RealStar();
        ProxyStar handler = new ProxyStar(realStar);
        Star proxy = (Star) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
                //Start：要生哪个接口的代理类
                //handler：代理类要做的事情
                new Class[] { Star.class },handler);
        proxy.sing();
    }*/
    public static void main(String[] args) {
        RealStar realStar = (RealStar) CglibProxy.newProxyInstance(RealStar.class);
        realStar.sing();
    }
}
