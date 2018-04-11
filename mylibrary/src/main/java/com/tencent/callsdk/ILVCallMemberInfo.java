package com.tencent.callsdk;

import com.tencent.ilivesdk.ILiveFunc;

public class ILVCallMemberInfo
{
    private String id;
    @Deprecated
    private boolean bJoin = false;
    private boolean bMicEnable = false;
    private boolean bCameraEnable = false;
    @Deprecated
    private long uTimeStamp = 0L;

    public ILVCallMemberInfo(String id)
    {
        this.id = id;
        this.uTimeStamp = ILiveFunc.getCurrentSec();
    }

    public String getId()
    {
        return this.id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    @Deprecated
    public boolean isJoin()
    {
        return this.bJoin;
    }

    @Deprecated
    public void setJoin(boolean bJoin)
    {
        this.bJoin = bJoin;
    }

    public boolean isMicEnable()
    {
        return this.bMicEnable;
    }

    public void setMicEnable(boolean bMicEnable)
    {
        this.bMicEnable = bMicEnable;
    }

    public boolean isCameraEnable()
    {
        return this.bCameraEnable;
    }

    public void setCameraEnable(boolean bCameraEnable)
    {
        this.bCameraEnable = bCameraEnable;
    }

    @Deprecated
    public long getTimeStamp()
    {
        return this.uTimeStamp;
    }

    @Deprecated
    public void setTimeStamp(long uTimeStamp)
    {
        this.uTimeStamp = uTimeStamp;
    }

    public String toString()
    {
        return "ILVCallMemberInfo{id='" + this.id + '\'' + ", bJoin=" + this.bJoin + ", bMicEnable=" + this.bMicEnable + ", bCameraEnable=" + this.bCameraEnable + ", uTimeStamp=" + this.uTimeStamp + '}';
    }
}
