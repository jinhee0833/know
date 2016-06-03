package com.helloants.mm.helloants1.data.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.helloants.mm.helloants1.db.bs.BsDB;
import com.helloants.mm.helloants1.db.member.MemberDB;
import com.helloants.mm.helloants1.login.LoginData;

import java.util.Date;
import java.util.Set;

/**
 * Created by kingherb on 2016-04-20.
 */
public class cardOffsetAlarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (LoginData.mEmail.equals("")) MemberDB.INSTANCE.init(context);

        String cardName = intent.getDataString();

        Set set = MemberDB.INSTANCE.myCardFind();
        String FinalCardName = "";
        String FinalAcount = "";
        for (Object card : set) {
            String temp = String.valueOf(card);
            String cardN[] = temp.split("~");
            if (cardName.equals(cardN[0])) {
                FinalCardName = cardN[2];
                FinalAcount = cardN[3];
            }
        }

        SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        Boolean push = pref.getBoolean("push", true);
        if (push == true) {
            NotificationFormat.NotificationPush(context, "헬로앤츠", FinalCardName + " 정산일 입니다. 자동 처리됩니다.", FinalCardName + " 정산일 입니다. 자동 처리됩니다.");
            NotificationFormat.NotificationPopup(context, FinalCardName + " 정산일 입니다. 자동 처리됩니다.");
        }
        int cardOffsetPrice = BsDB.INSTANCE.cardOffsetPrice(FinalCardName);


        BsDB.INSTANCE.newIsInsert(String.valueOf(cardOffsetPrice), "카드값 정산", new Date(), "repay", "self", FinalCardName + "-", FinalAcount + "-");


    }
}
