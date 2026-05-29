package com.quna.rag.common;


/***
 *
 * Copyright（C）, Quna Private License 1.0, 2015-2020
 * @Description: 自定义错误码
 *  完整错误码构成组合，总长度 6个数字
 *      前缀(2位) + 业务模块码(2位) + 错误点(2位)
 * @author tu.kai
 * @Date: 2020/11/30 17:05
 *
 */
public enum QunaExCode implements IRespCode {

    /***
     * 409001
     * 公共错误代码：接口请求无效参数
     */
    ILLEGAL_REQ_PARAM(PrefixCode.ILL, ServiceCode.COMMON, "01", "接口请求无效参数"),

    SERV_USER_NULL(PrefixCode.SERV, ServiceCode.USER, "01", "用户不存在"),

    DING_TOKEN_ERROR(PrefixCode.SERV, ServiceCode.COMMON, "03", "获取钉钉TOKEN失败"),

    /**
     * 无效数据
     */
    PRIMARY_KEY_NULL(PrefixCode.SERV, ServiceCode.COMMON, "02", "无效数据"),

    /**
     * 数据状态错误
     */
    DATA_STATUE_ERROR(PrefixCode.SERV, ServiceCode.COMMON, "03", "数据状态错误"),

    /***
     * 400101
     * 用户登录账号密码错误
     */
    SYSTEM_LOGIN_PARAM_ERR(PrefixCode.ILL, ServiceCode.SYSTEM, "01", "用户登录账号密码错误"),
    SYSTEM_LOGIN_ERR(PrefixCode.ILL, ServiceCode.SYSTEM, "02", "域网络异常"),

    /****
     * 400201
     * 400202
     * 微信登录授权相关错误
     */
    SYSTEM_WECHAT_CODE_ERR(PrefixCode.ILL, ServiceCode.SYSTEM_MICRO, "01", "授权码已过期，重新进入"),
    SYSTEM_WECHAT_LOGIN_ERR(PrefixCode.ILL, ServiceCode.SYSTEM_MICRO, "02", "微信接口访问异常"),

    /*-------------------------------------------机器管理----------------------------------------------------*/
    /**
     * 901001
     */
    SERV_MACHINE_NULL(PrefixCode.SERV, ServiceCode.MACHINE, "01", "机器不存在"),
    /**
     * 901002
     */
    SERV_MACHINE_OCCUPY_STATUS_CHANGE(PrefixCode.SERV, ServiceCode.MACHINE, "02", "当前站点占领状态发生变化，请刷新重试"),
    /**
     * 901003
     */
    SERV_MACHINE_QUERY_CONDITION_NULL(PrefixCode.SERV, ServiceCode.MACHINE, "03", "请输入查询条件"),
    /**
     * 901004
     */
    SERV_MACHINE_ADD_MACHINE_EXISTS(PrefixCode.SERV, ServiceCode.MACHINE, "04", "站点中已存在当前机器，请勿重复添加"),
    /**
     * 901005
     */
    SERV_MACHINE_ADD_MACHINE_COORD_TRANS_ERROR(PrefixCode.SERV, ServiceCode.MACHINE, "05", "站点坐标转换异常"),

    /**
     * 901006
     * 机器状态不在线
     */
    SERV_MACHINE_STATUS_EXCEPTION(PrefixCode.SERV, ServiceCode.MACHINE, "06", "机器运行状态异常"),

    /**
     * 901007
     * 机器货道异常
     */
    SERV_MACHINE_AISLE_EXCEPTION(PrefixCode.SERV, ServiceCode.MACHINE, "07", "机器货道异常"),

    /**
     * 901008
     * 当前机器无初始化的换货配置
     */
    SERV_MACHINE_NOT_VENDING_INFO(PrefixCode.SERV, ServiceCode.MACHINE, "08", "当前机器无初始化的换货配置"),

    /**
     * 901009
     * 网络异常, 请稍后重试
     */
    SERV_NETWORK_EXCEPTION(PrefixCode.SERV, ServiceCode.MACHINE, "09", "网络异常, 请稍后重试"),
    /*-------------------------------------------赏金----------------------------------------------------*/
    /**
     * 903001
     * 赏金活动不存在
     */
    BONUS_NULL(PrefixCode.SERV, ServiceCode.BONUS, "01", "赏金活动不存在"),
    /**
     * 903002
     * 赏金活动审核状态不一致
     */
    BONUS_STATUS_MORE(PrefixCode.SERV, ServiceCode.BONUS, "02", "赏金活动审核状态不一致"),
    /**
     * 903003
     * 优惠券获得概率总和不允许超出/低于100%
     */
    BONUS_STATUS_PROB_MORE(PrefixCode.SERV, ServiceCode.BONUS, "03", "优惠券获得概率总和不允许超出/低于100%"),

    /*-------------------------------------------提现记录----------------------------------------------------*/
    /**
     * 赏金提现记录不存在
     */
    BONUS_CASH_RECORD_NULL(PrefixCode.SERV, ServiceCode.WITHDRAW, "01", "赏金提现记录不存在"),

    /**
     * 904002
     * 余额不足
     */
    SERV_WITHDRAW_BALANCE_NOT_ENOUGH(PrefixCode.SERV, ServiceCode.WITHDRAW, "02", "余额不足"),

    /**
     * 904003
     * 审核打款失败
     */
    SERV_WITHDRAW_ERROR(PrefixCode.SERV, ServiceCode.WITHDRAW, "03", "审核打款失败"),

    /**
     * 904004
     * 提现订单已处理, 请勿重复处理
     */
    SERV_WITHDRAW_ORDER_HANDLED(PrefixCode.SERV, ServiceCode.WITHDRAW, "04", "提现订单已处理, 请勿重复处理"),
    /*-------------------------------------------趣赚钱用户----------------------------------------------------*/
    /**
     * 趣赚钱用户不存在
     */
    STAND_USER_NULL(PrefixCode.SERV, ServiceCode.STAND_USER, "01", "趣赚钱用户不存在"),

