package com.tencent.callsdk;

import com.tencent.ilivesdk.core.ILiveRoomOption;

public class ILVCallOption
        extends ILiveRoomOption<ILVCallOption>
{
    private int mCallType = 1;
    private int mRoomId = 0;
    private String mCallTips = "New Call";
    private String mCustomParam;
    private ILVBCallMemberListener mMemberListener;
    private long iHearBeatInterval = 10L;
    private boolean bOnlineCall = false;

    public ILVCallOption(String hostId)
    {
        super(hostId);

        ((ILVCallOption)((ILVCallOption)((ILVCallOption)((ILVCallOption)super.autoRender(true)).highAudioQuality(true)).imsupport(true)).autoMic(true))
                .groupType("Private");
    }

    public ILVCallOption setCallType(int callType)
    {
        this.mCallType = callType;
        autoCamera(2 == callType);
        return this;
    }

    public ILVCallOption setRoomId(int roomId)
    {
        this.mRoomId = roomId;
        return this;
    }

    public ILVCallOption setMemberListener(ILVBCallMemberListener memberListener)
    {
        this.mMemberListener = memberListener;
        return this;
    }

    public ILVCallOption callTips(String strTips)
    {
        this.mCallTips = strTips;
        return this;
    }

    public ILVCallOption customParam(String strParam)
    {
        this.mCustomParam = strParam;
        return this;
    }

    public ILVCallOption heartBeatInterval(long interval)
    {
        this.iHearBeatInterval = interval;
        return this;
    }

    public ILVCallOption setOnlineCall(boolean bEnable)
    {
        this.bOnlineCall = bEnable;
        return this;
    }

    public int getCallType()
    {
        return this.mCallType;
    }

    public boolean isVideoCall()
    {
        return 2 == this.mCallType;
    }

    public boolean isOnlineCall()
    {
        return this.bOnlineCall;
    }

    public int getRoomId()
    {
        return this.mRoomId;
    }

    public long getHearBeatInterval()
    {
        return this.iHearBeatInterval;
    }

    public String getCustomParam()
    {
        return this.mCustomParam;
    }

    public String getCallTips()
    {
        return this.mCallTips;
    }

    public ILVBCallMemberListener getMemberListener()
    {
        return this.mMemberListener;
    }
}
