package com.f1993yan.clnotify;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.ArrayList;
import android.os.Handler;

public class NotifyService extends Service{
    private RemoteViews remoteViews;
    private Context context;
    public static final String BUNDLE_MESSAGE = "com.f1993yan.NotifyService.message";
    private LocalBroadcastManager localBroadcastManager;
    private LocalServerReceive localServerReceive;
    private ArrayList<EventRecord> itemList;
    public static final int LOOP_FOR_SERVICE = 1;
    /*好奇怪啊，为什么默认添加的类是java.util.logging.Handler啊*/
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case LOOP_FOR_SERVICE:
                    remoteViews.removeAllViews(R.id.notification_content_root);
                    stopForeground(true);
                    itemList = msg.getData().getParcelableArrayList(BUNDLE_MESSAGE);
                    modifyWithRemoteView();
            }
        }
    };
    public NotifyService() {
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("NotifyService", "we create the service");
        context = getApplicationContext();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localServerReceive = new LocalServerReceive();
        localBroadcastManager.registerReceiver(localServerReceive,
                new IntentFilter(PreferFragment.PERIOD_CHANGE));
        CalendarEvent calendarEvent = new CalendarEvent(context);
        itemList = calendarEvent.OutputList();
        remoteViews = new RemoteViews(getPackageName(),R.layout.notification_main);
        initNotifyView();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new UpdateThread(handler,context)).start();
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        int interval = 5*60*1000;
        long triggerAtTime = SystemClock.elapsedRealtime()+interval;
        Intent i = new Intent(PreferFragment.PERIOD_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,i,0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(localServerReceive);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    private void initNotifyView(){
        Log.d("NotifyService", "初始化通知工作");
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        modifyWithRemoteView();
        Intent notifyIntent = new Intent();
        /*临时策略，需要修正
        * 2015年10月10日23:35:45进行修正，启动Dialog型的activity*/
//        notifyIntent.setComponent(new ComponentName("com.android.calendar", "com.android.calendar.LaunchActivity"));
        notifyIntent.setClass(context,InputDialog.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, 0);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setPriority(2);
                //听说，如果有新的Notification推送的话，会导致现有的bigContentView失效
        Notification mnotification = mBuilder.build();
        mnotification.bigContentView = remoteViews;
        startForeground(1, mnotification);
    }
    private void modifyWithRemoteView() {
        try{
            if (itemList.size()!=0) {
                for (int i = 0; i < itemList.size(); i++) {
                    RemoteViews itemView = new RemoteViews(getPackageName(), R.layout.notification_item);
                    EventRecord meventRecord = itemList.get(i);
                    itemView.setTextViewText(R.id.event_title, meventRecord.getEventName());
                    Log.d("NotifyService","一共有事件："+itemList.size());
                    Log.d("NotifyService", "第"+ i +"个事件是" + meventRecord.getEventName());
                    remoteViews.addView(R.id.notification_content_root, itemView);
                }
            }else{
                Log.d("NotifyService","没有事件发生");
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.d("NotifyService","没有传输数据");
        }
    }
    private class LocalServerReceive extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("NotifyService","广播接收器工作");
            if (intent.getAction().equals(PreferFragment.PERIOD_CHANGE)){
                int timeflag = intent.getIntExtra(PreferFragment.PERIOD_FLAG,172800000);
                CalendarEvent mCalendarEvent = new CalendarEvent(context,timeflag);
                itemList = mCalendarEvent.OutputList();
//                itemList = intent.getParcelableArrayListExtra(CalendarEvent.EVENT_RECORD);
                /*removeAllViews文档中是说移除的是参数View的子View*/
                remoteViews.removeAllViews(R.id.notification_content_root);
                /*Maybe this is an error ,as I was StartForward(),then Cancel() seen illogical
                * 好吧，我发现了stopForeground（），是我蠢了*/
                stopForeground(true);
                initNotifyView();
            }else if (intent.getAction().equals(PreferFragment.PERIOD_UPDATE)){
                startService(new Intent(context,NotifyService.class));
            }
        }
    }
}
