package com.tencent.callsdk.adapter;

import com.tencent.callsdk.ILVCallNotification;
import com.tencent.callsdk.data.CallInfo;
import com.tencent.callsdk.data.CallMsg;
import com.tencent.callsdk.data.IncomingInfo;
import java.util.List;

public abstract interface CallProtoEngine
{
    public abstract byte[] getDailingData(String paramString, CallInfo paramCallInfo);

    public abstract byte[] getDailingData(List<String> paramList, CallInfo paramCallInfo);

    public abstract byte[] getInviteData(List<String> paramList, CallInfo paramCallInfo);

    public abstract byte[] getSponsorCancelData(CallInfo paramCallInfo);

    public abstract byte[] getAcceptData(IncomingInfo paramIncomingInfo);

    public abstract byte[] getRejectData(IncomingInfo paramIncomingInfo);

    public abstract byte[] getHangUpData(CallInfo paramCallInfo);

    public abstract byte[] getLineBusyData(IncomingInfo paramIncomingInfo);

    public abstract byte[] getSponsorTimeOutData(CallInfo paramCallInfo);

    public abstract byte[] getDeviceEventData(List<String> paramList, CallInfo paramCallInfo);

    public abstract byte[] getHeartBeatData(CallInfo paramCallInfo);

    public abstract byte[] getNotificationData(ILVCallNotification paramILVCallNotification, CallInfo paramCallInfo);

    public abstract byte[] getNotificationData(ILVCallNotification paramILVCallNotification, IncomingInfo paramIncomingInfo);

    public abstract CallMsg parseData(byte[] paramArrayOfByte, String paramString);
}
