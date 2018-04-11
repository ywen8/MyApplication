package com.tencent.callsdk;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.tencent.TIMCallBack;
import com.tencent.TIMConversation;
import com.tencent.TIMConversationType;
import com.tencent.TIMCustomElem;
import com.tencent.TIMElem;
import com.tencent.TIMElemType;
import com.tencent.TIMGroupManager;
import com.tencent.TIMGroupMemberResult;
import com.tencent.TIMManager;
import com.tencent.TIMMessage;
import com.tencent.TIMMessageListener;
import com.tencent.TIMValueCallBack;
import com.tencent.callsdk.adapter.CallProtoEngine;
import com.tencent.callsdk.adapter.Json_Impl.JsonCallProto;
import com.tencent.callsdk.adapter.Pb_Impl.PbCallProto;
import com.tencent.callsdk.data.CallInfo;
import com.tencent.callsdk.data.CallMsg;
import com.tencent.callsdk.data.IncomingInfo;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.ILiveConstants;
import com.tencent.ilivesdk.ILiveFunc;
import com.tencent.ilivesdk.ILiveMemStatusLisenter;
import com.tencent.ilivesdk.core.ILiveLog;
import com.tencent.ilivesdk.core.ILiveLoginManager;
import com.tencent.ilivesdk.core.ILiveRoomManager;
import com.tencent.ilivesdk.core.ILiveRoomOption;
import com.tencent.ilivesdk.view.AVRootView;
import com.tencent.imsdk.util.QualityReportHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 视频通话实现类
 */
public class CallMgr extends ILVCallManager implements TIMMessageListener, ILiveMemStatusLisenter, ILiveRoomOption.onRoomDisconnectListener {
    private final static String TAG = "ILVB-CallMgr";

    private Handler mMainHandler = new Handler(Looper.getMainLooper());
    private AVRootView mAvRootView = null;      // 视频显示控件

    private CallProtoEngine protoEngine = null;
    private CallInfo curInfo = new CallInfo();
    private QualityReportHelper helper = new QualityReportHelper();
    private ILiveMemStatusLisenter mAppStatusListener = null;   // 备份用户设置的listener
    private ILiveRoomOption.onRoomDisconnectListener mAppDisconnectListener = null;    // 备份用户设置的diconnect回调
    private boolean bInviteSelf = false;
    private HashMap<String, ILVCallNotification> overMap = new HashMap<>();       // 用于缓存已结束的会话

    // 内部通话状态
    private enum CallStatus {
        eCallIdle,          // 空闲状态
        eCallIncoming,      // 正在接听来电
        eCallEnter,         // 正在进入房间
        eCallWait,          // 等待加入
        eCallEstablish,     // 建立成功
        eCallEnd,           // 呼叫结束
    }

    /**
     * 通话状态
     */
    private List<ILVIncomingListener> mIncomingListenrList = new ArrayList<>();
    private List<ILVCallListener> mListenerList = new ArrayList<>();
    private SparseArray<IncomingInfo> mIncomingMap = new SparseArray<>();
    private CallStatus mStatus = CallStatus.eCallIdle;
    private Runnable mTimeoutRunable = null;
    private Runnable mHeartBeatRunable = new Runnable() {
        @Override
        public void run() {
            if (null != curInfo.getOption()) {
                if (curInfo.getOption().isIMSupport()) {
                    sendGroupMessage(true, protoEngine.getHeartBeatData(curInfo), null);
                } else {
                    sendMembersMessage(true, protoEngine.getHeartBeatData(curInfo), curInfo.getMemberMap());
                }
                List<ILVCallMemberInfo> infos = getMembers();
                long curTime = ILiveFunc.getCurrentSec();
                for (ILVCallMemberInfo info : infos) {
                    long dt = curTime - info.getTimeStamp();
                    ILiveLog.dd(TAG, "heartbeat", new ILiveLog.LogExts().put("dt", dt).put("limit", curInfo.getOption().getHearBeatInterval() << 1));
                    if (dt > (curInfo.getOption().getHearBeatInterval() << 1)) {    // 未收到心跳超过两次
                        updateMember(info.getId(), false, true);
                    }
                }
                mMainHandler.postDelayed(this, curInfo.getOption().getHearBeatInterval() * 1000);
            }
        }
    };

    // 内部方法
    private boolean changeStatus(CallStatus newStatus) {
        boolean bRet = false;
        switch (mStatus) {
            case eCallIdle:
                bRet = (CallStatus.eCallEnter == newStatus || CallStatus.eCallIncoming == newStatus);
                break;
            case eCallIncoming:
                bRet = (CallStatus.eCallEnter == newStatus || CallStatus.eCallEstablish == newStatus || CallStatus.eCallEnd == newStatus);
                break;
            case eCallEnter:
                bRet = (CallStatus.eCallEstablish == newStatus || CallStatus.eCallWait == newStatus || CallStatus.eCallEnd == newStatus);
                break;
            case eCallWait:
                bRet = (CallStatus.eCallEstablish == newStatus || CallStatus.eCallEnd == newStatus);
                break;
            case eCallEstablish:
                bRet = (CallStatus.eCallEnd == newStatus);
                break;
            case eCallEnd:
                bRet = (CallStatus.eCallIdle == newStatus);
                break;
        }

        ILiveLog.ki(TAG, "changeStatus", new ILiveLog.LogExts().put("oldStatus", mStatus)
                .put("newStatus", newStatus)
                .put("ret", bRet));
        if (bRet) {
            mStatus = newStatus;
        }

        return bRet;
    }

    private void notifyMemberStatus(String id, boolean bJoined, boolean bFixed) {
        if (!mConfig.isMemberStatusFix() && bFixed) {   // 若未开启成员状态修订则忽略
            ILiveLog.dd(TAG, "notifyMemberStatus->ignore", new ILiveLog.LogExts().put("id", id).put("join", bJoined));
        }

        if (bJoined && !ILiveLoginManager.getInstance().getMyUserId().equals(id)) {
            // 有新成员加入
            if (CallStatus.eCallWait == mStatus) {   // 正在等待用户加入
                if (changeStatus(CallStatus.eCallEstablish)) {
                    ILiveLog.ki(TAG, "notifyMemberStatus->accept", new ILiveLog.LogExts().put("id", id));
                    mMainHandler.removeCallbacks(mTimeoutRunable);
                    mTimeoutRunable = null;
                    notifyCallEstablish(ILiveRoomManager.getInstance().getRoomId());
                }
            }
        }
    }

    // 成员相关
    private void updateMember(String id, boolean bJoined, boolean bFixed) {
        if (ILiveLoginManager.getInstance().getMyUserId().equals(id)) {  // 忽略自己
            return;
        }

        ILVCallMemberInfo info = null;
        if (curInfo.getMemberMap().containsKey(id)) {
            info = curInfo.getMemberMap().get(id);
            info.setTimeStamp(ILiveFunc.getCurrentSec());
        } else {
            info = new ILVCallMemberInfo(id);
            curInfo.getMemberMap().put(id, info);
            ILiveLog.dd(TAG, "updateMember", new ILiveLog.LogExts().put("id", id));
        }

        if (info.isJoin() != bJoined) {
            if (!mConfig.isMemberStatusFix() && bFixed) {   // 若未开启成员状态修订则忽略
                ILiveLog.dd(TAG, "updateMember->ignore", new ILiveLog.LogExts().put("id", id).put("join", bJoined));
            } else {
                info.setJoin(bJoined);
            }
            notifyMemberStatus(id, bJoined, bFixed);
        }
    }

