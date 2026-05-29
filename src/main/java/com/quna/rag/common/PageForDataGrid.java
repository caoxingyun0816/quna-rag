package com.quna.rag.common;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.List;

/***
 *
 * Copyright（C）, Quna Private License 1.0, 2015-2020
 * @Description: 数据列表分页标准对象
 * @author tu.kai
 * @Date: 2021/4/25 10:54
 *
 */
public class PageForDataGrid<T> implements Serializable {

    /****
     * 默认页码: 1
     */
    private final String DATAGRID_PAGE = "pageNumKey";
    private final Integer DATAGRID_PAGE_DEFAULT = 1;

    /***
     * 默认每页记录数: 20
     */
    private final String DATAGRID_PERPAGE = "pageSizeKey";
    private final Integer DATAGRID_PERPAGE_DEFAULT = 20;

    /***
     * 默认页数: 当前页数
     */
    private Integer current;

    /***
     * 默认每页条数
     */
    private Integer pageSize;

    /***
     * 总页码
     */
    private Integer pages;

    /****
     * 数据总数
     */
    private Integer total;

    /****
     * 数据结果
     */
    @JSONField(name = "data")
    private List<T> result = Lists.newArrayList();

    // ============ 自定义方法,构造函数


    public PageForDataGrid() {
    }

    public PageForDataGrid(Integer total, List<T> result) {
        this.total = total;
        this.result = result;
    }

    public PageForDataGrid(Integer current, Integer pageSize) {
        this.current = current;
        this.pageSize = pageSize;
        this.total = 0;
    }

    public PageForDataGrid(Integer current, Integer pageSize, Integer total, List<T> result) {
        this.current = current;
        this.pageSize = pageSize;
        this.total = total;
        this.result = result;
    }

    public void perfectness() {
        int pages = total / pageSize;

        if (total % pageSize > 0) {
            pages += 1;
        }

        this.pages = pages;
    }


    // ============= GET AND SET

    public Integer getCurrent() {
        return current;
    }

    public void setCurrent(Integer current) {
        this.current = current;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }
}
