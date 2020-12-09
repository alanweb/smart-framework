package org.smart4j.framework.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回数据对象
 */
public class Data {
    /**
     * 模型数据
     */
    private Object model;

    public Data(Object model) {
        this.model = model;
    }

    public Object getModel() {
        return model;
    }
}
