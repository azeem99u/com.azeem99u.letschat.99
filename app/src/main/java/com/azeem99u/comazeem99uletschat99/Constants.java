package com.azeem99u.comazeem99uletschat99;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class Constants {


    public static final String FLAG= "flag";
    public static final String DATA_PATH= "DATA_path";
    public static final String USERS= "users";
    public static final String LOCATION_KEY = "location_key";

    public static final String MESSAGE_DELIVERY= "message_delivery";
    public static final String DELIVERY= "delivery";
    public static final String USER_STATUS= "userStatus";
    public static final String MESSAGES= "messages";
    public static final String ACTION_PERSONAL_MESSAGE= "messages_p_m";
    public static final String ACTION_CALL = "action_call_m";
    public static final String ACTION_REQUEST= "action_request_r";
    public static final String ACTION_REQUEST_EXTRA= "action_request_E";
    public static final String REQUESTS= "requests";
    public static final String SELECTED_USER_ID_KEY = "selectedUserIdKey";

    public static final String GROUPS = "groups";
    public static final String GROUPS_NAME_KEY = "group_name";
    public static final String URL_KEY = "URL_key";
    public static final String FILE_PATH_KEY = "FILE_PATH_key";
    public static final String FILE_NAME_KEY= "NAME_PATH_key";
    public static final String SEND_IMAGE_DATA_KEY = "SendImageData";
    public static final int IMAGE_RESULT_CODE = 101;
    public static final int IMAGE_REQUEST_CODE = 100;
    public static final String IMAGE_EXTRA_KEY = "CROP";
    public static final String PROFILE_INTENT_EXTRA ="PROFILE_INTENT_EXTrA" ;

    public static final String REQUEST_TYPE= "request_type";
    public static final String SENT = "sent";
    public static final String RECEIVED = "receive";


    public static final String CONTACTS= "contacts";
    public static final String CONTACT= "contact";
    public static final String SAVE = "save";
    public static final String REMOVE = "remove";


    public static final String SELECTED_USER_ID_EXTRA_KEY = "USER_ID_EXTRA_KEY_";
    public static final String SELECTED_USER_ID_NOTIFICATION = "USER_ID_EXTRA_KEY_NOTIFICATION";
    public static final String USER_NAME_EXTRA_KEY = "USER_NAME_EXTRA_KEY_";
    public static final String USER_STATUS_EXTRA_KEY = "USER_STATUS_EXTRA_KEY_";
    public static final String USER_IMAGE_EXTRA_KEY = "USER_IMAGE_EXTRA_KEY_";
    public static final int PICK_FILE_REQUEST_CODE = 10011;
    public static final String RESPONSE_BODY = "RESP_B";

    public static final String PROGRESS_DOWNLOAD = "PROGRESS_D";
    public static final String PROGRESS_UPLOAD = "PROGRESS_U";
    public static final int NOTIFICATION_ID_DOWNLOAD = 1111;
    public static final int NOTIFICATION_ID_UPLOAD = 2222;
    public static final String FILE_SIZE_KEY = "filesize";
    public static final String ADAPTER_POSITION = "ADAPTER_PO";
    public static final String ADAPTER_POSITION_U = "ADAPTER_PO_U";

    //notification
    public static final  String RECEIVER_ID = "receiver_id";
    public static final  String  TITLE = "title";
    public static final  String BODY =  "body";
    public static final  String CLICK_ACTION = "click_action";
    public static final  String CHANNEL_ID  = "channel_id";
    public static final  String TRACKER = "ticker";
    public static final  String IMAGE =  "image";
    public static final  String MESSAGE_KEY =  "message_key";
    public static final  String SENDER_ID = "sender_id";


    public static final  String RECEIVER_ID_KEY = "receiver_id_key";
    public static final  String  TITLE_KEY = "title_key";
    public static final  String BODY_KEY =  "body_key";
    public static final  String CLICK_ACTION_KEY = "click_action";
    public static final  String CHANNEL_ID_KEY  = "channel_id_key";
    public static final  String IMAGE_KEY =  "image_key";
    public static final  String MESSAGE_KEY_W =  "message_key_w";
    public static final  String SENDER_ID_KEY = "sender_id_key";
    public static final String CALL_SENDER_ID = "call_sender_id";
    public static final String CALL_RECEIVER_ID = "call_receiver_id";
    public static final String CALL_ID = "call_id";
    public static final String CALLER_IMAGE_KEY = "CALLER_IMAGE_KEY_";


    public enum Delivery{
       Loading,
        Sent,
        Delivered,
        Seen
    }

    public static final  String MESSAGE_POJO_S_KEY =  "MESSAGE_POJO_S";
    public static final  String HOLDER_KEY =  "HOLDER_KEY";

    public static final String VIDEO_CALLS= "videoCalls";
    public static final String IS_BUSY= "is_busy";

    public static final String CONNECTION_KEY = "connection_key";
    public static final String CONNECTION_KEY_NOTIFICATION = "connection_key_NOTIF";



    public static final String CALL_CONNECTION = "connection_key";

    public enum CallConnection{
        Accepted,
        Rejected,
        Failed,
        End,
        Ringing,
        END_ONGOING,
        NoAnswer,
    }


    public static final String CALL_TYPE = "call_type";
    public static final  String  CALL_TYPE_KEY = "CALL_type_key";
    public static final  String  CALLER_NAME_KEY = "CALL_NAME_KEY";
    public static final  String  VOICE_CALL = "Voice Call";
    public static final  String  VIDEO_CALL = "Video Call";

    public static final String CALL_SENDER_KEY_FOR_CONN = "call_sender_key";
    public static final String CALL_TYPE_KEY_FOR_CONN = "CALL_TYPE_KEY";



    public static final String CONNECTION_STATUS = "connection_status";
    public static final int CALL_NOTIFICATION_ID = 12;

    public static final String HANG_UP_BROADCAST_ACTION = "ACTION";


    public static final String CALL_STATUS = "call_status";
    public static final String CALLING = "calling";
    public static final String MISSED_CALLING = "missedCall";

    public static String getTime(Long aLong) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm aaa");
        return currentTimeFormat.format(aLong);
    }

    public static String getTime() {
        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm aaa");
        currentTimeFormat.setTimeZone(TimeZone.getDefault());
        return currentTimeFormat.format(calendar.getTime());
    }

}