    /*-------------------------------------------趣赚钱业务----------------------------------------------------*/
    /**
     * 909001
     * 未指定查询时间范围
     */
    STAND_QUERY_TIME_EMPTY(PrefixCode.SERV, ServiceCode.STAND, "01", "未指定查询时间范围"),

    /**
     * 909002
     * 体验站长坑位配置不存在
     */
    STAND_EXP_PARTNER(PrefixCode.SERV, ServiceCode.STAND, "02", "体验站长坑位配置不存在"),

    /**
     * 909003
     * 任务模板不存在
     */
    STAND_TASK_TEMPLATE_NULL(PrefixCode.SERV, ServiceCode.STAND, "03", "任务模板不存在"),

    /**
     * 909004
     * 无效的站点任务行为
     */
    STAND_INVALID_TASK_ACTION(PrefixCode.SERV, ServiceCode.STAND, "04", "无效的站点任务行为"),

    /**
     * 909005
     * 不支持配置多商品
     */
    STAND_TASK_MANAGE_NOT_MUTI_AID(PrefixCode.SERV, ServiceCode.STAND, "05", "不支持配置多商品"),

    /**
     * 909006
     * 请选择关联SKU
     */
    STAND_TASK_MANAGE_SKU_NULL(PrefixCode.SERV, ServiceCode.STAND, "06", "请选择关联SKU"),
    /*-------------------------------------------2021_99----------------------------------------------------*/
    /**
     * 幸运抽奖格数过多
     */
    SCRIPT_LUCKY_MORE(PrefixCode.SERV, ServiceCode.SCRIPT, "01", "幸运抽奖格数过多"),

    /**
     * 生成能量石支付码错误
     */
    SCRIPT_STONE_PAY_QRCODE_ERROR(PrefixCode.SERV, ServiceCode.SCRIPT, "02", "生成能量石支付码错误"),

    /****
     * 虚拟点位核销
     *  无效的取物码
     */
    SCRIPT_BARCODE_INVALID(PrefixCode.SERV, ServiceCode.SCRIPT, "03", "无效的取物码"),


    /*-------------------------------------------2022_99----------------------------------------------------*/
    /**
     * 9015001
     * 用户身份不合法
     */
    VIRTUAL_USER_OFF_JOB(PrefixCode.SERV, ServiceCode.VIRTUAL_POINT, "01", "用户身份不合法"),

    /**
     * 9015002
     * 虚拟点位不存在
     */
    VIRTUAL_POINT_NOT_EXIST(PrefixCode.SERV, ServiceCode.VIRTUAL_POINT, "02", "虚拟点位不存在"),

    /**
     * 9015003
     * 无效取物码
     */
    VIRTUAL_QRCODE_INVALID(PrefixCode.SERV, ServiceCode.VIRTUAL_POINT, "03", "无效取物码"),

    /**
     * 9015004
     * 分配店长权限
     */
    VIRTUAL_SHOP_AUTH(PrefixCode.SERV, ServiceCode.VIRTUAL_POINT, "04", "请联系管理员,分配店长权限"),

    /**
     * 9015005
     * 权限不足
     */
    VIRTUAL_SHOP_AUTH_INSUFFICIENT(PrefixCode.SERV, ServiceCode.VIRTUAL_POINT, "05", "权限不足"),

    /**
     * 9015006
     * 虚拟点位货道信息为空
     */
    VIRTUAL_MACHINE_AISLE_NOT_EXIST(PrefixCode.SERV, ServiceCode.VIRTUAL_POINT, "06", "虚拟点位货道信息为空"),

    /**
     * 9015007
     * 无可执行的换货计划
     */
    VIRTUAL_MACHINE_NOT_VENDINGINFO(PrefixCode.SERV, ServiceCode.VIRTUAL_POINT, "07", "无可执行的换货计划"),
    /*-------------------------------------------点位合同管理----------------------------------------------------*/
    /**
     * 无效的点位合同ID
     */
    POINT_CONTRACT_ID_INVALID(PrefixCode.SERV, ServiceCode.POINT_CONTRACT, "01", "无效的点位合同ID"),
    /**
     * 无效的合同状态
     */
    POINT_CONTRACT_STATUS_INVALID(PrefixCode.SERV, ServiceCode.POINT_CONTRACT, "02", "无效的合同状态"),
    /**
     * 排他条款发送钉钉代办失败
     */
    QUERY_CONTRACT_DING_TODO_FAIL(PrefixCode.SERV, ServiceCode.POINT_CONTRACT, "03", "有排它条款的点位存在竞品，发送钉钉代办失败"),
    /**
     * 查询合同信息失败
     */
    QUERY_CONTRACT_FAIL(PrefixCode.SERV, ServiceCode.POINT_CONTRACT, "04", "查询合同信息失败"),

    /**
     * 点位合同周期变更失败
     */
    PERIOD_CONTRACT_FAIL(PrefixCode.SERV, ServiceCode.POINT_CONTRACT, "05", "暂不支持付款周期由短周期修改为长周期"),

    /***
     * 906003
     * 机器未配置能量石游戏
     */
    SCRIPT_ENERGY_MACHINE_CONFIG_ERROR(PrefixCode.SERV, ServiceCode.SCRIPT, "03", "机器未配置能量石游戏"),

    /*-------------------------------------------点位合同管理----------------------------------------------------*/
    /***
     * 9012001
     * 项目名称或pm缺失
     */
    INVESTMENT_PROJECT_NULL(PrefixCode.SERV, ServiceCode.INVESTMENT_PROJECT, "01", "项目名称或pm缺失"),

