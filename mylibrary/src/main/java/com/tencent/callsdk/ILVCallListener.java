package com.tencent.callsdk;

public abstract interface ILVCallListener
{
    public abstract void onCallEstablish(int paramInt);

    public abstract void onCallEnd(int paramInt1, int paramInt2, String paramString);

    public abstract void onException(int paramInt1, int paramInt2, String paramString);
}
