package com.helloants.mm.helloants1.activity.mypage;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.helloants.mm.helloants1.R;
import com.helloants.mm.helloants1.db.member.MemberDB;
import com.helloants.mm.helloants1.fragment.wm.BSFragment;
import com.helloants.mm.helloants1.loading.WaitDlg;
import com.helloants.mm.helloants1.login.Cryptogram;
import com.helloants.mm.helloants1.login.LoginData;
import com.mongodb.BasicDBObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class EditSalary extends AppCompatActivity {
    private String[] splitData;
    private ArrayList<String> cardNameList;
    private ArrayList<NumberPicker> pikerList;
    private boolean isInit;
    private Button modifyBtn;
    private BackPressCloseHandler backPressCloseHandler;
    WaitDlg mWaitDlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            isInit = true;
            setContentView(R.layout.activity_edit_salary);
            LinearLayout root = (LinearLayout) findViewById(R.id.linear_cardoffset_editsalary);
            String alreadyBudget = MemberDB.INSTANCE.budgetFind();
            final EditText budget = (EditText) findViewById(R.id.edit_budget_salarymodify);
            budget.setText(alreadyBudget);
            //툴바 타이틀 텍스트뷰
            TextView txvTitle = (TextView) findViewById(R.id.txv_title_editsalary);
            txvTitle.setText("월급날, 카드정산일 수정");

            //툴바 이미지 백 버튼
            ImageButton btnBack = (ImageButton) findViewById(R.id.img_btn_editsalary);
            btnBack.setImageResource(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditSalary.this.onBackPressed();
                }
            });

            //월급날 입력
            int salaryDay = MemberDB.INSTANCE.salaryDayFind();
            final NumberPicker datePicker1 = (NumberPicker) findViewById(R.id.npik_salarydate_editsalary);
            datePicker1.setMinValue(1);
            datePicker1.setMaxValue(31);
            datePicker1.setValue(salaryDay);
            datePicker1.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

            //카드 정산일 수정
            //이미 입력된 거 가져오기
            Set beforeCardOffset = MemberDB.INSTANCE.myCardOffsetFind();
            final Set afterCardOffset = new HashSet();

            cardNameList = new ArrayList<>();
            pikerList = new ArrayList<>();

            if (beforeCardOffset.isEmpty()) {
                Set set = MemberDB.INSTANCE.myCreditCardFind();

                if (set.isEmpty()) {
                    CardView cv = (CardView) findViewById(R.id.cardview_card_modify_edit_salary);
                    cv.setVisibility(View.GONE);
                } else {
                    isInit = false;
                    for (Object obj : set) {
                        String data = String.valueOf(obj);
                        splitData = data.split("~");

                        if (splitData[1].equals("credit")) {
                            LinearLayout layout = new LinearLayout(this);

                            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            View v = inflater.inflate(R.layout.card_offset_insert, layout);

                            TextView tvxCardName = (TextView) v.findViewById(R.id.txv_card_name);
                            tvxCardName.setText(splitData[0] + "카드");
                            cardNameList.add(splitData[0]);

                            NumberPicker datePicker = (NumberPicker) v.findViewById(R.id.npik_carddate_cardoffset);
                            datePicker.setMinValue(1);
                            datePicker.setMaxValue(31);
                            datePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
                            pikerList.add(datePicker);

                            root.addView(layout);
                        }
                    }
                }
            } else {
                //새로 입력할 데이터 담을 셋
                for (Object set : beforeCardOffset) {
                    String data = String.valueOf(set);
                    splitData = data.split("~");

                    LinearLayout layout = new LinearLayout(this);

                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View v = inflater.inflate(R.layout.card_offset_insert, layout);

                    TextView tvxCardName = (TextView) v.findViewById(R.id.txv_card_name);
                    tvxCardName.setText(splitData[0] + "카드");
                    cardNameList.add(splitData[0]);

                    NumberPicker datePicker = (NumberPicker) v.findViewById(R.id.npik_carddate_cardoffset);
                    datePicker.setMinValue(1);
                    datePicker.setMaxValue(31);
                    datePicker.setValue(Integer.parseInt(splitData[1]));
                    datePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
                    pikerList.add(datePicker);

                    root.addView(layout);
                }
            }

            //카드정산일 수정 버튼
            modifyBtn = (Button) findViewById(R.id.btn_next_editsalary);
            modifyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mWaitDlg = new WaitDlg(EditSalary.this, null, "Loading...");
                    mWaitDlg.start();
                    for (int i = 0; i < pikerList.size(); i++) {
                        afterCardOffset.add(cardNameList.get(i) + "~" + pikerList.get(i).getValue());
                    }

                    MemberDB.INSTANCE.init(EditSalary.this);
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            //데이트 픽커에서 가지고온 날짜
                            BasicDBObject user = new BasicDBObject("cardOffsetDay", afterCardOffset);

                            String email = "";
                            try {
                                email = Cryptogram.INSTANCE.Decrypt(LoginData.mEmail);
                            } catch (Exception e) {
                            }

                            MemberDB.INSTANCE.update(new BasicDBObject("email", email),
                                    new BasicDBObject("$set", user));
                        }
                    };
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    WaitDlg.stop(mWaitDlg);
                    Toast.makeText(EditSalary.this, "카드 정산일이 수정되었습니다", Toast.LENGTH_SHORT).show();
                }
            });

            //월급날 수정
            Button modifyBtn2 = (Button) findViewById(R.id.btn_next_editsalary2);
            modifyBtn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mWaitDlg = new WaitDlg(EditSalary.this, null, "Loading...");
                    mWaitDlg.start();

                    MemberDB.INSTANCE.init(EditSalary.this);
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            //데이트 픽커에서 가지고온 날짜
                            BasicDBObject user = new BasicDBObject("salaryDate", datePicker1.getValue());

                            String email = "";
                            try {
                                email = Cryptogram.INSTANCE.Decrypt(LoginData.mEmail);
                            } catch (Exception e) {
                            }

                            MemberDB.INSTANCE.update(new BasicDBObject("email", email),
                                    new BasicDBObject("$set", user));
                        }
                    };
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    WaitDlg.stop(mWaitDlg);
                    Toast.makeText(EditSalary.this, "월급날이 수정되었습니다", Toast.LENGTH_SHORT).show();
                }
            });

            //한달예산
            Button modifyBtn3 = (Button) findViewById(R.id.btn_next_budget);
            modifyBtn3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mWaitDlg = new WaitDlg(EditSalary.this, null, "Loading...");
                    mWaitDlg.start();
                    for (int i = 0; i < pikerList.size(); i++) {
                        afterCardOffset.add(cardNameList.get(i) + "~" + pikerList.get(i).getValue());
                    }

                    MemberDB.INSTANCE.init(EditSalary.this);
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            //데이트 픽커에서 가지고온 날짜
                            BasicDBObject user = new BasicDBObject("budget", budget.getText().toString());

                            String email = "";
                            try {
                                email = Cryptogram.INSTANCE.Decrypt(LoginData.mEmail);
                            } catch (Exception e) {
                            }

                            MemberDB.INSTANCE.update(new BasicDBObject("email", email),
                                    new BasicDBObject("$set", user));
                        }
                    };
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                    }

                    ArrayList yVals3 = new ArrayList<Entry>();
                    for (int i = 0; i < 31; i++) {
                        try {
                            yVals3.add(new Entry(Long.parseLong(budget.getText().toString()), i));
                        } catch (NumberFormatException e) {
                            yVals3.add(new Entry(Long.MAX_VALUE, i));
                        }
                    }

                    try {
                        BSFragment.budget.setText("예산   " + String.format("%,d", Integer.parseInt(budget.getText().toString())) + "원");
                    } catch (NumberFormatException ex) {
                        budget.setText("예산   21억원 이상");
                    }

                    LineDataSet set1 = BSFragment.set1;
                    LineDataSet set2 = BSFragment.set2;
                    LineDataSet set3 = new LineDataSet(yVals3, "");

                    set3.setLineWidth(3f);
                    set3.setColor(Color.parseColor("#FF0000"));
                    set3.setDrawCircles(false);

                    ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                    dataSets.add(set3);
                    dataSets.add(set2);
                    dataSets.add(set1);
                    LineData data = new LineData(BSFragment.xVals, dataSets);
                    data.setValueTextSize(9f);
                    data.setDrawValues(false);
                    data.setHighlightEnabled(false);
                    BSFragment.mLineChart.setData(data);

                    BSFragment.mLineChart.invalidate();

                    WaitDlg.stop(mWaitDlg);
                    Toast.makeText(EditSalary.this, "한달 예산이 수정되었습니다", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
        }
        backPressCloseHandler = new BackPressCloseHandler(this);
    }

    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }

    public class BackPressCloseHandler {
        private Activity activity;

        public BackPressCloseHandler(Activity context) {
            this.activity = context;
        }

        public void onBackPressed() {
            if (!isInit) {
                modifyBtn.performClick();
            }
            activity.finish();
        }
    }
}