    /**
     * 9012002
     */
    INVESTMENT_PROJECT_LIMIT(PrefixCode.SERV, ServiceCode.INVESTMENT_PROJECT, "02", "详细模式|价格|到仓数量字数限制50个字符"),
    /**
     * 9012003
     */
    INVESTMENT_PROJECT_DUPLICATE(PrefixCode.SERV, ServiceCode.INVESTMENT_PROJECT, "03", "添加商品-城市重复"),
    /**
     * 9012004
     */
    INVESTMENT_PROJECT_EMAIL_ERROR(PrefixCode.SERV, ServiceCode.INVESTMENT_PROJECT, "04", "邮箱格式错误"),
    /**
     * 9012005
     */
    INVESTMENT_PROJECT_EXPORT_FAIL(PrefixCode.SERV, ServiceCode.INVESTMENT_PROJECT, "05", "导出失败"),
    /*-------------------------------------------京东派发活动----------------------------------------------------*/
    /**
     * 907001
     * 京东派发活动-无效的商品ID
     */
    JD_FREE_SAMPLE_CONFIG_INVALID_SKU(PrefixCode.SERV, ServiceCode.JD_FREE_SAMPLE, "01", "无效的商品ID"),

    /*****
     * 400301
     * 机器直播控制配置参数错误
     */
    CONTROL_MACHINE_LIVE_ACTION_ERR(PrefixCode.ILL, ServiceCode.SYSTEM_MICRO_CONTROL, "01", "无效机器控制配置"),

    /****
     * 400302
     * 机器直播间未查询到数据
     */
    CONTROL_MACHINE_LIVE_ROOM_ERR(PrefixCode.ILL, ServiceCode.SYSTEM_MICRO_CONTROL, "02", "机器无直播间"),

    /***
     * 400303
     * 查询机器硬件信息失败
     */
    CONTROL_MACHINE_FET_INFO_ERR(PrefixCode.ILL, ServiceCode.SYSTEM_MICRO_CONTROL, "03", "无有效直播硬件信息"),

    /**
     * 907002
     */
    JD_FREE_SAMPLE_CONFIG_INVALID_TOTAL(PrefixCode.SERV, ServiceCode.JD_FREE_SAMPLE, "02", "无效的派发总量"),

    /**
     * 907003
     */
    JD_FREE_SAMPLE_CONFIG_INVALID_TIME(PrefixCode.SERV, ServiceCode.JD_FREE_SAMPLE, "03", "无效的活动时间"),

    /**
     * 907004
     */
    JD_FREE_SAMPLE_CONFIG_INVALID_CONFIG_ID(PrefixCode.SERV, ServiceCode.JD_FREE_SAMPLE, "04", "无效的活动配置ID"),

    /**
     * 907005
     */
    JD_FREE_SAMPLE_CONFIG_INVALID_MACHINE_ID(PrefixCode.SERV, ServiceCode.JD_FREE_SAMPLE, "05", "无效的活动机器ID"),

    /**
     * 907006
     */
    JD_FREE_SAMPLE_RECEIVE_RECORD_INVALID_ID(PrefixCode.SERV, ServiceCode.JD_FREE_SAMPLE, "06", "无效的派发记录ID"),

    /**
     * 907007
     */
    JD_FREE_SAMPLE_AFRESH_APPLY_ERROR(PrefixCode.SERV, ServiceCode.JD_FREE_SAMPLE, "07", "重置二维码异常"),

    /*-------------------------------------------明星生日活动----------------------------------------------------*/
    /**
     * 907501
     */
    SUPER_STAR_GIFT_INFO_ID_INVALID(PrefixCode.SERV, ServiceCode.SUPER_STAR, "01", "奖品ID不存在"),

    /**
     * 907502
     */
    SUPER_STAR_GIFT_TYPE_INVALID(PrefixCode.SERV, ServiceCode.SUPER_STAR, "02", "奖品类型不符合要求"),

    /*-------------------------------------------社区团购----------------------------------------------------*/
    /**
     * 908001
     */
    GROUP_BUY_SCHEDULE_TIME_CONFLICT(PrefixCode.SERV, ServiceCode.GROUP_BUY, "01", "同点位计划时间冲突"),

    /**
     * 908002
     */
    GROUP_BUY_SCHEDULE_IMPORT_MISSING_FIELD(PrefixCode.SERV, ServiceCode.GROUP_BUY, "02", "缺少必要字段"),

    /**
     * 908003
     */
    GROUP_BUY_SCHEDULE_INVALID_ID(PrefixCode.SERV, ServiceCode.GROUP_BUY, "03", "无效的系统ID"),


    /**********************************************用户评测start*************************************************/

    QUESTIONNAIRE_REST_STATE_ERROR(PrefixCode.SERV, ServiceCode.QUESTION, "01", "问卷状态无法重置"),

    QUESTIONNAIRE_REST_DATE_ERROR(PrefixCode.SERV, ServiceCode.QUESTION, "02", "优惠券无法延期"),

    QUESTIONNAIRE_EXIE_DATE_ERROR(PrefixCode.SERV, ServiceCode.QUESTION, "03", "问卷已存在无法下发"),

    QUESTIONNAIRE_EXIE_COUPON_DATE_ERROR(PrefixCode.SERV, ServiceCode.QUESTION, "04", "问卷信息与奖励不匹配"),

    QUESTIONNAIRE_NO_GCID_EXIE(PrefixCode.SERV, ServiceCode.QUESTION, "05", "该商品编码不存在"),

    QUESTIONNAIRE_EXCEL_ERROR(PrefixCode.SERV, ServiceCode.QUESTION, "06", "批量下发文件内容异常"),

    /**********************************************用户评测end***************************************************/

