package com.helloants.mm.helloants1.data.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Created by park on 2016-03-23.
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        Boolean push =  pref.getBoolean("push", true);
        if(push == true) {
            NotificationFormat.NotificationSalaryPush(context,"야호! 오늘은 월급날!","수입을 입력해 주세요. 한달간 고생하셨습니다","야호! 오늘은 월급날! 수입을 입력해 주세요.");
        }
    }
}