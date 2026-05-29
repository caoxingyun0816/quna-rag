package com.quna.rag.common;

import com.google.common.collect.Lists;
import com.quna.rag.util.EmptyUtils;

import java.util.ArrayList;
import java.util.List;

/***
 *
 * Copyright（C）, Quna Private License 1.0, 2015-2020
 * @Description: 分页查询数据类型
 * @author tu.kai
 * @Date: 2021/8/14 16:52
 *
 */
public class Page<T> extends ArrayList<T> {

    private Long total;

    public Page(){
        super();
    }

    public Page(long total){
        super();
        this.total = total;
    }

    public Page(List<T> dataList, long total){
        super();

        if(EmptyUtils.isEmpty(dataList)){
            dataList = Lists.newArrayList();
        }

        this.addAll(dataList);
        this.total = total;
    }

    public QunaDatagrid<T> toQunaDatagrid() {

        QunaDatagrid<T> datagrid = new QunaDatagrid<T>();
        datagrid.setTotal(this.total);
        datagrid.setResults(this);

        return datagrid ;
    }

    // ======= GET AND SET ====

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }
}
