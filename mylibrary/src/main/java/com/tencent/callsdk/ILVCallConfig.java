package com.tencent.callsdk;

import com.tencent.TIMMessageListener;
import com.tencent.ilivesdk.core.ILiveRoomConfig;

public class ILVCallConfig
        extends ILiveRoomConfig<ILVCallConfig>
{
    private messageHandle mMsgPipe = null;
    private long mTimeout = 10000L;
    private boolean bAutoBusy = true;
    private boolean bServerTimeStamp = true;
    private boolean bMemberStatusFix = true;
    private boolean bPbProto = false;
    private ILVCallNotificationListener mNotificationListener;
    private TIMMessageListener mMessageListener = null;

    public ILVCallConfig setMessagePipe(messageHandle msgHandle)
    {
        this.mMsgPipe = msgHandle;
        return this;
    }

    public ILVCallConfig setTimeOut(long timeout)
    {
        this.mTimeout = (timeout * 1000L);
        return this;
    }

    public ILVCallConfig setNotificationListener(ILVCallNotificationListener notificationListener)
    {
        this.mNotificationListener = notificationListener;
        return this;
    }

    public ILVCallConfig setAutoBusy(boolean enable)
    {
        this.bAutoBusy = enable;
        return this;
    }

    public boolean isServerTimeStamp()
    {
        return this.bServerTimeStamp;
    }

    public boolean isPbProto()
    {
        return this.bPbProto;
    }

    public ILVCallConfig setPbProto(boolean bPbProto)
    {
        this.bPbProto = bPbProto;
        return this;
    }

    public ILVCallConfig setServerTimeStamp(boolean enable)
    {
        this.bServerTimeStamp = enable;
        return this;
    }

    public boolean isMemberStatusFix()
    {
        return this.bMemberStatusFix;
    }

    public ILVCallConfig setMemberStatusFix(boolean enable)
    {
        this.bMemberStatusFix = enable;
        return this;
    }

    public ILVCallConfig setCallMessageListener(TIMMessageListener messageListener)
    {
        this.mMessageListener = messageListener;
        return this;
    }

    public messageHandle getMsgPipe()
    {
        return this.mMsgPipe;
    }

    public long getTimeout()
    {
        return this.mTimeout;
    }

    public boolean isAutoBusy()
    {
        return this.bAutoBusy;
    }

    public TIMMessageListener getMessageListener()
    {
        return this.mMessageListener;
    }

    public ILVCallNotificationListener getNotificationListener()
    {
        return this.mNotificationListener;
    }

    @Deprecated
    public ILVCallConfig messageListener(TIMMessageListener listener)
    {
        return (ILVCallConfig)super.messageListener(listener);
    }

    public static abstract interface messageHandle
    {
        public abstract void sendC2CMessage(String paramString1, String paramString2);

        public abstract void sendGroupMessage(String paramString1, String paramString2);
    }
}