    // 获取成员状态
    private boolean isMemberJoined(String id) {
        if (curInfo.getMemberMap().containsKey(id)) {
            return curInfo.getMemberMap().get(id).isJoin();
        }
        return false;
    }

    // 删除成员
    private void removeMember(String id) {
        if (curInfo.getMemberMap().containsKey(id)) {
            curInfo.getMemberMap().remove(id);
        }
        ILiveLog.di(TAG, "removeMember", new ILiveLog.LogExts().put("id", id).put("size", curInfo.getMemberMap().size()));
    }

    private ILVCallMemberInfo findMember(String id) {
        return curInfo.getMemberMap().get(id);
    }

    // 通知类
    private void notifyNewCall(int callId, int callType, ILVIncomingNotification notification) {
        if (null != mIncomingListenrList && !mIncomingListenrList.isEmpty()) {
            // 复制一个List，避免用户上层remove引起异常
            ArrayList<ILVIncomingListener> tmpList = new ArrayList<>(mIncomingListenrList);
            for (ILVIncomingListener listener : tmpList) {
                listener.onNewIncomingCall(callId, callType, notification);
            }
            ILiveLog.dd(TAG, "notifyNewCall", new ILiveLog.LogExts().put("size", tmpList.size()));
        } else {
            ILiveLog.kw(TAG, "notifyNewCall->no-listener");
        }
    }

    private void notifyCallEstablish(int callId) {
        if (null != mListenerList && 0 != mListenerList.size()) {
            // 复制一个List，避免用户上层remove引起异常
            ArrayList<ILVCallListener> tmpList = new ArrayList<>(mListenerList);
            for (ILVCallListener listener : tmpList) {
                listener.onCallEstablish(callId);
            }
            ILiveLog.dd(TAG, "notifyCallEstablish", new ILiveLog.LogExts().put("size", tmpList.size()));
        } else {
            ILiveLog.kw(TAG, "notifyCallEstablish->no-listenter");
        }
    }

    private void notifyCallEnd(int callId, int endResult, String endInfo) {
        removeHeartBeat();
        if (null != mListenerList && 0 != mListenerList.size()) {
            // 复制一个List，避免用户上层remove引起异常
            ArrayList<ILVCallListener> tmpList = new ArrayList<>(mListenerList);
            for (ILVCallListener listener : tmpList) {
                listener.onCallEnd(callId, endResult, endInfo);
            }
            ILiveLog.dd(TAG, "notifyCallEnd", new ILiveLog.LogExts().put("size", tmpList.size()));
        } else {
            ILiveLog.kw(TAG, "notifyCallEnd->no-listenter");
        }
    }

    private void notifyException(int iExceptionId, int errCode, String errMsg) {
        if (null != mListenerList && 0 != mListenerList.size()) {
            // 复制一个List，避免用户上层remove引起异常
            ArrayList<ILVCallListener> tmpList = new ArrayList<>(mListenerList);
            for (ILVCallListener listener : tmpList) {
                listener.onException(iExceptionId, errCode, errMsg);
            }
        } else {
            ILiveLog.kw(TAG, "notifyException->no-listenter");
        }
    }