    //********************************************数字周边***************************************************
    /**
     * 90-130-01
     */
    DIGITAL_PERIPHERY_PARAM_INVALID(PrefixCode.SERV, ServiceCode.DIGITAL_PERIPHERY, "01", "无效的字段输入!"),
    DIGITAL_PERIPHERY_DUPLICATE_NAME(PrefixCode.SERV, ServiceCode.DIGITAL_PERIPHERY, "02", "周边名称重复,无法创建!"),
    DIGITAL_PERIPHERY_OVERSTEP_STOCK(PrefixCode.SERV, ServiceCode.DIGITAL_PERIPHERY, "03", "计划配置库存超出实际库存!"),
    DIGITAL_PERIPHERY_MATCH_NULL(PrefixCode.SERV, ServiceCode.DIGITAL_PERIPHERY, "04", "请配置周边基础信息!"),
    DIGITAL_PERIPHERY_ADD_NOT_ALLOW(PrefixCode.SERV, ServiceCode.DIGITAL_PERIPHERY, "05", "修改时不可新增周边信息!"),

    //********************************************商品加购配置相关***************************************************
    MACHINE_EXPAND_PARAM_INVALID(PrefixCode.SERV, ServiceCode.MACHINE_EXPAND_CONFIG, "01", "<商品加购模块>不能重复添加!"),
    MACHINE_EXPAND_MAIN_GOODS_EMPTY(PrefixCode.SERV, ServiceCode.MACHINE_EXPAND_CONFIG, "02", "<商品加购模块>主商品不能为空!"),

    ADDITIONAL_CONFIG_NAME_REPEAT(PrefixCode.SERV, ServiceCode.MACHINE_EXPAND_CONFIG, "03", "商品加购配置名称重复"),

    /*-------------------------------------------权限用户----------------------------------------------------*/

    /**
     * 9014001
     * 权限用户不存在
     */
    AUTH_USER_NOT_EXIST(PrefixCode.SERV, ServiceCode.AUTH, "01", "权限用户不存在"),


    /*-------------------------------------------终端运营----------------------------------------------------*/

    /**
     * 9015001
     * 排货操作记录间隔不要超过一个月
     */
    MACHINE_SKU_CYCLE_ACTION_RECORD_TIME_NOT_RANGE_ONE_MONTH(PrefixCode.SERV, ServiceCode.TERMINAL_OPERATION, "01", "时间跨度不要超过一个月"),


    /*----------------------------可视化排货--------------------------*/
    VISUAL_MACHINE_SKU_CYCLE_NOT_EXIST(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "01", "机器排期不存在"),

    VISUAL_NEW_MACHINE_SKU_CYCLE_NOT_EXIST(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "01", "新机排期不存在"),

    VISUAL_MACHINE_SKU_MACHINE_NOT_EXIST(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "02", "机器不存在"),

    VISUAL_MACHINE_SKU_CITY_NOT_EXIST(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "03", "城市不存在"),

    VISUAL_MACHINE_SKU_DISTRICT_NOT_EXIST(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "04", "行政区不存在"),

    /**
     * 9017005
     * 机器货道信息不存在
     */
    VISUAL_MACHINE_SKU_MACHINE_SLOUT_NOT_EXIST(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "05", "当前系列机器基础信息不存在"),

    /**
     * 9017006
     * 未知机器外观类型
     */
    VISUAL_MACHINE_FACADE_INVALID(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "06", "未知机器外观类型"),

    /**
     * 9017007
     * 未知机器型号
     */
    VISUAL_MACHINE_UNKNOWN_SERIES(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "07", "未知机器型号"),

    /**
     * 9017008
     * 货道层数超出限制
     */
    VISUAL_MACHINE_AISLE_PLIE(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "08", "货道层数超出限制"),

    /**
     * 9017009
     * 每层货道数超出最大货道数or小于最小货道数
     */
    VISUAL_MACHINE_AISLE_NUM(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "09", "每层货道数超出最大货道数or小于最小货道数"),

    /**
     * 9017010
     * 当前商品摆放位置不正确
     */
    VISUAL_MACHINE_AISLE_POSITION_INCORRECTNESS(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "10", "当前商品摆放位置不正确"),

    /**
     * 9017011
     * 排货基础sku信息不存在
     */
    VISUAL_BASIC_SKU_NOT_EXIST(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "11", "排货基础sku信息不存在"),

    /**
     * 9017012
     * 当前商品摆放层数不正确
     */
    VISUAL_MACHINE_AISLE_PLIE_POSITION_INCORRECTNESS(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "12", "当前商品摆放层数不正确"),

    /**
     * 9017013
     * 获取机器内销量数据失败
     */
    VISUAL_MACHINE_SKU_GET_QUANTITY_RECORD_DAILY_ERROR(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "13", "获取机器内销量数据失败"),

    /**
     * 9017014
     * 商品高度超出层高
     */
    VISUAL_SKU_OUT_OF_PLIE_HEIGHT(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "14", "商品高度超出层高"),

    /**
     * 9017015
     * 商品宽度超出漏斗最大承受宽度
     */
    VISUAL_SKU_OUT_OF_MAX_WIDTH(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "15", "商品宽度超出漏斗最大承受宽度"),

    /**
     * 9017016
     * 未知的货道类别
     */
    VISUAL_UNKNOWN_AISLE_CATEGORY(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "16", "未知的货道类别"),

    /**
     * 9017017
     * 商品不支持弹簧货道
     */
    VISUAL_SKU_NON_SUPPORT_SPRING(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "17", "商品不支持弹簧货道"),

    /**
     * 9017018
     * 机器当前层不支持弹簧货道
     */
    VISUAL_MACHINE_PLIE_NON_SUPPORT_SPRING(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "18", "机器当前层不支持弹簧货道"),

