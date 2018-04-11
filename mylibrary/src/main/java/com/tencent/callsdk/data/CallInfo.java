package com.tencent.callsdk.data;

import com.tencent.callsdk.ILVCallMemberInfo;
import com.tencent.callsdk.ILVCallOption;
import com.tencent.ilivesdk.core.ILiveLog;
import java.util.HashMap;

public class CallInfo
{
    private final String TAG = "CallInfo";
    private ILVCallOption option;
    private String sponsor;
    private String uuid;
    private HashMap<String, ILVCallMemberInfo> memberMap;

    public CallInfo()
    {
        this.memberMap = new HashMap();
    }

    public ILVCallOption getOption()
    {
        return this.option;
    }

    public CallInfo setOption(ILVCallOption option)
    {
        this.option = option;
        return this;
    }

    public String getSponsor()
    {
        return this.sponsor;
    }

    public CallInfo setSponsor(String sponsor)
    {
        this.sponsor = sponsor;
        return this;
    }

    public String getUuid()
    {
        return this.uuid;
    }

    public CallInfo setUuid(String uuid)
    {
        this.uuid = uuid;
        return this;
    }

    public HashMap<String, ILVCallMemberInfo> getMemberMap()
    {
        return this.memberMap;
    }

    public void reset()
    {
        ILiveLog.ki("CallInfo", "reset->enter");
        this.option = null;
        this.sponsor = "";
        this.uuid = "";
        this.memberMap.clear();
    }
}
