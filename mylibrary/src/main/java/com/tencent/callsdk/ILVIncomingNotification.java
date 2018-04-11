package com.tencent.callsdk;

import com.tencent.callsdk.data.CallMsg;
import java.util.ArrayList;
import java.util.List;

public class ILVIncomingNotification
        extends ILVCallNotification<ILVIncomingNotification>
{
    private String sponsorId;
    private List<String> members;

    public ILVIncomingNotification() {}

    public ILVIncomingNotification(CallMsg callMsg)
    {
        ((ILVIncomingNotification)((ILVIncomingNotification)((ILVIncomingNotification)((ILVIncomingNotification)((ILVIncomingNotification)((ILVIncomingNotification)setNotifId(callMsg.getNotifId())).setSponsorId(callMsg.getSponserId()).setNotifDesc(callMsg.getNotifDesc())).setSender(callMsg.getSender())).setTargets(callMsg.getTargets())).setTimeStamp(callMsg.getTimeStamp())).setUuid(callMsg.getUuid())).setUserInfo(callMsg.getUserInfo());
        for (String user : callMsg.getMembers()) {
            addMember(user);
        }
    }

    public String getSponsorId()
    {
        return this.sponsorId;
    }

    public ILVIncomingNotification setSponsorId(String sponsorId)
    {
        this.sponsorId = sponsorId;
        return this;
    }

    public List<String> getMembers()
    {
        return this.members;
    }

    public ILVIncomingNotification setMembers(List<String> members)
    {
        this.members = members;
        return this;
    }

    public ILVIncomingNotification addMember(String member)
    {
        if (null == this.members) {
            this.members = new ArrayList();
        }
        if (!this.members.contains(member)) {
            this.members.add(member);
        }
        return this;
    }

    public String getMembersString()
    {
        String strMembers = null;
        if (null != this.members) {
            for (String member : this.members) {
                if (null == strMembers) {
                    strMembers = "" + member;
                } else {
                    strMembers = strMembers + ", " + member;
                }
            }
        }
        return strMembers;
    }
}