    /**
     * 9017019
     * 机器当前层支持的弹簧规格与商品支持的弹簧规格不一致
     */
    VISUAL_MACHINE_SPEC_INCONFORMITY_SKU_SPEC(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "19", "机器当前层支持的弹簧规格与商品支持的弹簧规格不一致"),

    /**
     * 9017020
     * 商品不支持单弹簧
     */
    VISUAL_SKU_NON_SUPPORT_SINGLE_SPRING(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "20", "商品不支持单弹簧"),

    /**
     * 9017021
     * 商品不支持双弹簧
     */
    VISUAL_SKU_NON_SUPPORT_DOUBLE_SPRING(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "21", "商品不支持双弹簧"),

    /**
     * 9017022
     * 机器商品超出最大宽度
     */
    VISUAL_MACHINE_SKU_OUT_OF_MAX_WIDTH(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "22", "机器商品超出最大宽度"),

    /**
     * 9017023
     * 5系机型不支持弹簧货道
     */
    VISUAL_FIVE_SERIES_NON_SUPPORT_SPRING_AISLE(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "23", "5系机型不支持弹簧货道"),


    /**
     * 9017024
     * 机器商品排期不存在
     */
    VISUAL_MACHINE_SKU_NOT_EXIST(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "24", "机器商品排期不存在"),

    /**
     * 9017025
     * 排期状态不对无法下发
     */
    VISUAL_MACHINE_SKU_SCHEDULE_ERROR(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "25", "排期状态不对无法下发"),

    /**
     * 9017026
     * 弹簧校验失败
     */
    VISUAL_MACHINE_SKU_SPRING_VERIFY_FAIL(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "26", "弹簧校验失败"),

    /**
     * 9017027
     * 货道数量校验失败
     */
    VISUAL_AISLE_NUM_VERIFY_FAIL(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "27", "货道数量校验失败"),

    /**
     * 9017028
     * 3系机型没有隔条货道
     */
    VISUAL_THREE_SERIES_NOT_CORSSER(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "28", "3系机型没有隔条货道"),

    /**
     * 9017029
     * 机器没有补货周期
     */
    VISUAL_MACHINE_REPLENISH_CYCLE_IS_NULL(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "29", "机器没有补货周期"),

    /**
     * 9017030
     * 机器商品排期表为空
     */
    VISUAL_MACHINE_SKU_CYCLE_IS_NULL(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "30", "机器商品排期表为空"),

    /**
     * 9017031
     * 商品高度大于层高
     */
    VISUAL_SKU_HEIGHT_GT_PLIE_HEIGHT(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "31", "商品高度大于层高"),

    /**
     * 9017032
     * 机器商品排期表流程状态错误
     */
    VISUAL_MACHINE_SKU_CYCLE_SCHEDULE_FAIL(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "32", "机器商品排期表流程状态错误"),

    /**
     * 9017033
     * 机器弹簧数量错误，请检查弹簧剩余数量
     */
    VISUAL_MACHINE_SKU_CYCLE_SPRING_SPEC_AMOUNT_ERROR(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "33", "机器内%s弹簧数量错误，请检查机器内%s弹簧剩余数量"),

    /**
     * 9017034
     * 22:00以后不可进行货道编辑与保存操作
     */
    VISUAL_MACHINE_SKU_CYCLE_CAN_EDIT(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "34", "22:00以后不可进行货道编辑与保存操作"),

    /**
     * 9017035
     * 当层货道验算结果为空
     */
    VISUAL_SKU_CHANGE_VERIFY_RESULT_NULL(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "35", "当层货道验算结果为空"),

    /**
     * 9017036
     * sku上架商品异常
     */
    VISUAL_SKU_CHANGE_PUT_ON_EXCEPTION(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "36", "sku上架商品异常"),

    /**
     * 9017035
     * 未来排期只能生成7天以内
     */
    VISUAL_PLAN_DATE_IN_SEVEN_DAYS(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "37", "未来排期只能生成7天以内"),

    /**
     * 9017035
     * 排期日期不能为空
     */
    VISUAL_PLAN_DATE_IS_NULL(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "35", "排期日期不能为空"),

    /**
     * /**
     * 9017037
     * 当期存在未放置货道商品, 不予保存
     */
    VISUAL_MACHINE_SKU_CYCLE_NOT_AISLE_ITEM(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "37", "当期存在未放置货道商品, 不予保存"),

    /**
     * 9017038
     * 货道详情超出限制
     */
    VISUAL_MACHINE_AISLE_ITEM_BEYOND(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "38", "货道详情超出限制"),

    /**
     * 9017039
     * sku上下架异常
     */
    VISUAL_SKU_CHANGE_EXCEPTION(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "39", "sku上下架异常"),

    /**
     * 9017040
     * 点位编码不能为空
     */
    VISUAL_MACHINE_NOT_EXIST(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "40", "点位编码不能为空"),

    /**
     * 9017041
     * 机器权限不足
     */
    VISUAL_HAND_JURISDICTION_FAIL(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "41", "机器权限不足"),

    /**
     * 9017042
     * 手工补货日期已失效
     */
    VISUAL_HAND_RT_EXPIRE(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "42", "手工补货日期已失效"),

    /**
     * 9017043
     * 无效类型
     */
    VISUAL_INVALID_TYPE(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "43", "无效类型"),

    /**
     * 9017044
     * 手工排期机器建议补货量计算失败
     */
    VISUAL_HAND_CYCLE_CALCULATE_FAIL(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "44", "手工排期机器建议补货量计算失败"),

    /**
     * 9017045
     * 手工排期补货信息为空
     */
    VISUAL_HAND_REPLENISHMENT_EMPTY(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "44", "请先编辑手工补货排期"),

    /**
     * 9017046
     * 手工排期审核信息异常
     */
    VISUAL_HAND_VERIFY_INFO_EXCEPTION(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "46", "手工排期审核信息异常"),

