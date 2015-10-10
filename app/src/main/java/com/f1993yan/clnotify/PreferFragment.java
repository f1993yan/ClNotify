package com.f1993yan.clnotify;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by f1993yan on 2015/9/17.
 *There is a notice that the IDE suggest to put the initCalendarList into the branch thread
 */
public class PreferFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener
        ,Preference.OnPreferenceClickListener{
    private SharedPreferences format;
    private Context context;
    private ListPreference period_listPrefer;
    public static final String PERIOD_CHANGE = "com.f1993yan.time_period_change";
    public static final String PERIOD_FLAG = "com.f1993yan.time_period_flag";
    public static final String PERIOD_UPDATE = "com.f1993yan.time_period_update";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_fragment);
        initSettingItem();
        context = getActivity().getApplicationContext();
        /*发现不能用this直接获取context
        * 这属于一个注意点
        * 通过getActivity().getApplicationContext()间接获取context*/
//        context.startService(new Intent(context, NotifyService.class)
//                .putExtra(PreferFragment.PERIOD_CHANGE,Integer.parseInt(period_listPrefer.getValue()))
//        );
        format = PreferenceManager.getDefaultSharedPreferences(context);
        if (format!=null)
            Log.d("PreferenceFragment","获得sharePreference");
//        CheckPreferenceAndSend();
    }

    private void initSettingItem() {
        period_listPrefer = (ListPreference)findPreference("time_period");
        period_listPrefer.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals("time_period")){
            /*我既然已经在CheckWithPreferenceAndStartService中执行了listPreference的数值检测
            * 那么就不需要在进行Broadcast的操作
            * 如果直接执行下面的函数，则获取的值是改变前的，即这个方法是在执行完方法体内的函数之后再
            * 改变preference*/
            LocalBroadcastManager lBroadMG = LocalBroadcastManager.getInstance(context);
            Intent mintent = new Intent(PERIOD_CHANGE)
                    .putExtra(PERIOD_FLAG, Integer.parseInt((String) newValue));
            Log.d("PreferFragment",(String)newValue);
            lBroadMG.sendBroadcast(mintent);
//            CheckPreferenceAndSend(Integer.parseInt((String)newValue));
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return false;
    }
    private void CheckPreferenceAndSend(){
        int firstTimePeriod =  Integer.parseInt(period_listPrefer.getValue());
        CheckPreferenceAndSend(firstTimePeriod);
    }
    private void CheckPreferenceAndSend(int value){
        /*这里有一个我现在还没弄懂的问题，即使我的entryValues设置成了integer-array
        * 在listPreference.getValue中获取的还是String的类型，
        * 这里用了Integer.parseInt进行了强制的数据转化*/
        Log.d("time_period", period_listPrefer.getValue());
        CalendarEvent calendarEvent = new CalendarEvent(context, value);
        calendarEvent.sendBroadCastWhenChanged();
    }
}
