package com.hwx.rx_chat.common.response;

public class DefaultResponse {
    private String code;
    private String message;
    private String value;

    public DefaultResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public DefaultResponse(String code, String message, String value) {
        this.code = code;
        this.message = message;
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
