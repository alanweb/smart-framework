package org.smart4j.framework;

import org.smart4j.framework.helper.*;
import org.smart4j.framework.util.ClassUtil;

import java.util.Arrays;

/**
 * 加载相应的Helper类
 */
public final class HelperLoader {
    public static void init() {
        Class<?>[] classList = {
                ClassHelper.class,
                BeanHelper.class,
                AopHelper.class,
                IocHelper.class,
                ControllerHelper.class
        };
        Arrays.stream(classList).forEach(cls -> {
            ClassUtil.loadClass(cls.getName());
        });
    }
}
