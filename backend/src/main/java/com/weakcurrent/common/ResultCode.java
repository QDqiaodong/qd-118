package com.weakcurrent.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    STOCK_INSUFFICIENT(1001, "库存不足"),
    DATA_NOT_FOUND(1002, "数据不存在"),
    CATEGORY_CIRCULAR_REFERENCE(1003, "父级分类不能是自身或其子孙节点"),
    CATEGORY_HAS_CHILDREN(1004, "该分类下存在子分类，请先删除子分类"),
    CATEGORY_HAS_ACCESSORIES(1005, "该分类或其子分类下存在绑定的配件，请先删除或转移配件"),
    ACCESSORY_HAS_RECORDS(1006, "配件存在历史记录，无法删除"),
    ACCESSORY_DUPLICATE_MODEL(1007, "同分类下已存在相同型号的配件"),
    DATA_DUPLICATE(1008, "数据已存在，不可重复"),
    DATA_IN_USE(1009, "数据正在使用中，无法删除");

    private final Integer code;
    private final String message;
}
