package com.quna.rag.common;

import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/***
 *
 * Copyright（C）, Quna Private License 1.0, 2015-2020
 * @Description: CMS 基础实体类 
 * @author tu.kai
 * @Date: 2021/6/1 19:08
 *
 */
public abstract class BasicEntity implements Serializable {

    /***
     * 数据ID
     */
    private long id;

    /***
     * 数据创建时间
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date ct;

    /***
     * 数据编辑时间
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date mt;


    // ============= GET AND SET =============

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public Date getCt() {
        return ct;
    }

    public void setCt(Date ct) {
        this.ct = ct;
    }

    public Date getMt() {
        return mt;
    }

    public void setMt(Date mt) {
        this.mt = mt;
    }

}
