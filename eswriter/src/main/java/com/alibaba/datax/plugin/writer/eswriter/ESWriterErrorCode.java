package com.alibaba.datax.plugin.writer.eswriter;

import com.alibaba.datax.common.spi.ErrorCode;

public enum ESWriterErrorCode implements ErrorCode {
    RUNTIME_EXCEPTION("ESWriter-00", "运行时异常"),
    ILLEGAL_VALUE("ESWriter-01", "您填写的参数值不合法."),
    CONFIG_INVALID_EXCEPTION("ESWriter-02", "您的参数配置错误."),
    SECURITY_NOT_ENOUGH("ESWriter-03", "您缺少权限执行相应的文件写入操作."),
    CONF_ERROR("ESWriter-04", "column配置错误."),
    ESSERVER_ERROR("ESWriter-05", "es服务器连接获取异常.");



    private final String code;
    private final String description;

    private ESWriterErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        return String.format("Code:[%s], Description:[%s]. ", this.code,
                this.description);
    }
}
