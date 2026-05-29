package com.quna.rag.common;

/***
 *
 * Copyright（C）, Quna Private License 1.0, 2015-2020
 * @Description: CMS 自有常量类 
 * @author tu.kai
 * @Date: 2020/11/25 20:17
 *
 */
public interface CmsConstants {

    /** 是|启用|有效 */
    public static final int YES = 1;

    /** 汉化 **/
    public static final String YES_CN = "成功";

    /** 否|禁用|无效 */
    public static final int NO = 0;

    /** 汉化 **/
    public static final String NO_CN = "失败";

    /** 禁止 **/
    public static final int FORBID = 403;

    /***
     * 默认 页总数
     */
    public static final int DEFAULT_PAGESIZE = 20;

    /****
     * 用户ID 默认长度
     */
    public static final int USER_ID_DEFAULT_LENGTH = 32;

    /**
     * 机器排期详情，下一台 redis key prefix
     */
    public static final String MACHINE_SKU_CYCLE_REDIS_KEY="machine_sku_cycle_next_";

    /**
     * 操作用户
     */
    String OPERATOR = "QZ-Access-User";

    /**
     * 指定sku上下架冲突信息
     */
    String ERROR_MSG = "sku：%s与计划：%s冲突";

}
