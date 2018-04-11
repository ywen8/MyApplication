package com.tencent.callsdk.adapter.Pb_Impl;

import com.tencent.mobileqq.pb.ByteStringMicro;
import com.tencent.mobileqq.pb.MessageMicro;
import com.tencent.mobileqq.pb.PBBytesField;
import com.tencent.mobileqq.pb.PBEnumField;
import com.tencent.mobileqq.pb.PBField;
import com.tencent.mobileqq.pb.PBRepeatField;
import com.tencent.mobileqq.pb.PBStringField;
import com.tencent.mobileqq.pb.PBUInt32Field;

public final class mwcall_cmd
{
    public static final int CMD_DAILING = 1;
    public static final int CMD_CANCEL = 2;
    public static final int CMD_ACCEPT = 3;
    public static final int CMD_REFUSE = 4;
    public static final int CMD_HANGUP = 5;
    public static final int CMD_LINEBUSY = 6;
    public static final int CMD_SPONSOR_TIMEOUT = 7;
    public static final int CMD_INVITE = 8;
    public static final int CMD_NOTIFICATION = 9;
    public static final int CMD_HEARTBEAT = 153;

    public static final class SubCmd_0x1
            extends MessageMicro<SubCmd_0x1>
    {
        static final MessageMicro.FieldMap __fieldMap__ = MessageMicro.initFieldMap(new int[] { 8, 18, 26, 32, 40, 50, 58 }, new String[] { "uint32_av_type", "str_invite_tip", "bytes_custom_data", "uint32_call_type", "uint32_room_id", "str_sponsor_id", "str_member_id" }, new Object[] { Integer.valueOf(0), "", ByteStringMicro.EMPTY, Integer.valueOf(0), Integer.valueOf(0), "", "" }, SubCmd_0x1.class);
        public static final int UINT32_AV_TYPE_FIELD_NUMBER = 1;
        public final PBUInt32Field uint32_av_type;
        public static final int STR_INVITE_TIP_FIELD_NUMBER = 2;
        public final PBStringField str_invite_tip;
        public static final int BYTES_CUSTOM_DATA_FIELD_NUMBER = 3;
        public final PBBytesField bytes_custom_data;
        public static final int UINT32_CALL_TYPE_FIELD_NUMBER = 4;
        public final PBUInt32Field uint32_call_type;
        public static final int UINT32_ROOM_ID_FIELD_NUMBER = 5;
        public final PBUInt32Field uint32_room_id;
        public static final int STR_SPONSOR_ID_FIELD_NUMBER = 6;
        public final PBStringField str_sponsor_id;
        public static final int STR_MEMBER_ID_FIELD_NUMBER = 7;

        public SubCmd_0x1()
        {
            this.uint32_av_type = PBField.initUInt32(0);

            this.str_invite_tip = PBField.initString("");

            this.bytes_custom_data = PBField.initBytes(ByteStringMicro.EMPTY);

            this.uint32_call_type = PBField.initUInt32(0);

            this.uint32_room_id = PBField.initUInt32(0);

            this.str_sponsor_id = PBField.initString("");
        }

        public final PBRepeatField<String> str_member_id = PBField.initRepeat(PBStringField.__repeatHelper__);
    }

    public static final class SubCmd_0x3
            extends MessageMicro<SubCmd_0x3>
    {
        static final MessageMicro.FieldMap __fieldMap__ = MessageMicro.initFieldMap(new int[] { 8, 24 }, new String[] { "uint32_av_type", "uint32_accept_call_type" }, new Object[] { Integer.valueOf(0), Integer.valueOf(0) }, SubCmd_0x3.class);
        public static final int UINT32_AV_TYPE_FIELD_NUMBER = 1;
        public final PBUInt32Field uint32_av_type = PBField.initUInt32(0);
        public static final int UINT32_ACCEPT_CALL_TYPE_FIELD_NUMBER = 3;
        public final PBUInt32Field uint32_accept_call_type = PBField.initUInt32(0);
    }

    public static final class SubCmd_0x9
            extends MessageMicro<SubCmd_0x9>
    {
        static final MessageMicro.FieldMap __fieldMap__ = MessageMicro.initFieldMap(new int[] { 8, 18, 26 }, new String[] { "uint32_notification_id", "str_custom_desc", "str_custom_info" }, new Object[] { Integer.valueOf(0), "", "" }, SubCmd_0x9.class);
        public static final int UINT32_NOTIFICATION_ID_FIELD_NUMBER = 1;
        public final PBUInt32Field uint32_notification_id = PBField.initUInt32(0);
        public static final int STR_CUSTOM_DESC_FIELD_NUMBER = 2;
        public final PBStringField str_custom_desc = PBField.initString("");
        public static final int STR_CUSTOM_INFO_FIELD_NUMBER = 3;
        public final PBStringField str_custom_info = PBField.initString("");
    }

    public static final class CallCmd
            extends MessageMicro<CallCmd>
    {
        static final MessageMicro.FieldMap __fieldMap__ = MessageMicro.initFieldMap(new int[] { 8, 16, 26, 34, 42, 90, 106, 146 }, new String[] { "enum_cmd_type", "uint32_time", "str_cmd_user_id", "str_session_id", "str_target_id", "cmd_0x1", "cmd_0x3", "cmd_0x9" }, new Object[] { Integer.valueOf(1), Integer.valueOf(0), "", "", "", null, null, null }, CallCmd.class);
        public static final int ENUM_CMD_TYPE_FIELD_NUMBER = 1;
        public final PBEnumField enum_cmd_type = PBField.initEnum(1);
        public static final int UINT32_TIME_FIELD_NUMBER = 2;
        public final PBUInt32Field uint32_time = PBField.initUInt32(0);
        public static final int STR_CMD_USER_ID_FIELD_NUMBER = 3;
        public final PBStringField str_cmd_user_id = PBField.initString("");
        public static final int STR_SESSION_ID_FIELD_NUMBER = 4;
        public final PBStringField str_session_id = PBField.initString("");
        public static final int STR_TARGET_ID_FIELD_NUMBER = 5;
        public final PBRepeatField<String> str_target_id = PBField.initRepeat(PBStringField.__repeatHelper__);
        public static final int CMD_0X1_FIELD_NUMBER = 11;
        public mwcall_cmd.SubCmd_0x1 cmd_0x1 = new mwcall_cmd.SubCmd_0x1();
        public static final int CMD_0X3_FIELD_NUMBER = 13;
        public mwcall_cmd.SubCmd_0x3 cmd_0x3 = new mwcall_cmd.SubCmd_0x3();
        public static final int CMD_0X9_FIELD_NUMBER = 18;
        public mwcall_cmd.SubCmd_0x9 cmd_0x9 = new mwcall_cmd.SubCmd_0x9();
    }
}
