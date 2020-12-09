package org.smart4j.framework.helper;

import org.smart4j.framework.util.ReflectionUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Bean 助手类
 */
public final class BeanHelper {
    /**
     * 定义bean映射表 存放bean类和bean实例的映射关系
     */
    private static final Map<Class<?>, Object> BEAN_MAP = new HashMap<Class<?>, Object>();

    static {
        Set<Class<?>> beanClassSet = ClassHelper.getBeanClassSet();
        beanClassSet.stream().forEach(beanClass -> {
            BEAN_MAP.put(beanClass, ReflectionUtil.newInstance(beanClass));
        });
    }

    /**
     * 获取 bean 映射
     *
     * @return
     */
    public static Map<Class<?>, Object> getBeanMap() {
        return BEAN_MAP;
    }

    /**
     * 获取 bean 实例
     *
     * @param cls
     * @return
     */
    public static <T> Object getBean(Class<T> cls) {
        if (!BEAN_MAP.containsKey(cls))
            throw new RuntimeException("can not get bean by class: " + cls);
        return (T) BEAN_MAP.get(cls);
    }

    /**
     * 设置 bean 实例
     * @param cls
     * @param bean
     */
    public static void setBean(Class<?> cls, Object bean) {
        BEAN_MAP.put(cls, bean);
    }
}
