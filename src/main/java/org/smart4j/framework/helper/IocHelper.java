package org.smart4j.framework.helper;

import org.smart4j.framework.annotation.Inject;
import org.smart4j.framework.util.ArrayUtil;
import org.smart4j.framework.util.CollectionUtil;
import org.smart4j.framework.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

/**
 * 依赖注入助手类
 */
public final class IocHelper {
    static {
        // 获取所有的Bean类与Bean实例之间的映射关系
        Map<Class<?>, Object> beanMap = BeanHelper.getBeanMap();
        if (CollectionUtil.isNotEmpty(beanMap)) {
            beanMap.entrySet().stream().forEach(beanEntry -> {
                //获取bean类和bean实例
                Class<?> beanClass = beanEntry.getKey();
                Object beanInstance = beanEntry.getValue();
                //获取bean类定义的所有成员变量
                Field[] beanFields = beanClass.getDeclaredFields();
                if (ArrayUtil.isNotEmpty(beanFields)) {
                    //遍历有Inject注解的bean field
                    Arrays.stream(beanFields).filter(beanField -> beanField.isAnnotationPresent(Inject.class)).forEach(beanField -> {
                        //从beanMap 中获取 bean field 对应的实例
                        Class<?> beanFieldClass = beanField.getType();
                        Object beanFieldInstance = beanMap.get(beanFieldClass);
                        if (beanFieldInstance != null) {
                            //通过反射初始化bean field的值
                            ReflectionUtil.setField(beanInstance, beanField, beanFieldInstance);
                        }
                    });
                }
            });
        }
    }
}
