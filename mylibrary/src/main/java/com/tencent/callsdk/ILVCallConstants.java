package com.tencent.callsdk;

public class ILVCallConstants
{
    public static final String Module_CallSDK = "CallSDK";
    public static final int CALL_TYPE_AUDIO = 1;
    public static final int CALL_TYPE_VIDEO = 2;
    public static final String TCEXT_MAGIC = "CallNotification";
    public static final String TCKEY_CALLID = "AVRoomID";
    public static final String TCKEY_SPONSORID = "CallSponsor";
    public static final String TCKEY_MSGGROUPID = "IMGroupID";
    public static final String TCKEY_MSGGROUPTYPE = "IMGroupType";
    public static final String TCKEY_CALLTIP = "CallTip";
    public static final String TCKEY_CALLTYPE = "CallType";
    public static final String TCKEY_CALLDATE = "CallDate";
    public static final String TCKEY_SENDER = "Sender";
    public static final String TCKEY_CMD = "UserAction";
    public static final String TCKEY_TARGETS = "Targets";
    public static final String TCKEY_MEMBERS = "Memebers";
    public static final String TCKEY_UUID = "CallUUID";
    public static final String TCKEY_USERINFO = "CustomParam";
    public static final int TCILiveCMD_Call = 128;
    public static final int TCILiveCMD_Dialing = 129;
    public static final int TCILiveCMD_Accepted = 130;
    public static final int TCILiveCMD_SponsorCancel = 131;
    public static final int TCILiveCMD_SponsorTimeout = 132;
    public static final int TCILiveCMD_Reject = 133;
    public static final int TCILiveCMD_Hangup = 134;
    public static final int TCILiveCMD_LineBusy = 135;
    public static final int TCILiveCMD_HeartBeat = 136;
    public static final int TCILiveCMD_Inviting = 144;
    public static final int TCILiveCMD_CustomBegin = 1536;
    public static final int TCILiveCMD_CustomEnd = 2048;
    public static final int ERR_CALL_SPONSOR_CANCEL = 1;
    public static final int ERR_CALL_SPONSOR_TIMEOUT = 2;
    public static final int ERR_CALL_RESPONDER_REFUSE = 3;
    public static final int ERR_CALL_HANGUP = 4;
    public static final int ERR_CALL_RESPONDER_LINEBUSY = 5;
    public static final int ERR_CALL_DISCONNECT = 6;
    public static final int ERR_CALL_NOT_EXIST = 100;
    public static final int ERR_CALL_FAILED = 101;
    public static final int ERR_CALL_LOCAL_CANCEL = 102;
}
