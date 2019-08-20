package com.henry.netty.time.client;

import java.io.Serializable;

public class Message implements Serializable {

    private String msg;

    public Message(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
