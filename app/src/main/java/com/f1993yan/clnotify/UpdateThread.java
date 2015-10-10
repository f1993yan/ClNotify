package com.f1993yan.clnotify;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;

import java.util.ArrayList;
import android.os.Handler;

/**
 * Created by f1993yan on 2015/10/5.
 * This thread is for the updating motivation
 */
public class UpdateThread implements Runnable {
    private Handler handler;
//    private long timeFlag;
    private Context context;
    public UpdateThread(Handler mHandler,Context context){
        super();
        this.handler = mHandler;
//        this.timeFlag = timeFlag*60*1000;
        this.context = context;
    }
    @Override
    public void run() {
        try {
//            Thread.sleep(timeFlag);
            CalendarEvent calendarEvent = new CalendarEvent(context);
            Message msg = Message.obtain();
            Bundle mBundle = new Bundle(ClassLoader.getSystemClassLoader());
            mBundle.putParcelableArrayList(NotifyService.BUNDLE_MESSAGE,calendarEvent.OutputList());
            msg.setData(mBundle);
            msg.what = NotifyService.LOOP_FOR_SERVICE;
            handler.sendMessage(msg);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
