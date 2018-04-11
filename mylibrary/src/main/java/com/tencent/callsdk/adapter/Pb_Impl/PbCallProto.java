package com.tencent.callsdk.adapter.Pb_Impl;

import android.text.TextUtils;
import com.tencent.callsdk.ILVCallNotification;
import com.tencent.callsdk.ILVCallOption;
import com.tencent.callsdk.adapter.CallProtoEngine;
import com.tencent.callsdk.data.CallInfo;
import com.tencent.callsdk.data.CallMsg;
import com.tencent.callsdk.data.IncomingInfo;
import com.tencent.ilivesdk.ILiveFunc;
import com.tencent.ilivesdk.core.ILiveLog;
import com.tencent.ilivesdk.core.ILiveLog.LogExts;
import com.tencent.ilivesdk.core.ILiveLoginManager;
import com.tencent.ilivesdk.core.ILiveRoomManager;
import com.tencent.mobileqq.pb.ByteStringMicro;
import com.tencent.mobileqq.pb.PBBytesField;
import com.tencent.mobileqq.pb.PBEnumField;
import com.tencent.mobileqq.pb.PBRepeatField;
import com.tencent.mobileqq.pb.PBStringField;
import com.tencent.mobileqq.pb.PBUInt32Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PbCallProto
        implements CallProtoEngine
{
    private static final int AVTYPE_AVSDK = 0;

    public class PbCallInfo
    {
        private int callid;

        public PbCallInfo() {}

        public int getCallid()
        {
            return this.callid;
        }

        public PbCallInfo setCallid(int callid)
        {
            this.callid = callid;
            return this;
        }
    }

    private static String TAG = "JsonCallProto";
    private HashMap<String, PbCallInfo> callHashMap = new HashMap();

    public byte[] getDailingData(String id, CallInfo info)
    {
        mwcall_cmd.CallCmd callCmd = buildCallCmd(info.getUuid());
        callCmd.enum_cmd_type.set(1);

        mwcall_cmd.SubCmd_0x1 cmd0x1 = new mwcall_cmd.SubCmd_0x1();
        if (!TextUtils.isEmpty(info.getOption().getCallTips())) {
            cmd0x1.str_invite_tip.set(info.getOption().getCallTips());
        }
        if (!TextUtils.isEmpty(info.getOption().getCustomParam())) {
            cmd0x1.bytes_custom_data.set(ByteStringMicro.copyFrom(info.getOption().getCustomParam().getBytes()));
        }
        cmd0x1.uint32_av_type.set(0);
        cmd0x1.uint32_room_id.set(info.getOption().getRoomId());
        cmd0x1.uint32_call_type.set(info.getOption().getCallType());
        cmd0x1.str_sponsor_id.set(info.getSponsor());

        callCmd.cmd_0x1.set(cmd0x1);

        this.callHashMap.put(info.getUuid(), new PbCallInfo().setCallid(info.getOption().getRoomId()));

        return callCmd.toByteArray();
    }

    public byte[] getDailingData(List<String> ids, CallInfo info)
    {
        mwcall_cmd.CallCmd callCmd = buildCallCmd(info.getUuid());
        callCmd.enum_cmd_type.set(1);

        mwcall_cmd.SubCmd_0x1 cmd0x1 = new mwcall_cmd.SubCmd_0x1();
        if (!TextUtils.isEmpty(info.getOption().getCallTips())) {
            cmd0x1.str_invite_tip.set(info.getOption().getCallTips());
        }
        if (!TextUtils.isEmpty(info.getOption().getCustomParam())) {
            cmd0x1.bytes_custom_data.set(ByteStringMicro.copyFrom(info.getOption().getCustomParam().getBytes()));
        }
        cmd0x1.uint32_av_type.set(0);
        cmd0x1.uint32_room_id.set(info.getOption().getRoomId());
        cmd0x1.uint32_call_type.set(info.getOption().getCallType());
        cmd0x1.str_sponsor_id.set(info.getSponsor());
        cmd0x1.str_member_id.addAll(ids);

        callCmd.cmd_0x1.set(cmd0x1);

        this.callHashMap.put(info.getUuid(), new PbCallInfo().setCallid(info.getOption().getRoomId()));

        return callCmd.toByteArray();
    }

    public byte[] getInviteData(List<String> ids, CallInfo info)
    {
        mwcall_cmd.CallCmd callCmd = buildCallCmd(info.getUuid());
        callCmd.enum_cmd_type.set(8);

        mwcall_cmd.SubCmd_0x1 cmd0x1 = new mwcall_cmd.SubCmd_0x1();
        if (!TextUtils.isEmpty(info.getOption().getCallTips())) {
            cmd0x1.str_invite_tip.set(info.getOption().getCallTips());
        }
        if (!TextUtils.isEmpty(info.getOption().getCustomParam())) {
            cmd0x1.bytes_custom_data.set(ByteStringMicro.copyFrom(info.getOption().getCustomParam().getBytes()));
        }
        cmd0x1.uint32_av_type.set(0);
        cmd0x1.uint32_room_id.set(info.getOption().getRoomId());
        cmd0x1.uint32_call_type.set(info.getOption().getCallType());
        cmd0x1.str_sponsor_id.set(info.getSponsor());
        cmd0x1.str_member_id.addAll(ids);

        callCmd.cmd_0x1.set(cmd0x1);

        this.callHashMap.put(info.getUuid(), new PbCallInfo().setCallid(info.getOption().getRoomId()));

        return callCmd.toByteArray();
    }

    public byte[] getSponsorCancelData(CallInfo info)
    {
        mwcall_cmd.CallCmd callCmd = buildCallCmd(info.getUuid());
        callCmd.enum_cmd_type.set(2);
        return callCmd.toByteArray();
    }

    public byte[] getAcceptData(IncomingInfo info)
    {
        mwcall_cmd.CallCmd callCmd = buildCallCmd(info.getUuid());
        callCmd.enum_cmd_type.set(3);

        mwcall_cmd.SubCmd_0x3 cmd0x3 = new mwcall_cmd.SubCmd_0x3();
        cmd0x3.uint32_av_type.set(0);
        cmd0x3.uint32_accept_call_type.set(info.getCallType());
        callCmd.cmd_0x3.set(cmd0x3);
        return callCmd.toByteArray();
    }

    public byte[] getRejectData(IncomingInfo info)
    {
        mwcall_cmd.CallCmd callCmd = buildCallCmd(info.getUuid());
        callCmd.enum_cmd_type.set(4);
        return callCmd.toByteArray();
    }

    public byte[] getHangUpData(CallInfo info)
    {
        mwcall_cmd.CallCmd callCmd = buildCallCmd(info.getUuid());
        callCmd.enum_cmd_type.set(5);
        return callCmd.toByteArray();
    }

    public byte[] getLineBusyData(IncomingInfo info)
    {
        mwcall_cmd.CallCmd callCmd = buildCallCmd(info.getUuid());
        callCmd.enum_cmd_type.set(6);
        return callCmd.toByteArray();
    }

    public byte[] getSponsorTimeOutData(CallInfo info)
    {
        mwcall_cmd.CallCmd callCmd = buildCallCmd(info.getUuid());
        callCmd.enum_cmd_type.set(7);
        return callCmd.toByteArray();
    }

    public byte[] getDeviceEventData(List<String> ids, CallInfo info)
    {
        return new byte[0];
    }

    public byte[] getHeartBeatData(CallInfo info)
    {
        mwcall_cmd.CallCmd callCmd = buildCallCmd(info.getUuid());
        callCmd.enum_cmd_type.set(153);
        return callCmd.toByteArray();
    }

    public byte[] getNotificationData(ILVCallNotification notification, CallInfo info)
    {
        mwcall_cmd.CallCmd callCmd = buildCallCmd(info.getUuid());
        callCmd.enum_cmd_type.set(9);

        mwcall_cmd.SubCmd_0x9 cmd0x9 = new mwcall_cmd.SubCmd_0x9();
        cmd0x9.uint32_notification_id.set(notification.getNotifId());
        if (!TextUtils.isEmpty(notification.getNotifDesc())) {
            cmd0x9.str_custom_desc.set(notification.getNotifDesc());
        }
        if (!TextUtils.isEmpty(notification.getUserInfo())) {
            cmd0x9.str_custom_info.set(notification.getUserInfo());
        }
        callCmd.cmd_0x9.set(cmd0x9);

        return callCmd.toByteArray();
    }

    public byte[] getNotificationData(ILVCallNotification notification, IncomingInfo info)
    {
        mwcall_cmd.CallCmd callCmd = buildCallCmd(info.getUuid());
        callCmd.enum_cmd_type.set(9);

        mwcall_cmd.SubCmd_0x9 cmd0x9 = new mwcall_cmd.SubCmd_0x9();
        cmd0x9.uint32_notification_id.set(notification.getNotifId());
        cmd0x9.str_custom_desc.set(notification.getNotifDesc());
        cmd0x9.str_custom_info.set(notification.getUserInfo());

        callCmd.cmd_0x9.set(cmd0x9);

        return callCmd.toByteArray();
    }

    public CallMsg parseData(byte[] data, String senderId)
    {
        CallMsg callMsg = new CallMsg();

        mwcall_cmd.CallCmd callCmd = new mwcall_cmd.CallCmd();
        try
        {
            callCmd.mergeFrom(data);

            callMsg.setSender(senderId);
            callMsg.setTimeStamp(callCmd.uint32_time.get());
            callMsg.setUuid(callCmd.str_session_id.get());
            callMsg.setTargets(new ArrayList());
            PbCallInfo info = (PbCallInfo)this.callHashMap.get(callCmd.str_session_id.get());
            if (null != info) {
                callMsg.setCallId(info.getCallid());
            } else {
                callMsg.setCallId(ILiveRoomManager.getInstance().getRoomId());
            }
            switch (callCmd.enum_cmd_type.get())
            {
                case 1:
                    mwcall_cmd.SubCmd_0x1 dailCmd0x1 = (mwcall_cmd.SubCmd_0x1)callCmd.cmd_0x1.get();
                    callMsg.setNotifId(129);
                    callMsg.setCallId(dailCmd0x1.uint32_room_id.get());
                    callMsg.setSponserId(senderId);
                    callMsg.setNotifDesc(dailCmd0x1.str_invite_tip.get());
                    callMsg.setCallType(dailCmd0x1.uint32_call_type.get());
                    callMsg.setUserInfo(new String(dailCmd0x1.bytes_custom_data.get().toByteArray(), "UTF-8"));
                    List<String> dailMembers = dailCmd0x1.str_member_id.get();
                    if (null != dailMembers) {
                        for (String member : dailMembers) {
                            callMsg.addMember(member);
                        }
                    }
                    this.callHashMap.put(callMsg.getUuid(), new PbCallInfo().setCallid(callMsg.getCallId()));
                    break;
                case 8:
                    mwcall_cmd.SubCmd_0x1 inviteCmd0x1 = (mwcall_cmd.SubCmd_0x1)callCmd.cmd_0x1.get();
                    callMsg.setNotifId(144);
                    callMsg.setCallId(inviteCmd0x1.uint32_room_id.get());
                    callMsg.setSponserId(senderId);
                    callMsg.setNotifDesc(inviteCmd0x1.str_invite_tip.get());
                    callMsg.setCallType(inviteCmd0x1.uint32_call_type.get());
                    callMsg.setUserInfo(new String(inviteCmd0x1.bytes_custom_data.get().toByteArray(), "UTF-8"));
                    List<String> inviteMembers = inviteCmd0x1.str_member_id.get();
                    if (null != inviteMembers) {
                        for (String member : inviteMembers) {
                            callMsg.addMember(member);
                        }
                    }
                    this.callHashMap.put(callMsg.getUuid(), new PbCallInfo().setCallid(callMsg.getCallId()));
                    break;
                case 2:
                    callMsg.setNotifId(131);
                    break;
                case 3:
                    mwcall_cmd.SubCmd_0x3 cmd0x3 = (mwcall_cmd.SubCmd_0x3)callCmd.cmd_0x3.get();
                    callMsg.setCallType(cmd0x3.uint32_accept_call_type.get());
                    break;
                case 4:
                    callMsg.setNotifId(133);
                    break;
                case 5:
                    callMsg.setNotifId(134);
                    break;
                case 6:
                    callMsg.setNotifId(135);
                    break;
                case 7:
                    callMsg.setNotifId(132);
                    break;
                case 9:
                    mwcall_cmd.SubCmd_0x9 cmd0x9 = (mwcall_cmd.SubCmd_0x9)callCmd.cmd_0x9.get();
                    callMsg.setNotifId(cmd0x9.uint32_notification_id.get());
                    callMsg.setNotifDesc(cmd0x9.str_custom_desc.get());
                    callMsg.setUserInfo(cmd0x9.str_custom_info.get());
                    break;
                case 153:
                    callMsg.setNotifId(136);
            }
        }
        catch (Exception e)
        {
            ILiveLog.dw(TAG, "parseData->error", new ILiveLog.LogExts().put("exception", e.toString()));
        }
        return callMsg;
    }

    private mwcall_cmd.CallCmd buildCallCmd(String uuid)
    {
        mwcall_cmd.CallCmd callCmd = new mwcall_cmd.CallCmd();
        callCmd.uint32_time.set((int)ILiveFunc.getCurrentSec());
        callCmd.str_cmd_user_id.set(ILiveLoginManager.getInstance().getMyUserId());
        callCmd.str_session_id.set(uuid);
        return callCmd;
    }
}
