package com.f1993yan.clnotify;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by f1993yan on 2015/9/18.
 * This is the principle fraction for the app,achieving the event listed in the Calendar
 * 整个流程是先getSystemTimeStream获得当前系统时间，然后用setEndTime设置终止时间，之后调用
 * startWithService启动服务，期间则是先行调用initCalendarList而后调用Activity.startService
 */
public class CalendarEvent {
    private int TIME_FLAG;
    private Context context;
    private static final int MONTH_DAY_TIME = 0;
    private static final int YEAR_DAY_TIME = 1;
    public static final String EVENT_RECORD = "EventRecord";
    private int TimeIndex;
    private int TitleIndex;
    private long START_TIME;
    private static final long ONE_DAY_TIME = 24*60*60*1000;
    public CalendarEvent(Context context,int timeflag){
        this.TIME_FLAG = timeflag;
        this.context = context;
    }
    /*For Service only*/
    public CalendarEvent(Context context){
        this.context = context;
        SharedPreferences mPreference = PreferenceManager.getDefaultSharedPreferences(context);
        this.TIME_FLAG = Integer.parseInt(mPreference.getString("time_period","172800000"));
    }
    private long getSystemTimeStream(){
        Calendar mcalendar = Calendar.getInstance();
        return mcalendar.getTimeInMillis();
    }
    private long setEndTime(int timeFlag){
        Calendar mcalendar = Calendar.getInstance();
        if (timeFlag == MONTH_DAY_TIME){
            mcalendar.set(Calendar.DAY_OF_MONTH, 1);
            mcalendar.roll(Calendar.DAY_OF_MONTH, -1);
            Log.d("CalendarEvent", mcalendar.get(Calendar.DAY_OF_MONTH) + "");
            return mcalendar.get(Calendar.DAY_OF_MONTH)*ONE_DAY_TIME + START_TIME;
        }else if (timeFlag == YEAR_DAY_TIME){
            mcalendar.set(Calendar.DAY_OF_YEAR, 1);
            mcalendar.roll(Calendar.DAY_OF_YEAR, -1);
            Log.d("CalendarEvent",mcalendar.get(Calendar.DAY_OF_YEAR)+"");
            return mcalendar.get(Calendar.DAY_OF_YEAR)*ONE_DAY_TIME + START_TIME;
        }else{return timeFlag+START_TIME;}
    }
    private ArrayList<EventRecord> initCalendarList(){
        START_TIME = getSystemTimeStream();
        long END_TIME = setEndTime(TIME_FLAG);
        Log.d("CalendarEvent","The END_TIME is"+ END_TIME);
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder,START_TIME);
        ContentUris.appendId(builder, END_TIME);
        Cursor cursor = context.getContentResolver().query(builder.build(), null, null, null, null, null);
        ArrayList<EventRecord> mparcelList = new ArrayList<>();
        if (cursor!=null){
            /*好像有问题，首先EventRecord并不是多次实例化，而是不断修改，这样的话，
            * ArrayList中保存的还是多个EventRecord吗
            * 其次，如果多次实例化，在循环体外的setEventList()的父类是哪一个EventRecord*/
            TitleIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE);
            TimeIndex = cursor.getColumnIndexOrThrow(CalendarContract.Instances.START_DAY);
            while (cursor.moveToNext()){
                /*注意：cursor.getColumnIndex获取的是列索引号
                * 而getXXX需要上一个方法返回的列索引号*/
                EventRecord meventRecord = new EventRecord();
                Log.d("CalendarEvent","第一个事件" + cursor.getString(TitleIndex));
                Log.d("CalendarEvent", "The Loop has been started");
                ParcelEvent(meventRecord, cursor, mparcelList);
            }
        cursor.close();
        }
        return mparcelList;
    }
    public ArrayList<EventRecord> OutputList(){
        return initCalendarList();
    }
    public void sendBroadCastWhenChanged(){
        /*加了新线程，感觉更乱了
        * 我想我知道哪错了，新线程中的数据初始化的同时还进行了广播，服务先于广播产生
        * 我TM的加了一把锁.........好像没必要，先启动服务不就行了吗。。。。。不行啊，activity永远是先于service初始化完成的
        * startService()只是设置了一个标记，并不是立马进入到Service中*/
            new Thread(new Runnable() {
                @Override
                public void run() {
//                    ArrayList<EventRecord> parcelList = initCalendarList();
                /*不对，当启动service时，不能马上接收广播进行初始化*/
//                    LocalBroadcastManager mlocalBM = LocalBroadcastManager.getInstance(context);
//                    Intent mintent = new Intent(PreferFragment.PERIOD_CHANGE)
//                            .putParcelableArrayListExtra(EVENT_RECORD,parcelList);
//                    mlocalBM.sendBroadcast(mintent);
                    Log.d("CalendarEvent", "we send BroadCast");
                }
            }).start();
    }
    public void startTheService(){
        context.startService(new Intent(context, NotifyService.class));
    }
    public void ParcelEvent(EventRecord eventRecord,Cursor cursor,ArrayList<EventRecord> list){
        String mtitle = cursor.getString(TitleIndex);
        int mtime = cursor.getInt(TimeIndex);
        eventRecord.setEventName(mtitle);
        eventRecord.setBeginTime(mtime);
        list.add(eventRecord);
    }
}
