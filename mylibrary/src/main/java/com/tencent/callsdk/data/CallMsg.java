package com.tencent.callsdk.data;

import com.tencent.callsdk.ILVCallNotification;
import java.util.LinkedList;
import java.util.List;

public class CallMsg
        extends ILVCallNotification<CallMsg>
{
    private int callId;
    private int callType;
    private String imGrpId;
    private String imGrpType;
    private String sponserId;
    private List<String> members = new LinkedList();

    public int getCallId()
    {
        return this.callId;
    }

    public CallMsg setCallId(int callId)
    {
        this.callId = callId;
        return this;
    }

    public int getCallType()
    {
        return this.callType;
    }

    public CallMsg setCallType(int callType)
    {
        this.callType = callType;
        return this;
    }

    public String getImGrpId()
    {
        return this.imGrpId;
    }

    public CallMsg setImGrpId(String imGrpId)
    {
        this.imGrpId = imGrpId;
        return this;
    }

    public String getImGrpType()
    {
        return this.imGrpType;
    }

    public CallMsg setImGrpType(String imGrpType)
    {
        this.imGrpType = imGrpType;
        return this;
    }

    public String getSponserId()
    {
        return this.sponserId;
    }

    public CallMsg setSponserId(String sponserId)
    {
        this.sponserId = sponserId;
        return this;
    }

    public List<String> getMembers()
    {
        return this.members;
    }

    public CallMsg addMember(String member)
    {
        this.members.add(member);
        return this;
    }
}
