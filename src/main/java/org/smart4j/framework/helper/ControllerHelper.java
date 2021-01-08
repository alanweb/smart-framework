package org.smart4j.framework.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smart4j.framework.annotation.GetMapping;
import org.smart4j.framework.annotation.PostMapping;
import org.smart4j.framework.annotation.RequestMapping;
import org.smart4j.framework.bean.Handler;
import org.smart4j.framework.bean.Request;
import org.smart4j.framework.util.ArrayUtil;
import org.smart4j.framework.util.CollectionUtil;
import org.smart4j.framework.util.StringUtil;

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
            controllerClassSet.forEach(controllerClass -> {
                String root = "/";
                //判断类是否有RequestMapping注解
                if (controllerClass.isAnnotationPresent(RequestMapping.class)) {
                    root += controllerClass.getAnnotation(RequestMapping.class).value();
                }
                final String base = root;
                //获取Controller类中定义的方法
                Method[] methods = controllerClass.getDeclaredMethods();
                if (ArrayUtil.isNotEmpty(methods)) {
                    //遍历存在RequestMapping注解的方法
                    Arrays.stream(methods).filter(method -> method.isAnnotationPresent(RequestMapping.class)).forEach(method -> {
                        //从Action注解中获取URL映射规则
                        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                        String mapping = requestMapping.value();
                        Arrays.stream(new String[]{"get", "post"}).forEach(e -> {
                            setAction(controllerClass, method, base, mapping, e);
                        });
                    });
                    //遍历存在GetMapping注解的方法
                    Arrays.stream(methods).filter(method -> method.isAnnotationPresent(GetMapping.class)).forEach(method -> {
                        //从Action注解中获取URL映射规则
                        GetMapping getMapping = method.getAnnotation(GetMapping.class);
                        String mapping = getMapping.value();
                        setAction(controllerClass, method, base, mapping, "get");
                    });
                    //遍历存在PostMapping注解的方法
                    Arrays.stream(methods).filter(method -> method.isAnnotationPresent(PostMapping.class)).forEach(method -> {
                        //从Action注解中获取URL映射规则
                        PostMapping postMapping = method.getAnnotation(PostMapping.class);
                        String mapping = postMapping.value();
                        setAction(controllerClass, method, base, mapping, "post");
                    });
                }
            });
        }
    }

    private static void setAction(Class<?> controllerClass, Method method, String base, String mapping, String e) {
        if (StringUtil.isNotEmpty(mapping))
            mapping = "/" + mapping;
        String requestPath = (base + mapping).replaceAll("/{1,}", "/");
        Request request = new Request(e, requestPath);
        Handler handler = new Handler(controllerClass, method);
        //初始化Action Map
        ACTION_MAP.put(request, handler);
    }

    /**
     * 获取 Handler
     *
     * @param requestMethod
     * @param requestPath
     * @return
     */
    public static Handler getHandler(String requestMethod, String requestPath) {
        return ACTION_MAP.get(new Request(requestMethod, requestPath));
    }
}
