package org.smart4j.framework;

import org.apache.commons.lang3.StringUtils;
import org.smart4j.framework.bean.Data;
import org.smart4j.framework.bean.Handler;
import org.smart4j.framework.bean.Param;
import org.smart4j.framework.bean.View;
import org.smart4j.framework.helper.*;
import org.smart4j.framework.util.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求转发器
 */
@WebServlet(urlPatterns = "/*", loadOnStartup = 0)
public class DispatcherServlet extends HttpServlet {
    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        //初始化相关Helper
        HelperLoader.init();
        //获取 ServletContext 对象 注册Servlet
        ServletContext servletContext = servletConfig.getServletContext();
        //注册处理jsp的servlet
        ServletRegistration jspServlet = servletContext.getServletRegistration("jsp");
        jspServlet.addMapping(ConfigHelper.getAppJspPath() + "*");
        //注册静态资源的默认Servlet
        ServletRegistration defaultServlet = servletContext.getServletRegistration("default");
        defaultServlet.addMapping(ConfigHelper.getAppAssetPath() + "*");
        //初始化上传文件
        UploadHelper.init(servletContext);
    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        //获取请求方法和请求路径
        String requestMethod = req.getMethod().toLowerCase();
        String requestPath = req.getPathInfo();
        if (requestPath.equals("/favicon.ico"))
            return;
        //获取Action处理器
        Handler handler = ControllerHelper.getHandler(requestMethod, requestPath);
        if (handler != null) {
            //获取Controller类及其Bean实例
            Class<?> controllerClass = handler.getControllerClass();
            Object controllerBean = BeanHelper.getBean(controllerClass);
            Param param;
            if (UploadHelper.isMultipart(req)) {
                param = UploadHelper.createParam(req);
            } else {
                param = RequestHelper.createParam(req);
            }
            ServletHelper.init(req, res);
            //调用Action 方法
            Method actionMethod = handler.getActionMethod();
            Object result = null;
            try {
                //没有参数
                if (actionMethod.getParameterCount() == 0)
                    result = ReflectionUtil.invokeMethod(controllerBean, actionMethod);
                else
                    result = ReflectionUtil.invokeMethod(controllerBean, actionMethod, param);
                //处理Action 方法返回值
                if (result instanceof View) {
                    //返回jsp页面
                    handleViewResult((View) result, req, res);
                } else if (result instanceof Data) {
                    //返回json数据
                    handleDataResult((Data) result, res);
                }
            } finally {
                ServletHelper.destroy();
            }
        } else {
            res.sendError(404);
        }
    }

    private void handleDataResult(Data data, HttpServletResponse res) throws IOException {
        Object model = data.getModel();
        if (model != null) {
            String json = JsonUtil.toJson(model);
            res.setContentType("application/json");
            res.setCharacterEncoding("UTF-8");
            PrintWriter writer = res.getWriter();
            writer.write(json);
            writer.close();
        }
    }

    private void handleViewResult(View view, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        String path = view.getPath();
        if (StringUtils.isNotEmpty(path)) {
            if (path.startsWith("redirect:/")) {
                res.sendRedirect(req.getContextPath() + path.substring(9));
            } else {
                Map<String, Object> model = view.getModel();
                model.entrySet().stream().parallel().forEach(entry -> {
                    req.setAttribute(entry.getKey(), entry.getValue());
                });
                req.getRequestDispatcher(ConfigHelper.getAppJspPath() + path).forward(req, res);
            }
        }
    }


}
