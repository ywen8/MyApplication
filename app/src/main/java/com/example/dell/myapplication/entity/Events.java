package com.example.dell.myapplication.entity;

import com.tencent.callsdk.ILVIncomingNotification;

public class Events {

    public int callId;

    public int callType;

    public ILVIncomingNotification notification;

    public Events(int callId, int callType, ILVIncomingNotification notification) {
        this.callId = callId;
        this.callType = callType;
        this.notification = notification;
    }
}
