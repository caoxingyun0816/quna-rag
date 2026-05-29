package com.quna.rag.common;

/***
 *
 * Copyright（C）, Mozilla Public License 1.1, 2015-2020
 * Description: Rest Response 请求响应状态码 :: 支持自定义
 * @author tu.kai@icloud.com
 * @version v1.0
 *
 */
public interface IRespCode{


    /***
     * 规范返回结果值
     * @author tu.kai@icloud.com
     */
    int getRespCode();

    /***
     * 规范返回结果消息
     * @author tu.kai@icloud.com
     */
    String getRespMessage();

}
