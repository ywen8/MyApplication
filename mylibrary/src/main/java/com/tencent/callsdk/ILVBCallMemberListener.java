package com.tencent.callsdk;

public abstract interface ILVBCallMemberListener
{
    public abstract void onCameraEvent(String paramString, boolean paramBoolean);

    public abstract void onMicEvent(String paramString, boolean paramBoolean);
}
