package com.tencent.callsdk;

import com.tencent.ilivesdk.ILiveFunc;
import com.tencent.ilivesdk.core.ILiveLoginManager;
import java.util.ArrayList;
import java.util.List;

public class ILVCallNotification<Self extends ILVCallNotification<Self>>
{
    private int notifId;
    private String notifDesc;
    private String sender;
    private List<String> targets = null;
    private long timeStamp;
    private String uuid;
    private String userInfo;

    public ILVCallNotification()
    {
        this.timeStamp = ILiveFunc.getCurrentSec();
        this.sender = ILiveLoginManager.getInstance().getMyUserId();
    }

    public Self setNotifId(int notifId)
    {
        this.notifId = notifId;
        return (Self) this;
    }

    public Self setNotifDesc(String notifDesc)
    {
        this.notifDesc = notifDesc;
        return (Self) this;
    }

    public Self setSender(String sender)
    {
        this.sender = sender;
        return (Self) this;
    }

    public Self addTarget(String target)
    {
        if (null == this.targets) {
            this.targets = new ArrayList();
        }
        this.targets.add(target);
        return (Self) this;
    }

    public Self setTargets(List<String> targets)
    {
        this.targets = targets;
        return (Self) this;
    }

    public Self setTimeStamp(long timeStamp)
    {
        this.timeStamp = timeStamp;
        return (Self) this;
    }

    public Self setUserInfo(String userInfo)
    {
        this.userInfo = userInfo;
        return (Self) this;
    }

    public Self setUuid(String uuid)
    {
        this.uuid = uuid;
        return (Self) this;
    }

    public String getUuid()
    {
        return this.uuid;
    }

    public int getNotifId()
    {
        return this.notifId;
    }

    public String getNotifDesc()
    {
        return this.notifDesc;
    }

    public String getSender()
    {
        return this.sender;
    }

    public List<String> getTargets()
    {
        return this.targets;
    }

    public long getTimeStamp()
    {
        return this.timeStamp;
    }

    public String getUserInfo()
    {
        return this.userInfo;
    }

    public String toString()
    {
        return "ILVCallNotification{notifId=" + this.notifId + ", notifDesc='" + this.notifDesc + '\'' + ", sender='" + this.sender + '\'' + ", targets=" + this.targets + ", timeStamp=" + this.timeStamp + ", userInfo='" + this.userInfo + '\'' + '}';
    }
}
