package com.helloants.mm.helloants1.activity.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.TextView;

import com.helloants.mm.helloants1.R;
import com.helloants.mm.helloants1.activity.MainActivity;
import com.helloants.mm.helloants1.activity.TempActivity;
import com.helloants.mm.helloants1.data.type.BSType;
import com.helloants.mm.helloants1.db.bs.BsDB;
import com.helloants.mm.helloants1.db.bs.BsItem;
import com.helloants.mm.helloants1.db.member.MemberDB;
import com.helloants.mm.helloants1.loading.WaitDlg;
import com.helloants.mm.helloants1.login.Cryptogram;
import com.helloants.mm.helloants1.login.LoginData;
import com.mongodb.BasicDBObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CardOffsetDayInsert extends AppCompatActivity {
    private ArrayList<NumberPicker> pikerList;
    private String[] cardN;
    private static ArrayList<BSType> mList;
    private static ArrayList<BSType> mList2;
    private ArrayList<String> cardNameList;
    private BackPressCloseHandler backPressCloseHandler;
    private ScrollView layout;
    private WaitDlg mWaitDlg;
    private static Set cardOffDateSet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            final Set myCardSet = MemberDB.INSTANCE.myCreditCardFind();

            if (isEmptySet(myCardSet)) {
                AssetInsertThread at = new AssetInsertThread();
                DebtInsertThread dt = new DebtInsertThread();

                try {
                    at.start();
                    at.join();
                    dt.start();
                    dt.join();
                } catch (Exception e) {
                }
                Intent intent = new Intent(CardOffsetDayInsert.this, MainActivity.class);
                startActivity(intent);
                CardOffsetDayInsert.this.finish();
            } else {
                setContentView(R.layout.activity_card_offset_day_insert);
                layout = (ScrollView) findViewById(R.id.sv_root_card_offset_insert);
                LinearLayout root = (LinearLayout) findViewById(R.id.linear_cardoffset);

                //툴바 이미지 백 버튼
                ImageButton btnBack = (ImageButton) findViewById(R.id.img_btn_aif);
                btnBack.setImageResource(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha);
                btnBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CardOffsetDayInsert.this.onBackPressed();
                    }
                });

                //카드 정산일 집어넣을 셋
                cardOffDateSet = new HashSet();

                cardNameList = new ArrayList<>();
                pikerList = new ArrayList<>();
                for (Object card : myCardSet) {
                    String cardName = String.valueOf(card);
                    cardN = cardName.split("~");
                    LinearLayout layout = new LinearLayout(this);

                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View v = inflater.inflate(R.layout.card_offset_insert, layout);

                    //카드이름만 추출해서 넣기
                    TextView tvxCardName = (TextView) v.findViewById(R.id.txv_card_name);
                    tvxCardName.setText(cardN[0]);
                    cardNameList.add(cardN[0]);

                    NumberPicker datePicker = (NumberPicker) v.findViewById(R.id.npik_carddate_cardoffset);
                    datePicker.setMinValue(1);
                    datePicker.setMaxValue(31);
                    datePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
                    pikerList.add(datePicker);
                    root.addView(layout);

                }

                //다음 버튼 누르면 디비에 입력
                Button nextBtn = (Button) findViewById(R.id.btn_next_cardoffset);
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mWaitDlg = new WaitDlg(CardOffsetDayInsert.this, null, "Loading...");
                        mWaitDlg.start();

                        for (int i = 0; i < pikerList.size(); i++) {
                            cardOffDateSet.add(cardNameList.get(i) + "~" + pikerList.get(i).getValue());
                        }

                        new Thread() {
                            @Override
                            public void run() {
                                //데이트 픽커에서 가지고온 날짜
                                BasicDBObject user = new BasicDBObject("cardOffsetDay", cardOffDateSet);

                                String email = "";
                                try {
                                    email = Cryptogram.INSTANCE.Decrypt(LoginData.mEmail);
                                } catch (Exception e) {
                                }

                                MemberDB.INSTANCE.update(new BasicDBObject("email", email),
                                        new BasicDBObject("$set", user));
                            }
                        }.start();

                        AssetInsertThread at = new AssetInsertThread();
                        DebtInsertThread dt = new DebtInsertThread();

                        try {
                            at.start();
                            at.join();
                            dt.start();
                            dt.join();
                        } catch (Exception e) {
                        }

                        WaitDlg.stop(mWaitDlg);
                        Intent MainActivity = new Intent(CardOffsetDayInsert.this, TempActivity.class);
                        startActivity(MainActivity);
                        CardOffsetDayInsert.this.finish();
                    }
                });

                backPressCloseHandler = new BackPressCloseHandler(this);
            }

        } catch (Exception e) {
        }
    }

    private boolean isEmptySet(Set set) {
        boolean isEmpty = MemberDB.INSTANCE.myCardOffsetFind().isEmpty();
        for (Object obj : set) {
            String str = String.valueOf(obj);
            if (str.contains("credit")
                    && isEmpty) return false;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        try {
            backPressCloseHandler.onBackPressed();
        } catch (NullPointerException e) {
            super.onBackPressed();
        }
    }

    public static class AssetInsertThread extends Thread {
        @Override
        public void run() {
            mList = new ArrayList();
            mList.add(new BSType("집값 혹은 보증금+", "house"));
            mList.add(new BSType("예·적금+", "save"));
            mList.add(new BSType("현금+", "income"));
            mList.add(new BSType("보험+", "save"));
            mList.add(new BSType("펀드+", "save"));
            mList.add(new BSType("주식+", "save"));
            mList.add(new BSType("자동차+", "car"));
            Set myCardSet = MemberDB.INSTANCE.myCardFind();
            for (Object card : myCardSet) {
                String cardName = String.valueOf(card);
                String[] cardN = cardName.split("~");
                if (cardName.contains("check")) {
                    mList.add(new BSType(cardN[0] + "은행 계좌+", "save"));
                } else {
                    mList.add(new BSType(cardN[0] + "카드 연동 계좌+", "save"));
                }
            }

            BsDB.INSTANCE.assetInsert(mList, "asset");
            BsItem.INSTANCE.insertAsset(mList);
        }
    }

    public static class DebtInsertThread extends Thread {
        @Override
        public void run() {
            //부채기본값
            mList2 = new ArrayList<BSType>();
            mList2.add(new BSType("부동산 대출+", "loan"));
            mList2.add(new BSType("신용 대출+", "loan"));
            mList2.add(new BSType("학자금 대출+", "loan"));

            Set myCardSet = cardOffDateSet;
            if (myCardSet != null) {
                for (Object card : myCardSet) {
                    String cardName = String.valueOf(card);
                    String[] cardN = cardName.split("~");

                    int cardDebtValue = BsDB.INSTANCE.firstCreditCardDebt(cardN[0] + "카드", cardN[1]);

                    mList2.add(new BSType(cardN[0] + "카드+", "loan", cardDebtValue));
                }
            }

            BsDB.INSTANCE.assetInsert(mList2, "debt");
            BsItem.INSTANCE.insertDebt(mList2);
            BsItem.INSTANCE.insert();
        }
    }

    private class BackPressCloseHandler {
        private long backKeyPressedTime = 0;
        private Activity activity;

        public BackPressCloseHandler(Activity context) {
            this.activity = context;
        }

        public void onBackPressed() {
            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis();
                showGuide();
                return;
            }
            if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                defaultAsset();
                activity.finish();
            }
        }

        private void showGuide() {
            Snackbar.make(layout, "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Snackbar.LENGTH_SHORT).show();
        }

        private void defaultAsset() {
            AssetInsertThread at = new AssetInsertThread();
            DebtInsertThread dt = new DebtInsertThread();

            try {
                at.start();
                at.join();
                dt.start();
                dt.join();
            } catch (Exception e) {
            }
        }
    }
}