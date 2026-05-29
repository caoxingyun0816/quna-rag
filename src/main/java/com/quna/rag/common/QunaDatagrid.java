package com.quna.rag.common;

import com.google.common.collect.Lists;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/***
 *
 * Copyright（C）, Quna Private License 1.0, 2015-2020
 * @Description: vue 数据列表封装 
 * @author tu.kai
 * @Date: 2021/8/13 16:41
 *
 */
@Data
public class QunaDatagrid<T> implements Serializable {

    /****
     * 数据总数
     */
    private Long total;

    /****
     * 数据结果
     */
    private List<T> results = Lists.newArrayList();


    // ======== GET AND SET ===========


    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<T> getResults() {
        return results;
    }

    public void setResults(List<T> results) {
        this.results.addAll(results);
    }
}
