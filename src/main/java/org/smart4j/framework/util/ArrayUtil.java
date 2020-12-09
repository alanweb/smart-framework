package org.smart4j.framework.util;

import org.apache.commons.lang3.ArrayUtils;

/**
 * 数组工具类
 */
public final class ArrayUtil {

    /**
     * 判断数组是否为空
     *
     * @param arr
     * @return
     */
    public static boolean isEmpty(Object[] arr) {
        return ArrayUtils.isEmpty(arr);
    }

    /**
     * 判断数组是否不为空
     *
     * @param arr
     * @return
     */
    public static boolean isNotEmpty(Object[] arr) {
        return !isEmpty(arr);
    }
}
