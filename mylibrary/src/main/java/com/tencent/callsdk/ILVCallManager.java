package com.tencent.callsdk;

import com.tencent.TIMMessage;
import com.tencent.callsdk.data.CallMsg;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.view.AVRootView;
import java.util.List;

public abstract class ILVCallManager
{
    protected ILVCallConfig mConfig = null;
    protected String version = "1.0.27";

    private static class ILVBCallHolder
    {
        private static ILVCallManager instance = new CallMgr();
    }

    public static ILVCallManager getInstance()
    {
        return ILVBCallHolder.instance;
    }

    public abstract int init(ILVCallConfig paramILVCallConfig);

    public abstract void shutdown();

    public abstract String getVersion();

    public abstract int addIncomingListener(ILVIncomingListener paramILVIncomingListener);

    public abstract int removeIncomingListener(ILVIncomingListener paramILVIncomingListener);

    public abstract int addCallListener(ILVCallListener paramILVCallListener);

    public abstract int removeCallListener(ILVCallListener paramILVCallListener);

    public abstract int makeCall(String paramString, ILVCallOption paramILVCallOption, ILiveCallBack paramILiveCallBack);

    @Deprecated
    public int makeCall(String userId, ILVCallOption option)
    {
        return makeCall(userId, option, null);
    }

    public abstract int makeMutiCall(List<String> paramList, ILVCallOption paramILVCallOption, ILiveCallBack paramILiveCallBack);

    @Deprecated
    public int makeMutiCall(List<String> nums, ILVCallOption option)
    {
        return makeMutiCall(nums, option, null);
    }

    public abstract int acceptCall(int paramInt, ILVCallOption paramILVCallOption);

    public abstract int rejectCall(int paramInt);

    public abstract int responseLineBusy(int paramInt);

    public abstract int endCall(int paramInt);

    public abstract int inviteUser(int paramInt, List<String> paramList);

    public abstract int initAvView(AVRootView paramAVRootView);

    public abstract int enableMic(boolean paramBoolean);

    public abstract int enableSpeaker(boolean paramBoolean);

    public abstract int enableCamera(int paramInt, boolean paramBoolean);

    public abstract int getCurCameraId();

    public abstract int switchCamera(int paramInt);

    public abstract int sendC2CMessage(String paramString, TIMMessage paramTIMMessage, ILiveCallBack<TIMMessage> paramILiveCallBack);

    public abstract int sendGroupMessage(TIMMessage paramTIMMessage, ILiveCallBack<TIMMessage> paramILiveCallBack);

    public abstract int sendC2COnlineMessage(String paramString, TIMMessage paramTIMMessage, ILiveCallBack<TIMMessage> paramILiveCallBack);

    public abstract int sendGroupOnlineMessage(TIMMessage paramTIMMessage, ILiveCallBack<TIMMessage> paramILiveCallBack);

    public abstract int enableBeauty(float paramFloat);

    public abstract int enableWhite(float paramFloat);

    public abstract void onPause();

    public abstract void onResume();

    public abstract void onDestory();

    public abstract int postNotification(int paramInt, ILVCallNotification paramILVCallNotification);

    @Deprecated
    public abstract boolean notifyMessage(String paramString1, String paramString2, long paramLong);

    public abstract boolean notifyMessage(CallMsg paramCallMsg);

    public abstract String getSponsorId();

    public abstract List<ILVCallMemberInfo> getMembers();
}
