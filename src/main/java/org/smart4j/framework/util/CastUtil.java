package org.smart4j.framework.util;

/**
 * 转换类型工具类
 */
public class CastUtil {
    public static int castInt(Object obj) {
        if(obj!=null)
            return Integer.parseInt(castString(obj));
        return 0;
    }
    public static long castLong(Object obj) {
        if(obj!=null)
            return Long.parseLong(castString(obj));
        return 0l;
    }
    public static boolean castBoolean(Object obj) {
        if(obj!=null)
          return Boolean.parseBoolean(castString(obj));
        return false;
    }

    public static String castString(Object obj) {
        if (obj!=null){
            return String.valueOf(obj);
        }
        return "";
    }

}