    /**
     * 9017047
     * 手工补货下发排期异常
     */
    VISUAL_HAND_GENERATE_VENDINGINFO_EXCEPTION(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "47", "手工补货下发排期异常"),

    /**
     * 9017048
     * 手工单流程错误
     */
    VISUAL_HAND_FLOW_ERROR(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "48", "手工单流程错误"),

    /**
     * 9017049
     * 手工单补货日期已失效
     */
    VISUAL_HAND_RT_INVALID(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "49", "手工单补货日期已失效"),

    /**
     * 9017050
     * 该手工单未达到删除条件
     */
    VISUAL_HAND_NOT_ALLOW_DELETE(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "50", "非草稿状态不可删除"),

    /**
     * 9017051
     * 数据字典编码不能重复
     */
    VISUAL_DICTIONARY_DICTCODE_EXIST(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "51", "数据字典编码不能重复"),

    /**
     * 9017052
     * 竞品管理查询失败
     */
    COMPETITOR_LIST_ERROR(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "52", "竞品管理查询失败"),

    /**
     * 9017052
     * 超过创建时间当天，不允许审核通过
     */
    VISUAL_HAND_CT_EXPIRED(PrefixCode.SERV, ServiceCode.VISUAL_MACHINE_SKU_CYCLE, "52", "超过创建时间当天，不允许审核通过"),

    /*-------------------------------------------终端运营start----------------------------------------------------*/

    /**
     * 9018001
     * 该商品已有审核通过的派样计划，请先停止正在审核通过的计划
     */
    BM_SKU_CITY_EXIST(PrefixCode.SERV, ServiceCode.BM, "01", "该商品已有审核通过的派样计划，请先停止正在审核通过的计划"),


    /*-------------------------------------------终端运营end----------------------------------------------------*/

    /*-------------------------------------------社群活动start----------------------------------------------------*/

    /**
     * 9020001
     * 仅活动未开始可删除
     */
    ASSOCIATION_ACTIVE_ERROR(PrefixCode.SERV, ServiceCode.ASSOCIATION, "01", "已开始的活动无法删除"),

    /**
     * 9020002
     * 社群领券记录未初始化
     */
    ASSOCIATION_RECORD_UNINITIALIZED(PrefixCode.SERV, ServiceCode.ASSOCIATION, "02", "社群领券记录未初始化"),

    /**
     * 9020003
     * 超出生成最大短链条数
     */
    ASSOCIATION_SHORT_LINK_BEYCOND_MAX_NUM(PrefixCode.SERV, ServiceCode.ASSOCIATION, "03", "超出生成最大短链条数"),

    /**
     * 9020004
     * 仅活动状态为未开始能编辑
     */
    ASSOCIATION_ACTIVE_NOT_ALLOW_EDIT(PrefixCode.SERV, ServiceCode.ASSOCIATION, "04", "仅活动状态为未开始能编辑"),

    /**
     * 9020005
     * 缺少活动状态参数
     */
    ASSOCIATION_MISSING_PARAM_ACTIVE(PrefixCode.SERV, ServiceCode.ASSOCIATION, "05", "缺少活动状态参数"),

    /*-------------------------------------------社群活动end----------------------------------------------------*/

    /*-------------------------------------------社群抽奖start----------------------------------------------------*/

    /**
     * 9020101
     * 不可修改抽奖次数
     */
    SYNTHETICAL_NOT_ALLOW_UPDATE_PLAYTOTAL(PrefixCode.SERV, ServiceCode.SYNTHETICAL, "01", "不可修改抽奖次数"),

    /**
     * 9020101
     * 不可修改活动开始时间
     */
    SYNTHETICAL_NOT_ALLOW_UPDATE_ST(PrefixCode.SERV, ServiceCode.SYNTHETICAL, "02", "不可修改活动开始时间"),

    /*-------------------------------------------社群抽奖start----------------------------------------------------*/


    /*-------------------------------------------精选点位start----------------------------------------------------*/

    /**
     * 9022001
     */
    EXCELLENT_SITE_ID_INVALID(PrefixCode.SERV, ServiceCode.EXCELLENT_SITE, "01", "精选点位ID错误"),

    /**
     * 9022002
     */
    EXCELLENT_SITE_MID_MUST(PrefixCode.SERV, ServiceCode.EXCELLENT_SITE, "02", "机器ID必填"),

    /**
     * 9022003
     */
    EXCELLENT_SITE_EXISTS(PrefixCode.SERV, ServiceCode.EXCELLENT_SITE, "03", "当前机器已创建精选点位"),
    /*-------------------------------------------精选点位end----------------------------------------------------*/


    /*-------------------------------------------告警中台start----------------------------------------------------*/

    /**
     * 9023001
     * 请先填写处理完成情况
     */
    TASK_CONDITION_NULL(PrefixCode.SERV, ServiceCode.TASK, "01", "请先填写处理完成情况"),

    /**
     * 902302
     */
    TASK_NOT_SUPPORT_DRAG(PrefixCode.SERV, ServiceCode.TASK, "02", "该任务不支持拖拽"),

    /*-------------------------------------------告警中台end----------------------------------------------------*/

    /*------------------------------------------- 点位分成start ----------------------------------------------------*/

    /**
     * 9024001
     * 账单已核对
     */
    DATAV_BILL_CHECKED(PrefixCode.SERV, ServiceCode.DATAV, "01", "账单账单已核对"),

    /**
     * 9024002
     * 税额计算不一致, 请核对
     */
    DATAV_TAX_AMOUNT_NOT_EQUAL(PrefixCode.SERV, ServiceCode.DATAV, "02", "税额计算不一致, 请核对"),

