package com.quna.rag.util;

import java.util.Collection;
import java.util.Map;

/***
 *
 * Copyright（C）, Quna Private License 1.0, 2015-2020
 * @Description: 判断空值工具类
 * @author tu.kai
 * @Date: 2020/7/20 18:05
 *
 */
public class EmptyUtils {

    /***
     * 判断字符串是否为空，长度为0被认为是空字符串
     * @author tu.kai@icloud.com
     *
     * @param str
     * @return true or false
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /***
     * 判断列表是否为空，列表没有元素也被认为是空
     * @author tu.kai@icloud.com
     *
     * @param collection
     * @return true or false
     */
    @SuppressWarnings("rawtypes")
    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.size() == 0;
    }

    /**
     * 判断map是否为空,没有元素也被认为是空
     * @author tu.kai@icloud.com
     *
     * @param map
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static boolean isEmpty(Map map) {
        return map == null || map.size() == 0;
    }

    /***
     * 判断数组是否为空
     * @author tu.kai@icloud.com
     *
     * @param array
     * @return true or false

     */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /***
     * 判断对象是否为空
     * @author tu.kai@icloud.com
     *
     * @param obj
     * @return true or false
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        } else {
            return isEmpty(obj.toString());
        }
    }

    /***
     * 判断字符串是否为空，长度为0被认为是空字符串
     * @author tu.kai@icloud.com
     *
     * @param str
     * @return true or false
     */
    public static boolean isNotEmpty(String str) {
        return !EmptyUtils.isEmpty(str);
    }

    /***
     * 判断列表是否为空，列表没有元素也被认为是空
     * @author tu.kai@icloud.com
     *
     * @param collection
     * @return true or false
     */
    @SuppressWarnings("rawtypes")
    public static boolean isNotEmpty(Collection collection) {
        return !EmptyUtils.isEmpty(collection);
    }

    @SuppressWarnings("rawtypes")
    public static boolean isNotEmpty(Map map) {
        return !EmptyUtils.isEmpty(map);
    }

    /***
     * 判断数组是否为空
     * @author tu.kai@icloud.com
     *
     * @param array
     * @return true or false

     */
    public static boolean isNotEmpty(Object[] array) {
        return !EmptyUtils.isEmpty(array);
    }

    /***
     * 判断对象是否为空
     * @author tu.kai@icloud.com
     *
     * @param obj
     * @return true or false
     */
    public static boolean isNotEmpty(Object obj) {
        return !EmptyUtils.isEmpty(obj);
    }

    // == 禁止实例化 ==
    private EmptyUtils(){}
}
