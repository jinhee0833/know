package com.helloants.mm.helloants1.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.helloants.mm.helloants1.R;
import com.helloants.mm.helloants1.activity.login.CardOffsetDayInsert;
import com.helloants.mm.helloants1.activity.login.LoginActivity;
import com.helloants.mm.helloants1.activity.login.SalaryInsert;
import com.helloants.mm.helloants1.db.bs.BsItem;
import com.helloants.mm.helloants1.db.content.NoticeDB;
import com.helloants.mm.helloants1.db.member.MemberDB;
import com.helloants.mm.helloants1.login.Cryptogram;
import com.helloants.mm.helloants1.login.LoginData;
import com.mongodb.BasicDBObject;

public class TempActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);

        String email = "";

        try {
            email = Cryptogram.Decrypt(LoginData.mEmail);
        } catch (Exception e) {
        }

        final String EMAIL = email;
        Thread thread = new Thread() {
            @Override
            public void run() {
                boolean bsItemExisted = BsItem.INSTANCE.isItemExisted();
                if (EMAIL.equals("")) {
                    startActivity(new Intent(getApplication(), LoginActivity.class));
                    TempActivity.this.finish();
                } else if (MemberDB.INSTANCE.find(new BasicDBObject("email", EMAIL)).next().get("salaryDate") == null) {
                    startActivity(new Intent(getApplication(), SalaryInsert.class));
                    TempActivity.this.finish();
                } else if (MemberDB.INSTANCE.isCardOff()
                        || !bsItemExisted) {
                    startActivity(new Intent(getApplication(), CardOffsetDayInsert.class));
                    TempActivity.this.finish();
                } else {
                    startActivity(new Intent(getApplication(), MainActivity.class));
                    NoticeDB.INSTANCE.settingImg();
                    TempActivity.this.finish();
                }
            }
        };

        thread.start();
    }

}
