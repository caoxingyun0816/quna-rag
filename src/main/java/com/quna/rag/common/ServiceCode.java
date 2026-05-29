package com.quna.rag.common;

/***
 *
 * Copyright（C）, Quna Private License 1.0, 2015-2020
 * @Description: 业务模块码
 * @author tu.kai
 * @Date: 2020/11/30 17:06
 *
 */
public enum ServiceCode {

    /***
     * 系统基础相关
     */
    SYSTEM(01),

    /***
     * Web网页版
     */
    SYSTEM_MICRO(02),

    /***
     * Web网页版
     *  远程控制机器直播
     */
    SYSTEM_MICRO_CONTROL(03),


    /**
     * 问卷重置
     *
     * @author gzq
     * @date 2022/6/29 17:06
     */
    QUESTION(04),

    /***
     * 机器管理业务
     */
    MACHINE(10),

    USER(20),

    BONUS(30),

    /**
     * 提现
     */
    WITHDRAW(40),

    /**
     * 趣赚钱用户
     */
    STAND_USER(50),

    /**
     * 2021_99
     */
    SCRIPT(60),

    /**
     * 京东派发活动
     */
    JD_FREE_SAMPLE(70),

    /**
     * 明星生日
     */
    SUPER_STAR(75),

    /**
     * 社区团购计划
     */
    GROUP_BUY(80),

    /**
     * 趣赚钱相关业务
     */
    STAND(90),

    /*** 共有业务码 ***/
    COMMON(90),

    /**
     * 点位合同管理
     */
    POINT_CONTRACT(110),

    /**
     * 招商项目
     */
    INVESTMENT_PROJECT(120),

    /**
     * 数字藏品
     */
    DIGITAL_PERIPHERY(130),

    /**
     * 权限管理
     */
    AUTH(140),

    /**
     * 虚拟点位
     */
    VIRTUAL_POINT(150),

    /**
     * 终端运营
     */
    TERMINAL_OPERATION(160),

    /**
     * 可视化排货
     */
    VISUAL_MACHINE_SKU_CYCLE(170),

    /**
     * 商品加购配置
     */
    MACHINE_EXPAND_CONFIG(180),

    /**
     * 趣选派样
     */
    BM(190),

    /**
     * 社群
     */
    ASSOCIATION(200),

    /**
     * 社群抽奖
     */
    SYNTHETICAL(210),

    /**
     * 精选点位
     */
    EXCELLENT_SITE(220),

    /**
     * 告警中台任务
     */
    TASK(230),

    /**
     * 点位分成
     */
    DATAV(240),

    /**
     * SKU业务功能控制
     */
    SKU_FEATURE(250),

    /**
     * 商品排序搭配管理
     */
    SKU_ORDER_CONFIG(260),

    /**
     * 点位竞品
     */
    POINT_COMPETITOR_CONFIG(270),

    /**
     * 营销计划
     */
    MARKETING_PLAN(270),

    /**
     * 商品黑名单
     */
    SKU_SHIELD_SCHEDULE(270),

    // =========== 在分割线以上添加业务模块码 ============

    ;

    private int code;

    ServiceCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
