package com.tencent.callsdk.adapter.Json_Impl;

import android.text.TextUtils;
import com.tencent.callsdk.ILVCallMemberInfo;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JsonCallProto
        implements CallProtoEngine
{
    private static String TAG = "JsonCallProto";

    public byte[] getDailingData(String id, CallInfo curInfo)
    {
        JSONObject inviteParam = getComParamJsonObj(curInfo.getOption().getRoomId(), curInfo.getOption().getCallType(), 129, curInfo.getUuid());
        try
        {
            inviteParam.put("CallTip", curInfo.getOption().getCallTips());
            inviteParam.put("CallSponsor", curInfo.getSponsor());
            inviteParam.put("Sender", ILiveLoginManager.getInstance().getMyUserId());
            if (curInfo.getOption().isIMSupport())
            {
                inviteParam.put("IMGroupID", ILiveRoomManager.getInstance().getIMGroupId());
                inviteParam.put("IMGroupType", curInfo.getOption().getGroupType());
            }
            if (!TextUtils.isEmpty(curInfo.getOption().getCustomParam())) {
                inviteParam.put("CustomParam", curInfo.getOption().getCustomParam());
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            ILiveLog.kw(TAG, "buildDialingCmd", new ILiveLog.LogExts().put("exception", e.toString()));
        }
        return inviteParam.toString().getBytes();
    }

    public byte[] getDailingData(List<String> ids, CallInfo curInfo)
    {
        JSONObject inviteParam = getComParamJsonObj(curInfo.getOption().getRoomId(), curInfo.getOption().getCallType(), 129, curInfo.getUuid());
        try
        {
            inviteParam.put("CallTip", curInfo.getOption().getCallTips());
            inviteParam.put("CallSponsor", curInfo.getSponsor());
            inviteParam.put("Sender", ILiveLoginManager.getInstance().getMyUserId());
            if (curInfo.getOption().isIMSupport())
            {
                inviteParam.put("IMGroupID", ILiveRoomManager.getInstance().getIMGroupId());
                inviteParam.put("IMGroupType", curInfo.getOption().getGroupType());
            }
            if (null != ids)
            {
                JSONArray jsonTargets = new JSONArray();
                JSONArray jsonMembers = new JSONArray();
                for (String user : ids)
                {
                    jsonTargets.put(user);
                    jsonMembers.put(user);
                }
                jsonMembers.put(ILiveLoginManager.getInstance().getMyUserId());
                inviteParam.put("Targets", jsonTargets);
                inviteParam.put("Memebers", jsonMembers);
            }
            if (!TextUtils.isEmpty(curInfo.getOption().getCustomParam())) {
                inviteParam.put("CustomParam", curInfo.getOption().getCustomParam());
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            ILiveLog.kw(TAG, "buildDialingCmd", new ILiveLog.LogExts().put("exception", e.toString()));
        }
        return inviteParam.toString().getBytes();
    }

    public byte[] getInviteData(List<String> ids, CallInfo curInfo)
    {
        JSONObject inviteParam = getComParamJsonObj(curInfo.getOption().getRoomId(), curInfo.getOption().getCallType(), 129, curInfo.getUuid());
        try
        {
            inviteParam.put("CallTip", curInfo.getOption().getCallTips());
            inviteParam.put("CallSponsor", curInfo.getSponsor());
            inviteParam.put("Sender", ILiveLoginManager.getInstance().getMyUserId());
            if (curInfo.getOption().isIMSupport())
            {
                inviteParam.put("IMGroupID", ILiveRoomManager.getInstance().getIMGroupId());
                inviteParam.put("IMGroupType", curInfo.getOption().getGroupType());
            }
            if (null != ids)
            {
                JSONArray jsonTargets = new JSONArray();
                JSONArray jsonMembers = new JSONArray();
                if (null != curInfo.getMemberMap()) {
                    for (Map.Entry<String, ILVCallMemberInfo> entry : curInfo.getMemberMap().entrySet()) {
                        jsonMembers.put(entry.getKey());
                    }
                }
                for (String user : ids)
                {
                    jsonTargets.put(user);
                    jsonMembers.put(user);
                }
                jsonMembers.put(ILiveLoginManager.getInstance().getMyUserId());
                inviteParam.put("Targets", jsonTargets);
                inviteParam.put("Memebers", jsonMembers);
            }
            if (!TextUtils.isEmpty(curInfo.getOption().getCustomParam())) {
                inviteParam.put("CustomParam", curInfo.getOption().getCustomParam());
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            ILiveLog.kw(TAG, "buildDialingCmd", new ILiveLog.LogExts().put("exception", e.toString()));
        }
        return inviteParam.toString().getBytes();
    }

    public byte[] getAcceptData(IncomingInfo info)
    {
        JSONObject jsonAccept = getComParamJsonObj(info.getCallId(), info.getCallType(), 130, info
                .getUuid());
        addTargets(jsonAccept, info.getUserId());
        return jsonAccept.toString().getBytes();
    }

    public byte[] getRejectData(IncomingInfo info)
    {
        JSONObject jsonReject = getComParamJsonObj(info.getCallId(), info.getCallType(), 133, info
                .getUuid());
        addTargets(jsonReject, info.getUserId());
        return jsonReject.toString().getBytes();
    }

    public byte[] getHangUpData(CallInfo info)
    {
        JSONObject jsonObj = getComParamJsonObj(info.getOption().getRoomId(), info.getOption().getCallType(), 134, info
                .getUuid());
        addTargets(jsonObj, info);
        return jsonObj.toString().getBytes();
    }

    public byte[] getLineBusyData(IncomingInfo info)
    {
        JSONObject jsonObject = getComParamJsonObj(info.getCallId(), info.getCallType(), 135, info
                .getUuid());
        return jsonObject.toString().getBytes();
    }

    public byte[] getDeviceEventData(List<String> ids, CallInfo info)
    {
        return new byte[0];
    }

    public byte[] getHeartBeatData(CallInfo curInfo)
    {
        JSONObject jsonObj = getComParamJsonObj(curInfo.getOption().getRoomId(), curInfo.getOption().getCallType(), 136, curInfo
                .getUuid());
        return jsonObj.toString().getBytes();
    }

    public byte[] getSponsorCancelData(CallInfo info)
    {
        JSONObject jsonObj = getComParamJsonObj(info.getOption().getRoomId(), info.getOption().getCallType(), 131, info
                .getUuid());
        addTargets(jsonObj, info);
        return jsonObj.toString().getBytes();
    }

    public byte[] getSponsorTimeOutData(CallInfo info)
    {
        JSONObject jsonObj = getComParamJsonObj(info.getOption().getRoomId(), info.getOption().getCallType(), 132, info
                .getUuid());
        addTargets(jsonObj, info);
        return jsonObj.toString().getBytes();
    }

    public byte[] getNotificationData(ILVCallNotification notification, CallInfo info)
    {
        JSONObject jsonNotification = getComParamJsonObj(info.getOption().getRoomId(), notification.getNotifId(), info
                .getOption().getCallType(), notification.getTimeStamp(), notification.getSender(), info.getUuid());
        try
        {
            jsonNotification.put("CallTip", notification.getNotifDesc());
            jsonNotification.put("CustomParam", notification.getUserInfo());
            jsonNotification.put("Targets", notification.getTargets());
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return jsonNotification.toString().getBytes();
    }

    public byte[] getNotificationData(ILVCallNotification notification, IncomingInfo info)
    {
        JSONObject jsonNotification = getComParamJsonObj(info.getCallId(), notification.getNotifId(), info
                .getCallType(), notification.getTimeStamp(), notification.getSender(), info.getUuid());
        try
        {
            jsonNotification.put("CallTip", notification.getNotifDesc());
            jsonNotification.put("CustomParam", notification.getUserInfo());
            jsonNotification.put("Targets", notification.getTargets());
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return jsonNotification.toString().getBytes();
    }

    public CallMsg parseData(byte[] data, String senderId)
    {
        CallMsg callMsg = new CallMsg();
        try
        {
            String strMsg = new String(data, "UTF-8");
            JSONTokener jsonTokener = new JSONTokener(strMsg);
            JSONObject jsonParam = (JSONObject)jsonTokener.nextValue();
            int cmd = jsonParam.getInt("UserAction");
            ILiveLog.di(TAG, "notifyMessage", new ILiveLog.LogExts().put("cmd", "0x" + Integer.toHexString(cmd))
                    .put("sender", senderId)
                    .put("msg", strMsg));

            ((CallMsg)((CallMsg)((CallMsg)((CallMsg)((CallMsg)((CallMsg)callMsg.setCallId(jsonParam.optInt("AVRoomID")).setNotifId(cmd)).setNotifDesc(jsonParam.optString("CallTip"))).setSender(senderId)).setTargets(parseJsonStringArray(jsonParam.optJSONArray("Targets")))).setTimeStamp(jsonParam.optLong("CallDate"))).setUuid(jsonParam.optString("CallUUID")))
                    .setUserInfo(jsonParam.optString("CustomParam"));
            if ((cmd == 129) || (cmd == 144))
            {
                callMsg.setCallType(jsonParam.getInt("CallType")).setSponserId(jsonParam.optString("CallSponsor", "")).setImGrpId(jsonParam.getString("IMGroupID")).setImGrpType(jsonParam.optString("IMGroupType", "Private"));
                JSONArray jsonMembers = jsonParam.optJSONArray("Memebers");
                if (null != jsonMembers) {
                    for (int i = 0; i < jsonMembers.length(); i++)
                    {
                        String member = jsonMembers.getString(i);
                        if (!ILiveLoginManager.getInstance().getMyUserId().equals(member)) {
                            callMsg.addMember(member);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            ILiveLog.dw(TAG, "parseData->error", new ILiveLog.LogExts().put("exception", e.toString()));
        }
        return callMsg;
    }

    private JSONObject getComParamJsonObj(int roomid, int cmd, int callType, long date, String sender, String uuid)
    {
        JSONObject param = new JSONObject();
        try
        {
            param.put("UserAction", cmd);
            param.put("AVRoomID", roomid);
            param.put("CallType", callType);
            param.put("CallDate", date);
            param.put("Sender", sender);
            param.put("CallUUID", uuid);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return param;
    }

    private JSONObject getComParamJsonObj(int roomId, int callType, int cmd, String uuid)
    {
        return getComParamJsonObj(roomId, cmd, callType,
                ILiveFunc.getCurrentSec(), ILiveLoginManager.getInstance().getMyUserId(), uuid);
    }

    private void addTargets(JSONObject jsonCmd, CallInfo curInfo)
    {
        try
        {
            JSONArray jsonTargets = new JSONArray();
            for (Map.Entry<String, ILVCallMemberInfo> entry : curInfo.getMemberMap().entrySet()) {
                jsonTargets.put(entry.getKey());
            }
            jsonCmd.put("Targets", jsonTargets);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void addTargets(JSONObject jsonCmd, String sender)
    {
        try
        {
            JSONArray jsonTargets = new JSONArray();
            jsonTargets.put(sender);
            jsonCmd.put("Targets", jsonTargets);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private List<String> parseJsonStringArray(JSONArray jsonArray)
            throws JSONException
    {
        ArrayList<String> arrayList = new ArrayList();
        if (null != jsonArray) {
            for (int i = 0; i < jsonArray.length(); i++) {
                arrayList.add(jsonArray.getString(i));
            }
        }
        return arrayList;
    }
}
