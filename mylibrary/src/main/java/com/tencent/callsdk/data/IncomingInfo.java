package com.tencent.callsdk.data;

import java.util.ArrayList;
import java.util.List;

public class IncomingInfo
{
    private int callId;
    private int callType;
    private String userId;
    private String sponsor;
    private String imGrpId;
    private String imGrpType;
    private String uuid;
    private List<String> listMembers;
    private List<String> listOnlineMembers;

    public IncomingInfo(int callId, String userId, int callType)
    {
        this.callId = callId;
        this.userId = userId;
        this.callType = callType;
        this.listOnlineMembers = new ArrayList();
    }

    public IncomingInfo setUUID(String uuid)
    {
        this.uuid = uuid;
        return this;
    }

    public int getCallId()
    {
        return this.callId;
    }

    public void setCallId(int callId)
    {
        this.callId = callId;
    }

    public int getCallType()
    {
        return this.callType;
    }

    public void setCallType(int callType)
    {
        this.callType = callType;
    }

    public String getUserId()
    {
        return this.userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getSponsor()
    {
        return this.sponsor;
    }

    public void setSponsor(String sponsor)
    {
        this.sponsor = sponsor;
    }

    public String getImGrpId()
    {
        return this.imGrpId;
    }

    public void setImGrpId(String imGrpId)
    {
        this.imGrpId = imGrpId;
    }

    public String getImGrpType()
    {
        return this.imGrpType;
    }

    public void setImGrpType(String imGrpType)
    {
        this.imGrpType = imGrpType;
    }

    public String getUuid()
    {
        return this.uuid;
    }

    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }

    public List<String> getListMembers()
    {
        return this.listMembers;
    }

    public List<String> getListOnlineMembers()
    {
        return this.listOnlineMembers;
    }

    public void setListMembers(List<String> listMembers)
    {
        this.listMembers = listMembers;
    }
}
