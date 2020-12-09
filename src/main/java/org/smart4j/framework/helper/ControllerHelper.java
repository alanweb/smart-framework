package org.smart4j.framework.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smart4j.framework.annotation.Action;
import org.smart4j.framework.bean.Handler;
import org.smart4j.framework.bean.Request;
import org.smart4j.framework.util.ArrayUtil;
import org.smart4j.framework.util.CollectionUtil;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 控制器助手类
 */
public final class ControllerHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerHelper.class);
    /**
     * 存放请求与处理器的映射关系
     */
    private static final Map<Request, Handler> ACTION_MAP = new HashMap<>();

    static {
        //获取所有的Controller类
        Set<Class<?>> controllerClassSet = ClassHelper.getControllerClassSet();
        if (CollectionUtil.isNotEmpty(controllerClassSet)) {
            //遍历全部的Controller类
            controllerClassSet.parallelStream().forEach(controllerClass -> {
                //获取Controller类中定义的方法
                Method[] methods = controllerClass.getDeclaredMethods();
                if (ArrayUtil.isNotEmpty(methods)) {
                    //遍历存在Action注解的方法
                    Arrays.stream(methods).filter(method -> method.isAnnotationPresent(Action.class)).forEach(method -> {
                        //从Action注解中获取URL映射规则
                        Action action = method.getAnnotation(Action.class);
                        String mapping = action.value();
                        //验证URL映射规则
                        if (mapping.matches("\\w+:/\\w*")) {
                            String[] arr = mapping.split(":");
                            if (ArrayUtil.isNotEmpty(arr) && arr.length == 2) {
                                //获取请求方法与请求路径
                                String requestMethod = arr[0];
                                String requestPath = arr[1];
                                Request request = new Request(requestMethod, requestPath);
                                Handler handler = new Handler(controllerClass, method);
                                //初始化Action Map
                                ACTION_MAP.put(request, handler);
                            }
                        }
                    });
                }
            });
        }
    }

    /**
     * 获取 Handler
     * @param requestMethod
     * @param requestPath
     * @return
     */
    public static Handler getHandler(String requestMethod,String requestPath){
        return ACTION_MAP.get(new Request(requestMethod,requestPath));
    }
}