    /**
     * 9024003
     * 本期实付金额计算不一致, 请核对
     */
    DATAV_PAYMENT_NOT_EQUAL(PrefixCode.SERV, ServiceCode.DATAV, "03", "本期实付金额计算不一致, 请核对"),


    /**
     * 9024004
     * 状态错误
     */
    DATAV_STATUS_ERROR(PrefixCode.SERV, ServiceCode.DATAV, "04", "状态错误"),

    /**
     * 9024005
     * 账单重推失败
     */
    DATAV_BILL_REPUSH_FAIL(PrefixCode.SERV, ServiceCode.DATAV, "05", "账单重推失败"),

    /**
     * 9024006
     */
    DATAV_BILL_RATIO_EMPTY(PrefixCode.SERV, ServiceCode.DATAV, "06", "系统核对后, 无时间段内的账单需计算"),

    /**
     * 9024007
     */
    ACCOUNT_NOT_ACTIVATE_BILL_REPUSH_FAIL(PrefixCode.SERV, ServiceCode.DATAV, "07", "账号未激活, 账单重推失败"),

    /**
     * 9024008
     * 密码规则错误, 至少包含一个数字or一个字母
     */
    ACCOUNT_PASSWORD_RULE_ERROR(PrefixCode.SERV, ServiceCode.DATAV, "08", "密码规则错误, 至少包含一个数字or一个字母, 且长度在6~12位"),

    /**
     * 9024009
     * 账号信息变更申请已审核
     */
    ACCOUNT_CHANGE_STATUS_CHECKED(PrefixCode.SERV, ServiceCode.DATAV, "09", "账号信息变更申请已审核"),

    /**
     * 9024010
     * 导入列不能为空
     */
    EXPORT_COLUMN_NOT_EMPTY(PrefixCode.SERV, ServiceCode.DATAV, "10", "导入列不能为空"),

    /**
     * 9024011
     * 暂未创建趣查查账号
     */
    EXPORT_MACHINE_EXCEPTION(PrefixCode.SERV, ServiceCode.DATAV, "11", "暂未创建趣查查账号"),

    /**
     * 9024012
     * 原付款开始时间=新付款开始时间, 继续提交则变更无效
     */
    EXPORT_PAYMENT_DATE_SAME(PrefixCode.SERV, ServiceCode.DATAV, "12", "原付款开始时间=新付款开始时间, 继续提交则变更无效"),

    /**
     * 9024013
     * 新付款时间不得早于原付款开始时间
     */
    EXPORT_PAYMENT_TIME_EXCEPTION(PrefixCode.SERV, ServiceCode.DATAV, "13", "新付款时间不得早于原付款开始时间"),

    /**
     * 9024014
     * 申请账单失败
     */
    APPLY_BILL_FAIL(PrefixCode.SERV, ServiceCode.DATAV, "14", "申请账单失败"),

    /**
     * 9024015
     * 合同编号不存在，请检查填写是否正确
     */
    BILL_CONTRACT_NOT_FOUND(PrefixCode.SERV, ServiceCode.DATAV, "15", "合同编号不存在，请检查填写是否正确"),

    /**
     * 9024016
     * 点位编号不存在，请检查填写是否正确
     */
    DATAV_MACHINE_NOT_FOUND(PrefixCode.SERV, ServiceCode.DATAV, "16", "点位编号不存在，请检查填写是否正确"),

    /**
     * 9024017
     * 合同编号与点位编号不匹配，请检查填写是否正确
     */
    DATAV_CONTRACT_MACHINE_MISMATCH(PrefixCode.SERV, ServiceCode.DATAV, "17", "合同编号与点位编号不匹配，请检查填写是否正确"),

    /**
     * 9024018
     */
    BILL_BEFORE_CONTRACT_START(PrefixCode.SERV, ServiceCode.DATAV, "18", "账单周期超出该合同的开始时间, 请检查填写是否正确"),

    /**
     * 9024019
     */
    BILL_AFTER_CONTRACT_END(PrefixCode.SERV, ServiceCode.DATAV, "19", "账单周期超出该合同的结束时间, 请检查填写是否正确"),

    /**
     * 9024020
     * 补充账单推送失败
     */
    DATAV_REPAIR_BILL_REPUSH_FAIL(PrefixCode.SERV, ServiceCode.DATAV, "20", "补充账单推送失败"),

    /**
     * 9024021
     * 暂无审核补充账单权限
     */
    DATAV_REPAIR_BILL_CHECK_NO_PERMISSION(PrefixCode.SERV, ServiceCode.DATAV, "21", "暂无审核补充账单权限"),

    /**
     * 9024021
     * 补充账单推送失败
     */
    DATAV_BILL_PEROD_ADJUST_FAIL(PrefixCode.SERV, ServiceCode.DATAV, "21", "付款周期调整推送消息失败"),

    /**
     * 9024022
     * 关闭账号申请提交失败
     */
    DATAV_ACCOUNT_CLOSE_SUBMIT_FAIL(PrefixCode.SERV, ServiceCode.DATAV, "22", "关闭账号申请提交失败，请检查"),

    /**
     * 9024023
     * 关闭账号申请审核失败
     */
    DATAV_ACCOUNT_CLOSE_AUDIT_FAIL(PrefixCode.SERV, ServiceCode.DATAV, "23", "关闭账号申请审核失败"),

    /**
     * 9024024
     * 该账号有待审核的账号关闭申请，请及时通知审批人审核
     */
    DATAV_ACCOUNT_CLOSE_AUDIT_REPEATED(PrefixCode.SERV, ServiceCode.DATAV, "24", "该账号有待审核的账号关闭申请，请及时通知审批人审核"),

    /**
     * 9024025
     * 该账号已关闭
     */
    DATAV_ACCOUNT_CLOSE_ALREADY(PrefixCode.SERV, ServiceCode.DATAV, "25", "该账号已关闭"),

