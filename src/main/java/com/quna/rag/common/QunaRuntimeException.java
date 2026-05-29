package com.quna.rag.common;

/***
 *
 * Copyright（C）, Quna Private License 1.0, 2015-2020
 * @Description: QUNA 异常类 
 * @author tu.kai
 * @Date: 2020/11/25 21:01
 *
 */
public class QunaRuntimeException extends RuntimeException {

    private IRespCode respCode;
    private String message;

    /****
     * 自定义定义构造函数
     */
    public QunaRuntimeException(IRespCode arg0){

        // 调用父类的构造函数
        super(arg0.getRespMessage());

        // 自定义变量
        this.respCode = arg0;
        this.message = arg0.getRespMessage();
    }

    /****
     * 自定义定义构造函数
     */
    public QunaRuntimeException(IRespCode arg0, String arg1){

        // 调用父类的构造函数
        super(arg0.getRespMessage());

        // 自定义变量
        this.respCode = arg0;
        this.message = arg1;
    }

    /****
     * 自定义定义构造函数
     */
    public QunaRuntimeException(String arg1){
        // 自定义变量
        this.message = arg1;
    }

    public IRespCode getRespCode() {
        return respCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
