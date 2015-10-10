package com.f1993yan.clnotify;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * Created by f1993yan on 2015/9/22.
 * 我试着自己想了一个办法在一个类中进行数据赋值和数组组合，没有理论依据，不知道会有什么奇怪的事情
 * 这里先标记一下，
 * 有个发现，Intent居然有个putParcelArrayListExtra()方法，这样的话就不需要我再创建了
 */
public class EventRecord implements Parcelable {
    private String EventName;
    private int BeginTime;
    /*我这里不确定会不会报错，因为ArrayList组合的就是本身的类*/
//    private ArrayList<EventRecord> EventList;
//    public EventRecord(Parcel in) {
//        this.EventName = in.readString();
//        this.BeginTime = in.readInt();
//    }
//    public EventRecord(){
//    }
    public static final Creator<EventRecord> CREATOR = new Creator<EventRecord>() {
        @Override
        public EventRecord createFromParcel(Parcel in) {
            EventRecord eventRecord = new EventRecord();
            eventRecord.EventName = in.readString();
            eventRecord.BeginTime = in.readInt();
            return eventRecord;
        }

        @Override
        public EventRecord[] newArray(int size) {
            return new EventRecord[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(EventName);
        dest.writeInt(BeginTime);
    }
    public void setEventName(String Name){
        this.EventName = Name;
    }
    public String getEventName(){
        return this.EventName;
    }
    public void setBeginTime(int time){
        this.BeginTime = time;
    }
    public int getBeginTime(){
        return this.BeginTime;
    }
}