    /**
     * 9024026
     * 该账号下点位账单未结清
     */
    DATAV_ACCOUNT_CLOSE_BILL_NOT_CLEAR(PrefixCode.SERV, ServiceCode.DATAV, "26", "该账号下点位账单未结清"),

    /**
     * 9024027
     * 该账号下点位合同未结束
     */
    DATAV_ACCOUNT_CLOSE_CONTRACT_NOT_END(PrefixCode.SERV, ServiceCode.DATAV, "27", "该账号下点位合同未结束"),

    /**
     * 4016010
     * 同组内至少两个点位
     */
    MNO_GROUP_AT_LEAST_TWO(PrefixCode.ILL, ServiceCode.TERMINAL_OPERATION, "10", "同组内至少两个有效点位"),

    /**
     * 4016011
     * 阈值需要在0-999
     */
    ALERT_CONFIG_THRESHOLD(PrefixCode.ILL, ServiceCode.TERMINAL_OPERATION, "11", "阈值需要在0-999"),

    /**
     * 4016012
     * 创建临时文件失败
     */
    CREATE_TEMP_FILE_FAIL(PrefixCode.SERV, ServiceCode.TERMINAL_OPERATION, "12", "创建临时文件失败"),


    /*------------------------------------------- 点位分成end ----------------------------------------------------*/

    /*------------------------------------------- SKU 业务功能控制 start ----------------------------------------------------*/

    /**
     * 9025001
     */
    SKU_FEATURES_DATA_EXISTS(PrefixCode.SERV, ServiceCode.SKU_FEATURE, "01", "当前商品的配置已存在, 请勿重复添加"),

    /**
     * 9025002
     */
    SKU_FEATURES_DATA_IMPORT_ERROR(PrefixCode.SERV, ServiceCode.SKU_FEATURE, "02", "缺少必要字段"),

    /*------------------------------------------- SKU 业务功能控制 end ----------------------------------------------------*/

    NOTIFY_AIOT_SALARY_EXCEPTION(PrefixCode.SERV, ServiceCode.MACHINE, "20", "调用AIOT接口异常"),

    NOTIFY_AIOT_HANDLE_EXCEPTION(PrefixCode.SERV, ServiceCode.MACHINE, "21", "AIOT处理物流薪资任务失败"),

    NOTIFY_AIOT_REQUEST_EXCEPTION(PrefixCode.SERV, ServiceCode.MACHINE, "22", "请求AIOT异常"),

    SYSTEM_EXCEPTION(PrefixCode.SYSTEM, ServiceCode.TERMINAL_OPERATION, "01", "系统异常，请联系管理员"),


    /*------------------------------------------- APP配置 start ----------------------------------------------------*/
    /**
     * 9026001
     */
    NOT_EXISTS(PrefixCode.SERV, ServiceCode.SKU_ORDER_CONFIG, "01", "数据不存在"),

    /**
     * 9026002
     */
    NOT_PERMIT_OPERATION(PrefixCode.SERV, ServiceCode.SKU_ORDER_CONFIG, "02", "审核拒绝后不能再次审核通过"),

    /**
     * 9026003
     */
    PLAN_ST_ET_ERROR(PrefixCode.SERV, ServiceCode.SKU_ORDER_CONFIG, "03", "计划时间错误"),

    /*------------------------------------------ APP配置 end ----------------------------------------------------*/

    /*------------------------------------------- 点位竞品start ----------------------------------------------------*/


    POINT_COMPETITOR_CONFIG_NAME_EXIST(PrefixCode.SERV, ServiceCode.POINT_COMPETITOR_CONFIG, "01", "配置名称已存在"),

    POINT_COMPETITOR_CONFIG_CITY_EXIST(PrefixCode.SERV, ServiceCode.POINT_COMPETITOR_CONFIG, "02", "城市分仓已存在"),


    /*------------------------------------------- 点位竞品end ----------------------------------------------------*/

    /*------------------------------------------- 点位竞品end ----------------------------------------------------*/



    /*------------------------------------------- 点位商品黑名单计划start ----------------------------------------------------*/

    SKU_SHIELD_SCHEDULE_CHECK_REJECT_EXP(PrefixCode.SERV, ServiceCode.SKU_SHIELD_SCHEDULE, "01", "选择的计划中存在审核不通过计划不支持再次审核，请重新选择！"),

    /*------------------------------------------- 点位商品黑名单计划end ----------------------------------------------------*/



    AUTO_SKU_ALREADY_MEET(PrefixCode.SERV, ServiceCode.TERMINAL_OPERATION, "02", "当前已满足【目标点位数】，请检查"),

    /*--------------------------------- 2025深圳99 二维码核销 ------------------------------------------*/
    QRCODE_NOT_EXIST(PrefixCode.SERV, ServiceCode.COMMON, "01", "无效的二维码"),
    QRCODE_EXPIRED(PrefixCode.SERV, ServiceCode.COMMON, "02", "二维码已过期"),


    /*--------------------------------- 营销计划 ------------------------------------------*/
    MARKETING_PLAN_REFUSE(PrefixCode.SERV, ServiceCode.MARKETING_PLAN, "01", "选择的计划中存在审核不通过计划不支持再次审核，请重新选择！"),

    
    ;
    private int excode;

    private String exmsg;

    QunaExCode(PrefixCode prefixCode, ServiceCode serviceCode, String excode, String exmsg) {

        this.excode = Integer.parseInt(new StringBuffer(String.valueOf(prefixCode.getCode()))
                .append(String.valueOf(serviceCode.getCode()))
                .append(String.valueOf(excode)).toString());

        this.exmsg = exmsg;
    }

    @Override
    public int getRespCode() {
        return this.excode;
    }

    @Override
    public String getRespMessage() {
        return this.exmsg;
    }
}
