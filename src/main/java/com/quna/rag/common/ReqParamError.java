package com.quna.rag.common;

import java.io.Serializable;

/***
 *
 * Copyright（C）, Quna Private License 1.0, 2015-2020
 * @Description: 参数错误 自定义 
 * @author tu.kai
 * @Date: 2020/11/30 16:49
 *
 */
public class ReqParamError implements Serializable {

    /***
     * 错误对象字段名称
     */
    private String field;

    /***
     * 错误信息
     */
    private String message;

    /***
     * 错误对象
     */
    private String objectName;

    // =============== GET AND SET ============

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

}
