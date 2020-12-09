package org.smart4j.framework.proxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.smart4j.framework.helper.BeanHelper;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 代理管理器
 */
public class ProxyManager {
    /**
     * 创建代理
     * @param targetClass
     * @param proxyList
     * @return
     */
    public static <T> T createProxy(final Class<?> targetClass, final List<Proxy> proxyList) {
        return (T) Enhancer.create(targetClass, new MethodInterceptor() {
            @Override
            public Object intercept(Object targetObject, Method targetMethod, Object[] methodParams, MethodProxy methodProxy) throws Throwable {
                return  new ProxyChain(targetClass,targetObject , targetMethod, methodProxy, methodParams, proxyList).doProxyChain();
            }
        });
        //return (T) Enhancer.create(targetClass, (MethodInterceptor) (targetObject, targetMethod, methodParams, methodProxy) -> new ProxyChain(targetClass, BeanHelper.getBean(targetClass), targetMethod, methodProxy, methodParams, proxyList).doProxyChain());
    }
}
