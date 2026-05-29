package com.quna.rag.common;

/***
 * Copyright（C）, Mozilla Public License 1.1, 2015-2019
 * Author: tu.kai@icloud.com
 * Description: 前缀码
 */
public enum PrefixCode {

    //---------- 业务异常情况
    SERV(90),

    //---------- 校验参数不合法
    ILL(40),

    //---------- 系统异常
    SYSTEM(50),

    ;
	
    // ------------------ 构造器

    private int code;

    PrefixCode(int arg0){
        this.code = arg0;
    }

    public int getCode() {
        return code;
    }

}