    // 退出AV房间
    private void quitAVRoom(final int roomId, final int reason, final String message) {
        ILiveRoomManager.getInstance().quitRoom(new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                changeStatus(CallStatus.eCallIdle);
                notifyCallEnd(roomId, reason, message);
                curInfo.reset();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                ILiveLog.ke(TAG, "quitAVRoom", module, errCode, errMsg);
                changeStatus(CallStatus.eCallIdle);
                notifyCallEnd(roomId, reason, message);
            }
        });
    }

    // 生成UUID
    private String generateUUID() {
        return (new Random().nextInt(10000) + "_"
                + ILiveLoginManager.getInstance().getMyUserId() + "_"
                + ILiveFunc.getCurrentSec());
    }

    // 群发通话信令
    private void sendUsersMessage(boolean bOnline, byte[] data, List<String> ids, ILiveCallBack cbRet) {
        for (int i = 0; i < ids.size(); i++) {
            sendC2CMessage(bOnline, data, ids.get(i), (i + 1) == ids.size() ? cbRet : null);
        }
    }

    private void sendGroupMessage(boolean bOnline, final String strGrpId, final byte[] data, final ILiveCallBack cbRet) {
        TIMConversation conversation = TIMManager.getInstance().getConversation(TIMConversationType.Group, strGrpId);
        TIMMessage msg = buildTIMMsg(data);
        TIMValueCallBack<TIMMessage> callBack = new TIMValueCallBack<TIMMessage>() {
            @Override
            public void onError(int errCode, String errMsg) {
                ILiveLog.ke(TAG, "sendGroupMessage", ILiveConstants.Module_IMSDK, errCode, errMsg, new ILiveLog.LogExts().put("groupId", strGrpId));
                notifyException(ILiveConstants.EXCEPTION_MESSAGE_EXCEPTION, errCode, errMsg);
                ILiveFunc.notifyError(cbRet, ILiveConstants.Module_IMSDK, errCode, errMsg);
            }

            @Override
            public void onSuccess(TIMMessage message) {
                ILiveLog.di(TAG, "sendGroupMessage->success", new ILiveLog.LogExts().put("groupId", strGrpId).put("msg", new String(data)));
                ILiveFunc.notifySuccess(cbRet, message);
            }
        };
        if (bOnline)
            conversation.sendOnlineMessage(msg, callBack);
        else
            conversation.sendMessage(msg, callBack);
    }

    // 发送来电消息
    private void sendIncomingCallMsg(byte[] data, IncomingInfo info, ILiveCallBack cbRet) {
        if (!TextUtils.isEmpty(info.getImGrpId()) && "Private".equals(info.getImGrpType())) {
            sendGroupMessage(false, info.getImGrpId(), data, cbRet);
        } else if (null != info.getListMembers() && info.getListMembers().size() > 0)
            sendUsersMessage(false, data, info.getListMembers(), cbRet);
        else
            sendC2CMessage(false, data, info.getUserId(), cbRet);
    }

    // 群发通话信令
    private void sendMembersMessage(boolean bOnline, byte[] data, HashMap<String, ILVCallMemberInfo> members) {
        for (Map.Entry<String, ILVCallMemberInfo> entry : members.entrySet()) {
            sendC2CMessage(bOnline, data, entry.getKey(), null);
        }
    }

    // 添加定时心跳
    private void initHeartBeat() {
        if (null != curInfo.getOption() && 0 != curInfo.getOption().getHearBeatInterval()) {
            ILiveLog.ki(TAG, "initHeartBeat", new ILiveLog.LogExts().put("hearbeat", curInfo.getOption().getHearBeatInterval()));
            mMainHandler.postDelayed(mHeartBeatRunable, curInfo.getOption().getHearBeatInterval() * 1000);
        }
    }

    private void removeHeartBeat() {
        ILiveLog.ki(TAG, "removeHeartBeat->enter");
        mMainHandler.removeCallbacks(mHeartBeatRunable);
    }

    // 生成内部信令
    private TIMMessage buildTIMMsg(byte[] data) {
        TIMMessage msg = new TIMMessage();
        TIMCustomElem elem = new TIMCustomElem();
        elem.setData(data);
        elem.setExt(ILVCallConstants.TCEXT_MAGIC.getBytes());
        msg.addElement(elem);

        return msg;
    }

    private String getProtoString(String strMsg){
        if (null != mConfig && mConfig.isPbProto()){
            return ILiveFunc.byte2HexStr(strMsg.getBytes());
        }else{
            return strMsg;
        }
    }

    // 发送群通话信令
    private void sendGroupMessage(boolean bOnline, final byte[] data, final ILiveCallBack cbRet) {
        final String strMsg = new String(data);
        if (null != mConfig.getMsgPipe()) {   // 使用自定义消息通道
            ILiveLog.ki(TAG, "sendGroupMessage->custom", new ILiveLog.LogExts().put("msg", getProtoString(strMsg)));
            mConfig.getMsgPipe().sendGroupMessage(ILiveRoomManager.getInstance().getIMGroupId(), strMsg);
            return;
        }

        //ILiveLog.i(TAG, "send group cmd : " + strMsg + "/" + ILiveRoomManager.getInstance().getIMGroupId());
        TIMMessage msg = buildTIMMsg(data);
        ILiveCallBack callBack = new ILiveCallBack<TIMMessage>() {
            @Override
            public void onSuccess(TIMMessage data) {
                ILiveLog.di(TAG, "sendGroupMessage->success", new ILiveLog.LogExts().put("msg", getProtoString(strMsg)));
                ILiveFunc.notifySuccess(cbRet, data);
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                ILiveLog.ke(TAG, "sendGroupMessage", module, errCode, errMsg);
                ILiveFunc.notifyError(cbRet, module, errCode, errMsg);
            }
        };
        if (bOnline) {
            ILiveRoomManager.getInstance().sendGroupOnlineMessage(msg, callBack);
        } else {
            ILiveRoomManager.getInstance().sendGroupMessage(msg, callBack);
        }
    }

    // 发送C2C视频通话信令
    private void sendC2CMessage(final boolean bOnline, byte[] data, final String sendId, final ILiveCallBack cbRet) {
        if (TextUtils.isEmpty(sendId) || sendId.equals(ILiveLoginManager.getInstance().getMyUserId())) {
            return;
        }

        final String strMsg = new String(data);
        if (null != mConfig.getMsgPipe()) {   // 使用自定义消息通道
            ILiveLog.di(TAG, "sendC2CMessage->custom", new ILiveLog.LogExts().put("dstId", sendId).put("msg", getProtoString(strMsg)));
            mConfig.getMsgPipe().sendC2CMessage(sendId, strMsg);
            return;
        }

        //ILiveLog.i(TAG, "send C2C cmd : " + strMsg + "/" + sendId);
        TIMMessage msg = buildTIMMsg(data);
        ILiveCallBack callBack = new ILiveCallBack<TIMMessage>() {
            @Override
            public void onSuccess(TIMMessage data) {
                ILiveLog.di(TAG, "sendC2CMessage", new ILiveLog.LogExts().put("online", bOnline).put("dstId", sendId).put("msg", getProtoString(strMsg)));
                ILiveFunc.notifySuccess(cbRet, data);
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                ILiveLog.ke(TAG, "sendC2CMessage", module, errCode, errMsg, new ILiveLog.LogExts()
                        .put("online", bOnline).put("dstId", sendId).put("msg", getProtoString(strMsg)));
                notifyException(ILiveConstants.EXCEPTION_MESSAGE_EXCEPTION, errCode, errMsg);
                ILiveFunc.notifyError(cbRet, module, errCode, errMsg);
            }
        };
        if (bOnline) {
            ILiveRoomManager.getInstance().sendC2COnlineMessage(sendId, msg, callBack);
        } else {
            ILiveRoomManager.getInstance().sendC2CMessage(sendId, msg, callBack);
        }
    }

    /**
     * 处理来电消息
     */
    private void processIncoming(int roomId, CallMsg callMsg){
        if (!TextUtils.isEmpty(callMsg.getUuid()) && overMap.containsKey(callMsg.getUuid())) {
            ILiveLog.kw(TAG, "processIncoming->ignore", new ILiveLog.LogExts().put("callId", roomId).put("uuid", callMsg.getUuid()));
            return;
        }

        if (ILiveRoomManager.getInstance().getRoomId() == roomId) {  // 当前通话
            if (null != callMsg.getTargets()) {
                for (String id : callMsg.getTargets()) {
                    updateMember(id, false, true);    // 添加成员
                }
            }
            ILiveLog.kw(TAG, "processIncoming->expired", new ILiveLog.LogExts().put("callId", roomId));
            return;
        }

        if (null != mIncomingMap.get(roomId)) {
            IncomingInfo info = mIncomingMap.get(roomId);
            if (null != callMsg.getTargets()) {
                for (String id : callMsg.getTargets()) {
                    if (!info.getListMembers().contains(id)) {    // 添加成员
                        info.getListMembers().add(id);
                    }
                }
            }
            ILiveLog.kw(TAG, "processIncoming->repeat", new ILiveLog.LogExts().put("callId", roomId));
            return;
        }

        if (!isMsgToMe(callMsg)) {
            ILiveLog.kw(TAG, "processIncoming->not-match");
            return;
        }

        ILVIncomingNotification notification = new ILVIncomingNotification(callMsg);

        final IncomingInfo info = new IncomingInfo(roomId, notification.getSender(), callMsg.getCallType());
        info.setUUID(notification.getUuid());
        info.setSponsor(notification.getSponsorId());
        info.setImGrpId(callMsg.getImGrpId());
        info.setImGrpType(callMsg.getImGrpType());
        info.setListMembers(callMsg.getMembers());
        info.getListOnlineMembers().add(notification.getSender());
        if (mConfig.isAutoBusy() && CallStatus.eCallIdle != mStatus) { // 占线
            ILiveCallBack cbRet = new ILiveCallBack() {
                @Override
                public void onSuccess(Object data) {
                    quitDiscuss(info);
                }

                @Override
                public void onError(String module, int errCode, String errMsg) {
                    quitDiscuss(info);
                }
            };
            if (null != callMsg.getMembers() && callMsg.getMembers().size() > 0) {
                sendUsersMessage(false, protoEngine.getLineBusyData(info), callMsg.getMembers(), cbRet);
            } else {
                sendC2CMessage(false, protoEngine.getLineBusyData(info), notification.getSender(), cbRet);
            }

            ILiveLog.kw(TAG, "processIncoming->busy", new ILiveLog.LogExts().put("status", mStatus));
        } else {
            mIncomingMap.put(roomId, info);
            ILiveLog.ki(TAG, "processIncoming", new ILiveLog.LogExts().put("sender", notification.getSender()).put("callId", roomId));
            notifyNewCall(roomId, callMsg.getCallType(), notification);
        }
    }

    /**
     * 处理电话结束
     */
    public void processCallEnd(int callId, ILVCallNotification notification, int errCode) throws JSONException {
        if (ILiveRoomManager.getInstance().getRoomId() == callId) {
            Log.i("--------------",notification.toString()+"-----------");
            // 先通知用户离开
            updateMember(notification.getSender(), false, false);
            removeMember(notification.getSender());
            if (0 == curInfo.getMemberMap().size()) {   // 该通话已没有其它成员了
                endCallEx(callId, errCode, "Remote cancel");
            } else {
                String users = null;
                for (Map.Entry<String, ILVCallMemberInfo> entry : curInfo.getMemberMap().entrySet()) {
                    ILVCallMemberInfo info = entry.getValue();
                    users = null == users ? "" + info.getId() : users + "," + info.getId();
                }
                ILiveLog.dd(TAG, "processCallEnd->left", new ILiveLog.LogExts().put("users", users));
            }
        } else if (null != mIncomingMap.get(callId)) {    // 取消来电
            IncomingInfo info = mIncomingMap.get(callId);
            info.getListMembers().remove(notification.getSender());
            if (info.getListMembers().size() < 1) {
                notifyCallEnd(callId, errCode, "remote cancel");
                mIncomingMap.remove(callId);
            }
        } else {
            if (ILVCallConstants.ERR_CALL_SPONSOR_CANCEL == errCode ||
                    ILVCallConstants.ERR_CALL_SPONSOR_TIMEOUT == errCode) {
                // 缓存会话
                if (!TextUtils.isEmpty(notification.getUuid()))
                    overMap.put(notification.getUuid(), notification);
            }
            ILiveLog.kw(TAG, "processCallEnd->not-exist", new ILiveLog.LogExts().put("callid", callId));
        }
    }


    /**
     * 处理自定义消息
     *
     * @param ele
     * @param msg
     */
    private boolean processCustomMsg(TIMCustomElem ele, TIMMessage msg) {
        try {
            String strExt = new String(ele.getExt(), "UTF-8");
            if (strExt.equals(ILVCallConstants.TCEXT_MAGIC) && !ILiveLoginManager.getInstance().getMyUserId().equals(msg.getSender())) {
                String senderId = msg.getSender();
                CallMsg callMsg = protoEngine.parseData(ele.getData(), senderId);
                return notifyMessage(callMsg);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            ILiveLog.kw(TAG, "processCustomMsg", new ILiveLog.LogExts().put("exception", ex.toString()));
        }
        return false;
    }

    /**
     * 解析消息回调
     *
     * @param message IM 消息
     * @return 如果该消息是内部消息并被处理，返回 true，否则返回 false
     */
    private boolean parseIMMessage(TIMMessage message) {
        boolean bInnerMsg = false;
        for (int i = 0; i < message.getElementCount(); i++) {
            TIMElem elem = message.getElement(i);
            if (null == elem) {
                continue;
            }

            TIMElemType type = elem.getType();
            ILiveLog.dd(TAG, "parseIMMessage", new ILiveLog.LogExts().put("type", type).put("from", message.getSender()));

            switch (type) {
                case GroupSystem:   // 系统消息
                    break;
                case Custom:        // 定制消息
                    bInnerMsg = bInnerMsg | processCustomMsg((TIMCustomElem) elem, message);
                    break;
                default:
                    break;
            }
        }
        return bInnerMsg;
    }

    // 不对外接口
    private int endCallEx(int callId, int reason, String message) {
        ILiveLog.ki(TAG, "endCallEx", new ILiveLog.LogExts().put("callId", callId).put("errCode", reason).put("ereMsg", message));
        if (ILiveRoomManager.getInstance().getRoomId() == callId
                || ILiveConstants.INVALID_INTETER_VALUE == ILiveRoomManager.getInstance().getRoomId()) {
            if (CallStatus.eCallEstablish == mStatus) {
                changeStatus(CallStatus.eCallEnd);
                // 退出发C2C消息，避免群组退出
                sendMembersMessage(false, protoEngine.getHangUpData(curInfo), curInfo.getMemberMap());
                if (ILVCallConstants.ERR_CALL_LOCAL_CANCEL == reason)
                    reason = ILVCallConstants.ERR_CALL_HANGUP;
                quitAVRoom(callId, reason, message);
            } else if (CallStatus.eCallIncoming == mStatus) {
                changeStatus(CallStatus.eCallEnd);
                changeStatus(CallStatus.eCallIdle);
                quitAVRoom(callId, reason, message);
            } else {
                if (CallStatus.eCallWait == mStatus) {  // 对方尚未接入
                    mMainHandler.removeCallbacks(mTimeoutRunable);
                    mTimeoutRunable = null;
                    changeStatus(CallStatus.eCallEnd);
                    if (ILVCallConstants.ERR_CALL_SPONSOR_TIMEOUT == reason) {
                        sendMembersMessage(false, protoEngine.getSponsorTimeOutData(curInfo), curInfo.getMemberMap());
                    } else {
                        if (ILVCallConstants.ERR_CALL_LOCAL_CANCEL == reason)
                            reason = ILVCallConstants.ERR_CALL_SPONSOR_CANCEL;
                        sendMembersMessage(false, protoEngine.getSponsorCancelData(curInfo), curInfo.getMemberMap());
                    }

                    quitAVRoom(callId, reason, message);
                }
                changeStatus(CallStatus.eCallEnd);
            }
        } else if (null != mIncomingMap.get(callId)) {   // 来电被取消
            if (ILVCallConstants.ERR_CALL_LOCAL_CANCEL == reason)
                reason = ILVCallConstants.ERR_CALL_SPONSOR_CANCEL;
            notifyCallEnd(callId, reason, "remote cancel");
            mIncomingMap.remove(callId);
        } else {
            ILiveLog.kw(TAG, "endCallEx->not-found", new ILiveLog.LogExts().put("callId", callId));
            return ILVCallConstants.ERR_CALL_NOT_EXIST;
        }

        return ILiveConstants.NO_ERR;
    }

    private void clearCallInfo() {
        mStatus = CallStatus.eCallIdle;
        curInfo.reset();
    }

    // 接口实现
    @Override
    public int init(ILVCallConfig config) {
        mConfig = config;
        config.messageListener(this);   // 拦截IM消息

        if (config.isPbProto()) {
            protoEngine = new PbCallProto();
        }else{
            protoEngine = new JsonCallProto();
        }
        clearCallInfo();

        ILiveLog.ki(TAG, "init", new ILiveLog.LogExts().put("version", getVersion()));
        return ILiveRoomManager.getInstance().init(config);
    }

    @Override
    public void shutdown() {
        ILiveRoomManager.getInstance().shutdown();
        mConfig = null;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public int addCallListener(ILVCallListener listener) {
        ILiveLog.dd(TAG, "addCallListener", new ILiveLog.LogExts().put("listener", listener.hashCode()));
        mListenerList.add(listener);
        return ILiveConstants.NO_ERR;
    }

    @Override
    public int removeCallListener(ILVCallListener listener) {
        ILiveLog.dd(TAG, "removeCallListener", new ILiveLog.LogExts().put("listener", listener.hashCode()));
        if (mListenerList.contains(listener)) {
            mListenerList.remove(listener);
        }
        return ILiveConstants.NO_ERR;
    }

    @Override
    public int addIncomingListener(ILVIncomingListener listener) {
        ILiveLog.dd(TAG, "addIncomingListener", new ILiveLog.LogExts().put("listener", listener.hashCode()));
        mIncomingListenrList.add(listener);
        return ILiveConstants.NO_ERR;
    }

    @Override
    public int removeIncomingListener(ILVIncomingListener listener) {
        ILiveLog.dd(TAG, "removeIncomingListener", new ILiveLog.LogExts().put("listener", listener.hashCode()));
        if (mIncomingListenrList.contains(listener)) {
            mIncomingListenrList.remove(listener);
        }
        return ILiveConstants.NO_ERR;
    }

    @Override
    public int makeMutiCall(final List<String> nums, ILVCallOption option, final ILiveCallBack callBack) {
        ILiveLog.ki(TAG, "makeMutiCall", new ILiveLog.LogExts().put("to", ILiveFunc.getListStr(nums)).put("type", option.getCallType()));
        if (!changeStatus(CallStatus.eCallEnter)) {
            return ILiveConstants.INVALID_INTETER_VALUE;
        }

        // 用户未指定房间号时生成随机房间号
        if (0 == option.getRoomId()) {
            option.setRoomId(ILiveFunc.generateAVCallRoomID());
        }
        final int roomId = option.getRoomId();

        bInviteSelf = false;
        curInfo.reset();
        // 拦截onEndPoint
        mAppStatusListener = option.getMemberStatusLisenter();
        curInfo.setSponsor(ILiveLoginManager.getInstance().getMyUserId())
                .setOption(option.setRoomMemberStatusLisenter(this));
        for (String number : nums) {
            updateMember(number, false, true);
        }
        // 创建AVRoom, 不需要创建IM聊天室
        if (null != mAvRootView) {
            ILiveRoomManager.getInstance().initAvRootView(mAvRootView);
        }

        ILiveRoomManager.getInstance().createRoom(roomId, option, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                ILiveLog.ki(TAG, "makeMutiCall->success", new ILiveLog.LogExts().put("callId", roomId));
                if (changeStatus(CallStatus.eCallWait)) {
                    curInfo.setUuid(generateUUID());
                    sendMutiCallInvite(nums, false);       // 邀请成员
                    initHeartBeat();                    // 启动呼叫方心跳定时器
                    helper.init(ILiveConstants.EVENT_MAKE_MULTICALL, 0, "");
                    helper.report();//数据采集
                    // 设置超时
                    mTimeoutRunable = new Runnable() {
                        @Override
                        public void run() {
                            ILiveLog.ki(TAG, "makeMutiCall->timeout", new ILiveLog.LogExts().put("callId", roomId));
                            endCallEx(roomId, ILVCallConstants.ERR_CALL_SPONSOR_TIMEOUT, "call timeout");
                        }
                    };
                    mMainHandler.postDelayed(mTimeoutRunable, mConfig.getTimeout());
                    ILiveFunc.notifySuccess(callBack, 0);
                } else {
                    quitAVRoom(roomId, ILiveConstants.ERR_WRONG_STATE, "make call failed");
                    ILiveFunc.notifyError(callBack, ILVCallConstants.Module_CallSDK, ILiveConstants.ERR_WRONG_STATE, "make call with status " + mStatus);
                }
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                helper.init(ILiveConstants.EVENT_MAKE_MULTICALL, errCode, errMsg);
                helper.report();//数据采集
                ILiveLog.ke(TAG, "makeMutiCall", module, errCode, errMsg, new ILiveLog.LogExts().put("callId", roomId));
                changeStatus(CallStatus.eCallEnd);
                changeStatus(CallStatus.eCallIdle);
                ILiveFunc.notifyError(callBack, module, errCode, errMsg);
                notifyCallEnd(roomId, ILVCallConstants.ERR_CALL_FAILED, errMsg);
            }
        });

        return roomId;
    }

    @Override
    public int makeCall(final String toUserId, final ILVCallOption option, final ILiveCallBack callBack) {
        ILiveLog.ki(TAG, "makeCall", new ILiveLog.LogExts().put("to", toUserId).put("type", option.getCallType()));
        if (!changeStatus(CallStatus.eCallEnter)) {
            return ILiveConstants.INVALID_INTETER_VALUE;
        }

        // 用户未指定房间号时生成随机房间号
        if (0 == option.getRoomId()) {
            option.setRoomId(ILiveFunc.generateAVCallRoomID());
        }
        final int roomId = option.getRoomId();
        bInviteSelf = false;

        // 发起者为自己
        curInfo.reset();
        // 拦截onEndPoint
        mAppStatusListener = option.getMemberStatusLisenter();
        mAppDisconnectListener = option.getRoomDisconnectListener();
        curInfo.setSponsor(ILiveLoginManager.getInstance().getMyUserId())
                .setOption(option.setRoomMemberStatusLisenter(this)
                        .roomDisconnectListener(this));
        updateMember(toUserId, false, false);
        // 创建AVRoom, 不需要创建IM聊天室
        if (null != mAvRootView) {
            ILiveRoomManager.getInstance().initAvRootView(mAvRootView);
        }

        int ret = ILiveRoomManager.getInstance().createRoom(roomId, option, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                ILiveLog.ki(TAG, "makeCall", new ILiveLog.LogExts().put("callId", roomId));
                if (changeStatus(CallStatus.eCallWait)) {
                    curInfo.setUuid(generateUUID());
                    sendMakeCallMsg(toUserId);       // 邀请成员
                    initHeartBeat();                    // 启动呼叫方心跳定时器
                    helper.init(ILiveConstants.EVENT_MAKE_CALL, 0, "");
                    helper.report();//数据采集
                    // 设置超时
                    mTimeoutRunable = new Runnable() {
                        @Override
                        public void run() {
                            ILiveLog.ki(TAG, "makeCall->timeout", new ILiveLog.LogExts().put("callId", roomId));
                            endCallEx(roomId, ILVCallConstants.ERR_CALL_SPONSOR_TIMEOUT, "call timeout");
                        }
                    };
                    mMainHandler.postDelayed(mTimeoutRunable, mConfig.getTimeout());
                    ILiveFunc.notifySuccess(callBack, 0);
                } else {
                    quitAVRoom(roomId, ILiveConstants.ERR_WRONG_STATE, "make call failed");
                    ILiveFunc.notifyError(callBack, ILVCallConstants.Module_CallSDK, ILiveConstants.ERR_WRONG_STATE, "make call with status " + mStatus);
                }
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                helper.init(ILiveConstants.EVENT_MAKE_CALL, errCode, errMsg);
                helper.report();//数据采集
                ILiveLog.ke(TAG, "makeCall", module, errCode, errMsg, new ILiveLog.LogExts().put("callId", roomId));
                changeStatus(CallStatus.eCallEnd);
                changeStatus(CallStatus.eCallIdle);
                ILiveFunc.notifyError(callBack, module, errCode, errMsg);
                notifyCallEnd(roomId, ILVCallConstants.ERR_CALL_FAILED, errMsg);
            }
        });
        if (ILiveConstants.NO_ERR != ret) {    // 进房间失败
            ILiveLog.ke(TAG, "makeCall", ILiveConstants.Module_ILIVESDK, ret, "enter room failed", new ILiveLog.LogExts().put("callId", roomId));
            if (ILiveConstants.ERR_ALREADY_IN_ROOM == ret) { // 上次通话未结束
                quitAVRoom(ILiveRoomManager.getInstance().getRoomId(), ILiveConstants.ERR_USER_CANCEL, "make new call");
            }
            changeStatus(CallStatus.eCallEnd);
            changeStatus(CallStatus.eCallIdle);
            notifyCallEnd(roomId, ILVCallConstants.ERR_CALL_FAILED, "createRoom failed");
            return ILiveConstants.INVALID_INTETER_VALUE;
        }

        return roomId;
    }

    @Override
    public int acceptCall(final int callId, ILVCallOption option) {
        ILiveLog.ki(TAG, "acceptCall", new ILiveLog.LogExts().put("callId", callId).put("type", option.getCallType()));
        final IncomingInfo info = mIncomingMap.get(callId);
        if (null == info) {
            ILiveLog.kw(TAG, "acceptCall->not-found", new ILiveLog.LogExts().put("callId", callId));
            return ILVCallConstants.ERR_CALL_NOT_EXIST;
        }

        if (!changeStatus(CallStatus.eCallIncoming)) {
            return ILiveConstants.ERR_WRONG_STATE;
        }

        curInfo.reset();
        // 拦截onEndPoint
        mAppStatusListener = option.getMemberStatusLisenter();
        // 更新发起者id
        curInfo.setSponsor(info.getSponsor())
                .setUuid(info.getUuid())
                .setOption(option.setRoomMemberStatusLisenter(this));
        option.setRoomId(callId);
        if (!TextUtils.isEmpty(info.getImGrpId()) && !TextUtils.isEmpty(info.getImGrpType())) {
            curInfo.getOption().imsupport(true);
            curInfo.getOption().imGroupId(info.getImGrpId());
            curInfo.getOption().groupType(info.getImGrpType());
        } else {
            curInfo.getOption().imsupport(false);
        }

        mIncomingMap.remove(callId);
        if (null != info.getListMembers()) {
            for (String member : info.getListMembers()) {
                updateMember(member, false, false);
            }
            for (String online : info.getListOnlineMembers()) {   // 更新来电的成员状态
                ILVCallMemberInfo memInfo = curInfo.getMemberMap().get(online);
                if (null != memInfo) {
                    memInfo.setJoin(true);
                }
            }
        }
        updateMember(info.getUserId(), true, true);
        if (null != mAvRootView) {
            ILiveRoomManager.getInstance().initAvRootView(mAvRootView);
        }

        ILiveRoomManager.getInstance().joinRoom(info.getCallId(), option, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                ILiveLog.ki(TAG, "acceptCall->success", new ILiveLog.LogExts().put("callId", callId));
                initHeartBeat();                    // 启动接听方心跳定时器
                helper.init(ILiveConstants.EVENT_ACCEPT_CALL, 0, "");
                helper.report();//数据采集
                if (changeStatus(CallStatus.eCallEstablish)) {
                    notifyCallEstablish(callId);
                    sendIncomingCallMsg(protoEngine.getAcceptData(info), info, null);
                } else {
                    quitAVRoom(callId, ILiveConstants.ERR_WRONG_STATE, "accept call failed");
                }
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                ILiveLog.ke(TAG, "acceptCall", module, errCode, errMsg, new ILiveLog.LogExts().put("callId", callId));
                helper.init(ILiveConstants.EVENT_ACCEPT_CALL, errCode, errMsg);
                helper.report();//数据采集
                quitAVRoom(callId, errCode, errMsg);
                notifyCallEnd(callId, errCode, errMsg);
            }
        });
        return ILiveConstants.NO_ERR;
    }

    @Override
    public int rejectCall(int callId) {
        ILiveLog.ki(TAG, "rejectCall", new ILiveLog.LogExts().put("callId", callId));
        final IncomingInfo info = mIncomingMap.get(callId);
        if (null == info) {
            ILiveLog.kw(TAG, "rejectCall->not-found", new ILiveLog.LogExts().put("callId", callId));
            return ILiveConstants.ERR_NO_ROOM;
        }

        helper.init(ILiveConstants.EVENT_REJECT_CALL, 0, "");
        helper.report();//数据采集
        mIncomingMap.remove(callId);
        sendIncomingCallMsg(protoEngine.getRejectData(info), info, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                quitDiscuss(info);
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                quitDiscuss(info);
            }
        });
        notifyCallEnd(callId, ILVCallConstants.ERR_CALL_RESPONDER_REFUSE, "local reject");
        return ILiveConstants.NO_ERR;
    }

    @Override
    public int responseLineBusy(int callId) {
        ILiveLog.ki(TAG, "responseLineBusy", new ILiveLog.LogExts().put("callId", callId));
        final IncomingInfo info = mIncomingMap.get(callId);
        if (null == info) {
            ILiveLog.kw(TAG, "responseLineBusy->not-found", new ILiveLog.LogExts().put("callId", callId));
            return ILiveConstants.ERR_NO_ROOM;
        }

        helper.init(ILiveConstants.EVENT_REJECT_CALL, 0, "");
        helper.report();//数据采集
        mIncomingMap.remove(callId);
        sendIncomingCallMsg(protoEngine.getLineBusyData(info), info, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                quitDiscuss(info);
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                quitDiscuss(info);
            }
        });
        notifyCallEnd(callId, ILVCallConstants.ERR_CALL_RESPONDER_LINEBUSY, "local busy");
        return ILiveConstants.NO_ERR;
    }

    @Override
    public int endCall(int callId) {
        return endCallEx(callId, ILVCallConstants.ERR_CALL_LOCAL_CANCEL, "User Cancel");
    }

    @Override
    public int inviteUser(int callId, List<String> users) {
        if (ILiveRoomManager.getInstance().getRoomId() == callId && null != curInfo.getOption()) {
            if (null != users) {
                for (String id : users) {
                    updateMember(id, false, false);
                }
            }
            return sendMutiCallInvite(users, true);
        } else {
            ILiveLog.kw(TAG, "inviteUser->not-match", new ILiveLog.LogExts().put("callId", callId).put("curCallId", ILiveRoomManager.getInstance().getRoomId()));
            return ILVCallConstants.ERR_CALL_NOT_EXIST;
        }
    }

    private void invitePrivateMember(List<String> ids) {
        if (!bInviteSelf) {
            ids.add(ILiveLoginManager.getInstance().getMyUserId());
            bInviteSelf = true;
        }
        TIMGroupManager.getInstance().inviteGroupMember(curInfo.getOption().getIMGroupId(), ids, new TIMValueCallBack<List<TIMGroupMemberResult>>() {
            @Override
            public void onError(int errCode, String errMsg) {
                ILiveLog.ke(TAG, "invitePrivateMember", ILiveConstants.Module_IMSDK, errCode, errMsg);
            }

            @Override
            public void onSuccess(List<TIMGroupMemberResult> timGroupMemberResults) {
                boolean bSuccess = true;
                if (null != timGroupMemberResults) {
                    for (TIMGroupMemberResult ret : timGroupMemberResults) {
                        if (0 == ret.getResult()) {
                            bSuccess = false;
                            ILiveLog.kw(TAG, "invitePrivateMember->fail", new ILiveLog.LogExts().put("id", ret.getUser()));
                        }
                    }
                }
                if (bSuccess) {
                    // 发送心跳激活讨论组
                    sendGroupMessage(true, protoEngine.getHeartBeatData(curInfo), null);
                    ILiveLog.di(TAG, "invitePrivateMember->success");
                }
            }
        });
    }

    // 检测是否应该退出讨论组
    private void quitDiscuss(IncomingInfo info) {
        if (!TextUtils.isEmpty(info.getImGrpId()) && "Private".equals(info.getImGrpType())) {   // 退出讨论组
            ILiveLog.ki(TAG, "quitDiscuss", new ILiveLog.LogExts().put("groupId", info.getImGrpId()));
            TIMGroupManager.getInstance().quitGroup(info.getImGrpId(), new TIMCallBack() {
                @Override
                public void onError(int errCode, String errMsg) {
                    ILiveLog.de(TAG, "quitDiscuss", ILiveConstants.Module_IMSDK, errCode, errMsg);
                }

                @Override
                public void onSuccess() {
                    ILiveLog.dd(TAG, "quitDiscuss->success");
                }
            });
        }
    }

    /**
     * 发送多人视频邀请
     */
    private int sendMutiCallInvite(List<String> nums, boolean bInvite) {
        byte[] data = bInvite ? protoEngine.getInviteData(nums, curInfo) : protoEngine.getDailingData(nums, curInfo);
        if (null != curInfo.getOption() && curInfo.getOption().isIMSupport()) {
            //sendGroupMessage(mOption.isOnlineCall(), inviteStr, null);
            if (curInfo.getOption().getGroupType().equals("Private")) {  // 讨论组需要拉人
                List<String> listIds = new ArrayList<>();
                for (String id : nums)
                    listIds.add(id);
                invitePrivateMember(listIds);
            }
        } else if (bInvite) {
            sendMembersMessage(true, data, curInfo.getMemberMap());
        }
        ILiveLog.ki(TAG, "sendMutiCallInvite", new ILiveLog.LogExts().put("users", ILiveFunc.getListStr(nums)));
        sendUsersMessage(curInfo.getOption().isOnlineCall(), data, nums, null);
        return 0;
    }

    /**
     * 发送双人视频邀请
     **/
    private int sendMakeCallMsg(String userId) {
        if (null != curInfo.getOption() && curInfo.getOption().isIMSupport() && curInfo.getOption().getGroupType().equals("Private")) {  // 讨论组需要拉人
            List<String> listIds = new ArrayList<>();
            listIds.add(userId);
            invitePrivateMember(listIds);
        }
        ILiveLog.ki(TAG, "sendMakeCallMsg", new ILiveLog.LogExts().put("user", userId));
        sendC2CMessage(curInfo.getOption().isOnlineCall(), protoEngine.getDailingData(userId, curInfo), userId, null);
        return 0;
    }

    @Override
    public int initAvView(AVRootView view) {
        mAvRootView = view;
        ILiveRoomManager.getInstance().initAvRootView(view);

        return ILiveConstants.NO_ERR;
    }

    @Override
    public int enableMic(boolean bEnable) {
        int iRet = ILiveRoomManager.getInstance().enableMic(bEnable);
        return iRet;
    }

    @Override
    public int enableSpeaker(boolean bEnable) {
        int iRet = ILiveRoomManager.getInstance().enableSpeaker(bEnable);
        return iRet;
    }

    @Override
    public int enableCamera(int cameraId, boolean bEnable) {
        int iRet = ILiveRoomManager.getInstance().enableCamera(cameraId, bEnable);
        return iRet;
    }

    @Override
    public int getCurCameraId() {
        return ILiveRoomManager.getInstance().getCurCameraId();
    }

    @Override
    public int switchCamera(int cameraId) {
        return ILiveRoomManager.getInstance().switchCamera(cameraId);
    }

    @Override
    public int sendC2CMessage(String dstUser, TIMMessage message, ILiveCallBack<TIMMessage> callBack) {
        return ILiveRoomManager.getInstance().sendC2CMessage(dstUser, message, callBack);
    }

    @Override
    public int sendGroupMessage(TIMMessage message, ILiveCallBack<TIMMessage> callBack) {
        return ILiveRoomManager.getInstance().sendGroupMessage(message, callBack);
    }

    @Override
    public int sendC2COnlineMessage(String dstUser, TIMMessage message, ILiveCallBack<TIMMessage> callBack) {
        return ILiveRoomManager.getInstance().sendC2COnlineMessage(dstUser, message, callBack);
    }

    @Override
    public int sendGroupOnlineMessage(TIMMessage message, ILiveCallBack<TIMMessage> callBack) {
        return ILiveRoomManager.getInstance().sendGroupOnlineMessage(message, callBack);
    }

    @Override
    public int enableBeauty(float value) {
        return ILiveRoomManager.getInstance().enableBeauty(value);
    }

    @Override
    public int enableWhite(float value) {
        return ILiveRoomManager.getInstance().enableWhite(value);
    }

    @Override
    public void onPause() {
        ILiveRoomManager.getInstance().onPause();
    }

    @Override
    public void onResume() {
        ILiveRoomManager.getInstance().onResume();
    }

    @Override
    public void onDestory() {
        if (CallStatus.eCallIdle == mStatus) {
            // 重置call状态
            clearCallInfo();
            ILiveRoomManager.getInstance().onDestory();
        } else {
            endCall(ILiveRoomManager.getInstance().getRoomId());
            clearCallInfo();
        }
        mAvRootView = null;
    }

    private void notifyCustomMessage(int notificationId, ILVCallNotification notification) {
        if (null == mConfig || null == mConfig.getNotificationListener()) {
            ILiveLog.kw(TAG, "notifyCustomMessage->no-listener");
            return;
        }

        mConfig.getNotificationListener().onRecvNotification(notificationId, notification);
    }

    @Override
    public int postNotification(int callId, ILVCallNotification notification) {
        if (callId == ILiveRoomManager.getInstance().getRoomId()) {  // 当前房间
            if (curInfo.getOption().isIMSupport()) {
                sendGroupMessage(false, protoEngine.getNotificationData(notification, curInfo), null);
            } else {
                sendMembersMessage(false, protoEngine.getNotificationData(notification, curInfo), curInfo.getMemberMap());
            }
        } else if (null != mIncomingMap.get(callId)) {
            IncomingInfo info = mIncomingMap.get(callId);
            sendIncomingCallMsg(protoEngine.getNotificationData(notification, info), info, null);
        }
        return ILiveConstants.NO_ERR;
    }

    private boolean isMsgToMe(ILVCallNotification notification) {
        return 0 == notification.getTargets().size() || notification.getTargets().contains(ILiveLoginManager.getInstance().getMyUserId());
    }

    @Override
    public boolean notifyMessage(String strMsg, String senderId, long timeStamp) {
        CallMsg callMsg = protoEngine.parseData(strMsg.getBytes(), senderId);
        if (mConfig.isServerTimeStamp()){
            callMsg.setTimeStamp(timeStamp);
        }
        return notifyMessage(callMsg);
    }

    @Override
    public boolean notifyMessage(CallMsg msg) {
        try {
            ILiveLog.di(TAG, "notifyMessage", new ILiveLog.LogExts().put("cmd", "0x" + Integer.toHexString(msg.getNotifId()))
                    .put("sender", msg.getSender()));

            switch (msg.getNotifId()) {
                case ILVCallConstants.TCILiveCMD_Inviting:
                case ILVCallConstants.TCILiveCMD_Dialing:       //  新来电
                    processIncoming(msg.getCallId(), msg);
                    break;
                case ILVCallConstants.TCILiveCMD_LineBusy:      // 占线
                    ILiveLog.ki(TAG, "notifyMessage->LineBusy", new ILiveLog.LogExts().put("sender", msg.getSender()));
                    mMainHandler.removeCallbacks(mTimeoutRunable);
                    mTimeoutRunable = null;
                    processCallEnd(msg.getCallId(), msg, ILVCallConstants.ERR_CALL_RESPONDER_LINEBUSY);
                    break;
                case ILVCallConstants.TCILiveCMD_Hangup:  // 挂断
                    ILiveLog.ki(TAG, "notifyMessage->HangUp", new ILiveLog.LogExts().put("sender", msg.getSender()));
                    processCallEnd(msg.getCallId(), msg, ILVCallConstants.ERR_CALL_HANGUP);
                    break;
                case ILVCallConstants.TCILiveCMD_SponsorCancel:
                    processCallEnd(msg.getCallId(), msg, ILVCallConstants.ERR_CALL_SPONSOR_CANCEL);
                    break;
                case ILVCallConstants.TCILiveCMD_SponsorTimeout:
                    processCallEnd(msg.getCallId(), msg, ILVCallConstants.ERR_CALL_SPONSOR_TIMEOUT);
                    break;
                case ILVCallConstants.TCILiveCMD_Accepted:     // 接听
                    if (isMsgToMe(msg)) {
                        // 是发给自己的
                        if (msg.getCallId() == ILiveRoomManager.getInstance().getRoomId()) {
                            ILiveLog.ki(TAG, "notifyMessage->Accept", new ILiveLog.LogExts().put("sender", msg.getSender()));
                            mMainHandler.removeCallbacks(mTimeoutRunable);
                            mTimeoutRunable = null;
                            updateMember(msg.getSender(), true, true);
                            if (changeStatus(CallStatus.eCallEstablish)) {
                                notifyCallEstablish(ILiveRoomManager.getInstance().getRoomId());
                            }
                        } else {
                            // 底层状态不匹配
                            if (ILiveConstants.INVALID_INTETER_VALUE == ILiveRoomManager.getInstance().getRoomId()) {
                                // 底层已无房间状态
                                endCallEx(msg.getCallId(), ILiveConstants.ERR_SDK_FAILED, "no room found");
                            }
                        }
                    } else {  // 仅更新成员状态
                        if (!mConfig.isMemberStatusFix()) {
                            if (msg.getCallId() == ILiveRoomManager.getInstance().getRoomId()) {
                                updateMember(msg.getSender(), true, true);
                            } else if (null != mIncomingMap.get(msg.getCallId())) {
                                IncomingInfo info = mIncomingMap.get(msg.getCallId());
                                info.getListOnlineMembers().add(msg.getSender());
                            }
                        }
                    }
                    break;
                case ILVCallConstants.TCILiveCMD_Reject:        // 拒接
                    if (msg.getCallId() == ILiveRoomManager.getInstance().getRoomId()) {
                        ILiveLog.ki(TAG, "notifyMessage->Reject", new ILiveLog.LogExts().put("sender", msg.getSender()));
                        mMainHandler.removeCallbacks(mTimeoutRunable);
                        mTimeoutRunable = null;
                        processCallEnd(msg.getCallId(), msg, ILVCallConstants.ERR_CALL_RESPONDER_REFUSE);
                    } else {
                        // 底层状态不匹配
                        if (ILiveConstants.INVALID_INTETER_VALUE == ILiveRoomManager.getInstance().getRoomId()) {
                            // 底层已无房间状态
                            endCallEx(msg.getCallId(), ILiveConstants.ERR_SDK_FAILED, "no room found");
                        }
                    }
                    break;
                case ILVCallConstants.TCILiveCMD_HeartBeat:     // 心跳
                    if (isMsgToMe(msg) && msg.getCallId() == ILiveRoomManager.getInstance().getRoomId()) {
                        // 是发给自己的
                        updateMember(msg.getSender(), true, true);
                        if (CallStatus.eCallWait == mStatus) {   // 正在等待用户加入
                            if (changeStatus(CallStatus.eCallEstablish)) {
                                ILiveLog.ki(TAG, "notifyMessage->HeartBeat-Accept", new ILiveLog.LogExts().put("sender", msg.getSender()));
                                mMainHandler.removeCallbacks(mTimeoutRunable);
                                mTimeoutRunable = null;
                                notifyCallEstablish(ILiveRoomManager.getInstance().getRoomId());
                            }
                        }
                    }
                    break;
                default:
                    if (msg.getNotifId() >= ILVCallConstants.TCILiveCMD_CustomBegin && msg.getNotifId() <= ILVCallConstants.TCILiveCMD_CustomEnd) {
                        notifyCustomMessage(msg.getCallId(), msg);
                        return true;
                    }
                    return false;
            }

            // 通知上层事件
            notifyCustomMessage(msg.getCallId(), msg);
        } catch (Exception e) {
            ILiveLog.kw(TAG, "notifyMessage", new ILiveLog.LogExts().put("exception", ILiveFunc.getExceptionInfo(e)));
            return false;
        }

        return true;
    }

    public String getSponsorId() {
        return curInfo.getSponsor();
    }

    @Override
    public List<ILVCallMemberInfo> getMembers() {
        List<ILVCallMemberInfo> membsers = new ArrayList<>();
        for (Map.Entry<String, ILVCallMemberInfo> entry : curInfo.getMemberMap().entrySet()) {
            membsers.add(entry.getValue());
        }
        return membsers;
    }

    @Override
    public boolean onNewMessages(List<TIMMessage> list) {
        List<TIMMessage> unhandledMessages = new ArrayList<>();
        // 从旧到新处理消息
        for (int i = list.size() - 1; i >= 0; i--) {
            TIMMessage currMsg = list.get(i);
            if (!parseIMMessage(currMsg)) {
                unhandledMessages.add(currMsg);
            }
        }
        return mConfig.getMessageListener() != null &&
                !unhandledMessages.isEmpty() &&
                mConfig.getMessageListener().onNewMessages(unhandledMessages);
    }

    @Override
    public boolean onEndpointsUpdateInfo(int eventid, String[] updateList) {
        if (null == updateList) {
            return false;
        }
        switch (eventid) {
            case ILiveConstants.TYPE_MEMBER_CHANGE_IN:
                for (String id : updateList) {
                    updateMember(id, true, false);
                }
                break;
            case ILiveConstants.TYPE_MEMBER_CHANGE_OUT:
                for (String id : updateList) {
                    updateMember(id, false, false);
                }
                break;
            case ILiveConstants.TYPE_MEMBER_CHANGE_HAS_CAMERA_VIDEO:
                for (String id : updateList) {
                    updateMember(id, true, false);
                    ILVCallMemberInfo info = findMember(id);
                    if (null != info) {
                        info.setCameraEnable(true);
                    }
                    if (null != curInfo.getOption().getMemberListener()) {
                        curInfo.getOption().getMemberListener().onCameraEvent(id, true);
                    }
                }
                break;
            case ILiveConstants.TYPE_MEMBER_CHANGE_NO_CAMERA_VIDEO:
                for (String id : updateList) {
                    ILVCallMemberInfo info = findMember(id);
                    if (null != info) {
                        info.setCameraEnable(false);
                    }
                    if (null != curInfo.getOption().getMemberListener()) {
                        curInfo.getOption().getMemberListener().onCameraEvent(id, false);
                    }
                }
                break;
            case ILiveConstants.TYPE_MEMBER_CHANGE_HAS_AUDIO:
                for (String id : updateList) {
                    updateMember(id, true, false);
                    ILVCallMemberInfo info = findMember(id);
                    if (null != info) {
                        info.setMicEnable(true);
                    }
                    if (null != curInfo.getOption().getMemberListener()) {
                        curInfo.getOption().getMemberListener().onMicEvent(id, true);
                    }
                }
                break;
            case ILiveConstants.TYPE_MEMBER_CHANGE_NO_AUDIO:
                for (String id : updateList) {
                    //updateMember(id, true);
                    ILVCallMemberInfo info = findMember(id);
                    if (null != info) {
                        info.setMicEnable(false);
                    }
                    if (null != curInfo.getOption().getMemberListener()) {
                        curInfo.getOption().getMemberListener().onMicEvent(id, false);
                    }
                }
                break;
        }

        if (null != mAppStatusListener) {    // 通知上层
            return mAppStatusListener.onEndpointsUpdateInfo(eventid, updateList);
        } else {
            return false;
        }
    }

    @Override
    public void onRoomDisconnect(int errCode, String errMsg) {
        changeStatus(CallStatus.eCallEnd);
        changeStatus(CallStatus.eCallIdle);
        notifyCallEnd(ILiveRoomManager.getInstance().getRoomId(), ILVCallConstants.ERR_CALL_DISCONNECT, errCode + "|" + errMsg);
        curInfo.reset();

        if (null != mAppDisconnectListener) {
            mAppDisconnectListener.onRoomDisconnect(errCode, errMsg);
        }
    }
}
