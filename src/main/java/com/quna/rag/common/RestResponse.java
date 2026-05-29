package com.quna.rag.common;

import com.quna.rag.util.EmptyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/***
 *
 * Copyright（C）, Mozilla Public License 1.1, 2015-2020
 * Description: Restful json Return Object
 * @author tu.kai@icloud.com
 * @version v1.0
 *
 */
public class RestResponse<E> implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(RestResponse.class);

    // 定义全局默认返回值 常量: 成功 || 失败 || 无权访问
    private final static int success = CmsConstants.YES;
    private final static int fail = CmsConstants.NO;
    private final static int dangerous = CmsConstants.FORBID;

    // == 标准返回参

    /****
     * 接口返回参：状态码
     */
    private int respCode;

    /**
     * 接口返回参：提示信息
     */
    private String respMessage;

    /****
     * AMIS-低代码-状态
     */
    private int status;

    /**
     * AMIS-低代码-错误信息
     */
    private String msg;

    /***
     * 接口返回参：数据对象
     */
    private E data;

    /****
     * 成功 - 无返回值
     * @author tu.kai@icloud.com
     *
     * @param <E>
     * @return
     */
    public static <E> RestResponse<E> success() {

        return new RestResponse<E>(success, CmsConstants.YES_CN);
    }

    /****
     * 成功 - 有返回值
     * @author tu.kai@icloud.com
     *
     * @param e
     * @param <E>
     * @return
     */
    public static <E> RestResponse<E> success(E e) {

        // 未传递返回值
        if(null == e){
            return new RestResponse<E>(success,  CmsConstants.YES_CN);
        }else {
            return new RestResponse<E>(success,  CmsConstants.YES_CN, e);
        }
    }

    /****
     * 失败 :: 自定义返回信息
     * @author tu.kai@icloud.com
     *
     * @param respMessage
     * @param <E>
     * @return
     */
    public static <E> RestResponse<E> fail(String respMessage) {

        if(EmptyUtils.isEmpty(respMessage)){

            log.warn("接口业务处理失败，有个不听话的码农没有返回错误信息");
            return new RestResponse<E>(fail, "系统异常");
        }

        return new RestResponse<E>(fail, respMessage);
    }

    /****
     * 失败 :: 自定义返回状态
     * @author tu.kai@icloud.com
     *
     * @param respCode
     * @param <E>
     * @return
     */
    public static <E> RestResponse<E> fail(IRespCode respCode, E e) {

        if(!vaildRespCode(respCode)){
            return new RestResponse<E>(fail, CmsConstants.NO_CN);
        }

        return fail(respCode, respCode.getRespMessage(), e);
    }

    /**
     * 失败 :: 自定义数据、返回提示信息
     * @param respMessage
     * @param e
     * @return
     * @param <E>
     */
    public static <E> RestResponse<E> fail(String respMessage, E e) {
        if(EmptyUtils.isEmpty(respMessage)){
            log.warn("接口业务处理失败，有个不听话的码农没有返回错误信息");
            return new RestResponse<E>(fail, "系统异常",e);
        }
        return new RestResponse<E>(fail, respMessage,e);
    }

    /****
     * 失败 :: 自定义返回状态
     * @author tu.kai@icloud.com
     *
     * @param respCode
     * @param <E>
     * @return
     */
    public static <E> RestResponse<E> fail(IRespCode respCode) {

        if(!vaildRespCode(respCode)){
            return new RestResponse<E>(fail, CmsConstants.NO_CN);
        }

        return fail(respCode, respCode.getRespMessage());
    }

    /****
     * 失败 :: 自定义返回状态，及信息
     * @author tu.kai@icloud.com
     *
     * @param respCode
     * @param respMessage
     * @param <E>
     * @return
     */
    public static <E> RestResponse<E> fail(IRespCode respCode, String respMessage) {

        if(!vaildRespCode(respCode)){

            return new RestResponse<E>(fail, CmsConstants.NO_CN);
        }

        // 缺省错误信息
        if(EmptyUtils.isEmpty(respMessage)){
            respMessage = respCode.getRespMessage();
        }

        return new RestResponse<E>(respCode.getRespCode(), respMessage);
    }

    public static <E> RestResponse<E> fail(IRespCode respCode, String respMessage, E e) {

        if(!vaildRespCode(respCode)){

            return new RestResponse<E>(fail, CmsConstants.NO_CN);
        }

        // 缺省错误信息
        if(EmptyUtils.isEmpty(respMessage)){
            respMessage = respCode.getRespMessage();
        }

        return new RestResponse<E>(respCode.getRespCode(), respMessage, e);
    }

    /*****
     * 校验响应状态码
     * @author tu.kai@icloud.com
     *
     * @param respCode
     * @return
     */
    private static boolean vaildRespCode(IRespCode respCode) {

        if(!EmptyUtils.isEmpty(respCode)
                && !EmptyUtils.isEmpty(respCode.getRespCode())
                && !EmptyUtils.isEmpty(respCode.getRespMessage())){

            return true;
        }

        log.warn("业务处理异常状态码信息读取失败, 派生类错误. || class: {}", respCode);
        return false;
    }

    /****
     * 判断请求响应是否成功
     * @author tu.kai@icloud.com
     *
     * @return
     */
    public boolean isSuccess(){

        if(success == this.respCode){
            return true;
        }

        return false;
    }


    // ==== 私有实例化 ========

    private RestResponse(int respCode, String respMessage, E e){
        this.respCode = respCode;
        this.respMessage = respMessage;
        this.data = e;
        this.status = respCode==CmsConstants.YES?0:respCode;
        if(respCode==CmsConstants.YES){
            this.status=0;
        }else if(respCode==CmsConstants.NO){
            this.status=1;
        }else{
            this.status=respCode;
        }
        this.msg = respMessage;
    }

    private RestResponse(int respCode, String respMessage){
        this.respCode = respCode;
        this.respMessage = respMessage;
        if(respCode==CmsConstants.YES){
            this.status=0;
        }else if(respCode==CmsConstants.NO){
            this.status=1;
        }else{
            this.status=respCode;
        }
        this.msg = respMessage;
    }

    private RestResponse(){
        this(RestResponse.dangerous, "无权访问");
    }

    // ====== GET AND SET =========

    public int getRespCode() {
        return respCode;
    }

    public void setRespCode(int respCode) {
        this.respCode = respCode;
    }

    public String getRespMessage() {
        return respMessage;
    }

    public void setRespMessage(String respMessage) {
        this.respMessage = respMessage;
    }

    public E getData() {
        return data;
    }

    public void setData(E data) {
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
