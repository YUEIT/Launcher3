package com.wos.launcher3;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.util.Log;
/*
 * add file by luobiao@wind-mobi.com 2015-8-14
 */
public class WOSContentObserver {

    private ContentObserver mMmsContentObserver;
    private ContentObserver mCallLogContentObserver;
    private Context mContext;

    public WOSContentObserver(Context context) {
        mContext = context;
    }
    protected void initContent(){
        Handler mHandler = new Handler();
        ContentResolver cr = mContext.getContentResolver();
        mMmsContentObserver = new MmsContentObserver(mHandler);
        mCallLogContentObserver = new CallLogContentObserver(mHandler);

        cr.registerContentObserver(CONTENT_URI_SMS, true, mMmsContentObserver);
        cr.registerContentObserver(CONTENT_URI_MMS, true, mMmsContentObserver);
        cr.registerContentObserver(CONTENT_URI_MMS_SMS, true, mMmsContentObserver);
        cr.registerContentObserver(CallLog.Calls.CONTENT_URI, true,mCallLogContentObserver);
    }
    void unregiestContent(){
        ContentResolver cr = mContext.getContentResolver();
        cr.unregisterContentObserver(mMmsContentObserver);
        cr.unregisterContentObserver(mCallLogContentObserver);
    }
    private class MmsContentObserver extends ContentObserver {

        public MmsContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.d("LUOBIAO", "MmsContentObserver onChange");
            int unreadNum = getMissedMmsCount();
            Intent intent = new Intent("com.wos.launcher3.action.MMS_CONTENT_OBSERVER");
            intent.putExtra("unreadNum", unreadNum);
            mContext.sendBroadcast(intent);
        }

    }

    private class CallLogContentObserver extends ContentObserver {

        public CallLogContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.d("LUOBIAO", "CallLogContentObserver onChange");
            int unreadNum = getMissedCallCount();
            Intent intent = new Intent("com.wos.launcher3.action.CALL_CONTENT_OBSERVER");
            intent.putExtra("unreadNum", unreadNum);
            mContext.sendBroadcast(intent);
        }

    }

    private static final Uri CONTENT_URI_SMS = Uri.parse("content://sms/");
    private static final Uri CONTENT_URI_MMS = Uri.parse("content://mms/inbox/");
    private static final Uri CONTENT_URI_MMS_SMS = Uri.parse("content://mms-sms/inbox/");
    private int getMissedCallCount() {
        int count = 0;

        try {
            Cursor c = mContext.getContentResolver().query(
                    CallLog.Calls.CONTENT_URI, new String[] { Calls.TYPE },
                    Calls.TYPE + "=? and " + Calls.NEW + "=?",
                    new String[] { Calls.MISSED_TYPE + "", "1" }, null);
            if (c != null) {
                count = c.getCount();
                c.close();
            }
        } catch (Exception e) {
        }

        return count;
    }

    private int getMissedMmsCount() {
        int count = 0;

        Cursor c = null;
        try {
            c = mContext.getContentResolver().query(CONTENT_URI_SMS, null,
                    "type = 1 and read = 0", null, null);
            if (c != null) {
                count += c.getCount();
                c.close();
            }
            c = mContext.getContentResolver().query(CONTENT_URI_MMS, null,
                    "read = 0", null, null);
            if (c != null) {
                count += c.getCount();
                c.close();
            }
        } catch (Exception e) {
        }
        return count;
    }

}
