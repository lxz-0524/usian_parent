package com.usian.proxy.dynamicProxy;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class CglibProxy implements MethodInterceptor {

    private CglibProxy() {
    }

    public static <T extends Object> Object newProxyInstance(Class<T> targetInstanceClazz) {
        // 生成真实角色的子类(代理角色)，在子类中调用父类的方法
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetInstanceClazz);
        enhancer.setCallback(new CglibProxy());
        return enhancer.create();
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        Object object = null;

        System.out.println("真正的方法执行前！");
        System.out.println("面谈，签合同，预付款，订机票");

        object = methodProxy.invokeSuper(obj, args);

        System.out.println("真正的方法执行后！");
        System.out.println("收尾款");
        return object;
    }
}
