package com.quna.rag.common;

import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


/***
 *
 * Copyright（C）, Quna Private License 1.0, 2015-2020
 * @Description: 基础Action
 * @author tu.kai
 * @Date: 2020/11/25 21:16
 *
 */
@Slf4j
@ControllerAdvice
public abstract class BasicAction {

    public abstract Logger getLogger();

    /****
     * 统一捕获自定义趣拿业务异常
     * @author tu.kai
     *
     */
    @ExceptionHandler(QunaRuntimeException.class)
    @ResponseBody
    public RestResponse<String> handlerQunaRuntimeException(final HttpServletRequest request, final QunaRuntimeException ex) {

        ex.printStackTrace();

        if(null != ex.getRespCode()){
            this.getLogger().error("[访问 {} 异常] 业务逻辑异常原因: {}", request.getRequestURL(), ex.getRespCode().getRespMessage(), ex);
            return RestResponse.fail(ex.getRespCode());
        }

        return RestResponse.fail(ex.getMessage());
    }

    /****
     * 统一捕获接口请求参数不符合要求
     * @author tu.kai
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public RestResponse<List<ReqParamError>> handlerRequestParamException(final MethodArgumentNotValidException ex) {

        ex.printStackTrace();
        this.getLogger().warn("target controller advice handler request param ex", ex);

        BindingResult bindingResult = ex.getBindingResult();
        List<ObjectError> allErrors = bindingResult.getAllErrors();
        List<ReqParamError> reqParamErrors = Lists.newArrayList();

        allErrors.forEach(objectError -> {
            ReqParamError errorMsg = new ReqParamError();
            FieldError fieldError = (FieldError)objectError;
            errorMsg.setField(fieldError.getField());
            errorMsg.setObjectName(fieldError.getObjectName());
            errorMsg.setMessage(fieldError.getDefaultMessage());
            reqParamErrors.add(errorMsg);
        });

        return RestResponse.fail(QunaExCode.ILLEGAL_REQ_PARAM, reqParamErrors);
    }

    /****
     * 统一捕获请求参数缺少
     * @author tu.kai
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public RestResponse<ReqParamError> handlerServletRequestParameterException(final MissingServletRequestParameterException ex) {

        ex.printStackTrace();
        this.getLogger().warn("target controller advice handler servlet request param ex", ex);

        ReqParamError reqParamError = new ReqParamError();
        reqParamError.setField(ex.getParameterName());
        reqParamError.setMessage(ex.getMessage());

        return RestResponse.fail(QunaExCode.ILLEGAL_REQ_PARAM, reqParamError);
    }

    /****
     * 统一捕获运行时异常
     *  包含 自定义异常
     * @author tu.kai
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public RestResponse<String> handlerRuntimeException(final RuntimeException ex) {
        log.error("exception,e:", ex);
        ex.printStackTrace();
        this.getLogger().error("target controller advice handler runtime ex", ex);

        return RestResponse.fail(ex.getMessage());
    }

    /****
     * 捕获超运行时异常的严重异常情况
     * @author tu.kai
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public RestResponse<String> handlerException(final Exception ex) {

        ex.printStackTrace();
        this.getLogger().error("target controller advice handler system ex");

        this.getLogger().error("CMS Exception: {}", ex.getMessage(), ex);
        return RestResponse.fail("系统异常");
    }

}
